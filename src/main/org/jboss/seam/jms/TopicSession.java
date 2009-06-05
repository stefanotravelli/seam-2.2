package org.jboss.seam.jms;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.NamingException;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Manager for a JMS TopicSession
 * 
 * @author Gavin King
 *
 */
@Scope(ScopeType.EVENT)
@BypassInterceptors
@Name("org.jboss.seam.jms.topicSession")
@Install(precedence=BUILT_IN, genericDependencies=ManagedTopicPublisher.class)
public class TopicSession
{
   
   private javax.jms.TopicSession topicSession;
   
   @Create
   public void create() throws JMSException, NamingException
   {
      //TODO: i really want a transactional session!
      topicSession = TopicConnection.instance().createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
   }
   
   @Destroy
   public void destroy() throws JMSException
   {
      topicSession.close();
   }
   
   @Unwrap
   public javax.jms.TopicSession getTopicSession()
   {
      return topicSession;
   }
   
   public static javax.jms.TopicSession instance()
   {
      return (javax.jms.TopicSession) Component.getInstance(TopicSession.class);
   }
   
}
