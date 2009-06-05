package org.jboss.seam.jms;

import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.naming.NamingException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.Naming;

/**
 * Manager for a JMS TopicPublisher for a named JMS topic
 * 
 * @author Gavin King
 *
 */
@Scope(ScopeType.EVENT)
@BypassInterceptors
@Install(false)
public class ManagedTopicPublisher
{
   private String topicJndiName;
   
   private TopicPublisher topicPublisher;
   
   /**
    * The JNDI name of the topic
    */
   public String getTopicJndiName()
   {
      return topicJndiName;
   }

   public void setTopicJndiName(String jndiName)
   {
      this.topicJndiName = jndiName;
   }
   
   public Topic getTopic() throws NamingException
   {
      return (Topic) Naming.getInitialContext().lookup(topicJndiName);
   }
   
   @Create
   public void create() throws JMSException, NamingException
   {
      topicPublisher = org.jboss.seam.jms.TopicSession.instance().createPublisher( getTopic() );
   }
   
   @Destroy
   public void destroy() throws JMSException
   {
      topicPublisher.close();
   }
   
   @Unwrap
   public TopicPublisher getTopicPublisher()
   {
      return topicPublisher;
   }
   
   @Override
   public String toString()
   {
      return "TopicConnection(" + topicJndiName + ")";
   }

}
