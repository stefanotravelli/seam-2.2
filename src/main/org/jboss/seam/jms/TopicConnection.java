package org.jboss.seam.jms;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import javax.jms.JMSException;
import javax.jms.TopicConnectionFactory;
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
import org.jboss.seam.util.Naming;

/**
 * Manager for a JMS TopicConnection. By default, the JBoss MQ UIL2.
 * 
 * @author Gavin King
 * 
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Name("org.jboss.seam.jms.topicConnection")
@Install(precedence=BUILT_IN, genericDependencies=ManagedTopicPublisher.class)
public class TopicConnection
{
   private String topicConnectionFactoryJndiName = "UIL2ConnectionFactory";
   private javax.jms.TopicConnection topicConnection;

   /**
    * The JNDI name of the TopicConnectionFactory
    */
   public String getTopicConnectionFactoryJndiName()
   {
      return topicConnectionFactoryJndiName;
   }

   public void setTopicConnectionFactoryJndiName(String jndiName)
   {
      this.topicConnectionFactoryJndiName = jndiName;
   }
   
   @Create
   public void init() throws NamingException, JMSException
   {
      topicConnection = getTopicConnectionFactory().createTopicConnection();
      topicConnection.start();
   }
   
   @Destroy
   public void destroy() throws JMSException
   {
      topicConnection.stop();
      topicConnection.close();
   }

   private TopicConnectionFactory getTopicConnectionFactory() throws NamingException
   {
      return (TopicConnectionFactory) Naming.getInitialContext().lookup(getTopicConnectionFactoryJndiName());
   }
   
   @Unwrap
   public javax.jms.TopicConnection getTopicConnection()
   {
      return topicConnection;
   }
   
   public static javax.jms.TopicConnection instance()
   {
      return (javax.jms.TopicConnection) Component.getInstance(TopicConnection.class);
   }

   @Override
   public String toString()
   {
      return "TopicConnection(" + getTopicConnectionFactoryJndiName() + ")";
   }

}
