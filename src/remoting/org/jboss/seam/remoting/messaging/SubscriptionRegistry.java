package org.jboss.seam.remoting.messaging;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.TopicConnection;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 *
 * @author Shane Bryzak
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Name("org.jboss.seam.remoting.messaging.subscriptionRegistry")
@Install(value = false, precedence=BUILT_IN)
public class SubscriptionRegistry
{
  public static final String CONTEXT_USER_TOKENS =
      "org.jboss.seam.remoting.messaging.SubscriptionRegistry.userTokens";

  private static final LogProvider log = Logging.getLogProvider(SubscriptionRegistry.class);

  private String connectionProvider;

  private volatile TopicConnection topicConnection;

  private Object monitor = new Object();

  private Map<String,RemoteSubscriber> subscriptions = new HashMap<String,RemoteSubscriber>();

  /**
   * Contains a list of all the topics that clients are allowed to subscribe to.
   */
  private Set<String> allowedTopics = new HashSet<String>();

  public static SubscriptionRegistry instance()
  {
    SubscriptionRegistry registry = (SubscriptionRegistry) Component.getInstance(SubscriptionRegistry.class);

    if (registry == null)
    {
      throw new IllegalStateException("No SubscriptionRegistry exists");
    }

    return registry;
  }

  public Set<String> getAllowedTopics()
  {
    return allowedTopics;
  }

  public void setAllowedTopics(Set<String> allowedTopics)
  {
    this.allowedTopics = allowedTopics;
  }

  public String getConnectionProvider()
  {
    return connectionProvider;
  }

  public void setConnectionProvider(String connectionProvider)
  {
    this.connectionProvider = connectionProvider;
  }

  private TopicConnection getTopicConnection()
    throws Exception
  {
    if (topicConnection == null)
    {
      synchronized(monitor)
      {
        if (topicConnection == null)
        {
            topicConnection = org.jboss.seam.jms.TopicConnection.instance();
            
            topicConnection.setExceptionListener(new ExceptionListener() {
              public void onException(JMSException ex)
              {
                // swallow the exception for now - do we need to try and reconnect???
              }
            });
            topicConnection.start();
        }
      }
    }
    return topicConnection;
  }

  public RemoteSubscriber subscribe(String topicName)
  {
    if (!allowedTopics.contains(topicName))
      throw new IllegalArgumentException(String.format(
        "Cannot subscribe to a topic that is not allowed. Topic [%s] is not an " +
        "allowed topic.", topicName));

    RemoteSubscriber sub = new RemoteSubscriber(UUID.randomUUID().toString(), topicName);

    try {
      sub.subscribe(getTopicConnection());
      subscriptions.put(sub.getToken(), sub);

      // Save the client's token in their session context
      getUserTokens().add(sub.getToken());

      return sub;
    }
    catch (Exception ex) {
      log.error(ex);
      return null;
    }
  }

  /**
   *
   * @return Set
   */
  public Set getUserTokens()
  {
    Context session = Contexts.getSessionContext();
    if (session.get(CONTEXT_USER_TOKENS) == null)
    {
      synchronized(session)
      {
        if (session.get(CONTEXT_USER_TOKENS) == null)
          session.set(CONTEXT_USER_TOKENS, new HashSet<String> ());
      }
    }
    return (Set) session.get(CONTEXT_USER_TOKENS);
  }

  public RemoteSubscriber getSubscription(String token)
  {
    if (!getUserTokens().contains(token))
      throw new IllegalArgumentException(
        "Invalid token argument - token not found in Session Context.");

    return subscriptions.get(token);
  }
}
