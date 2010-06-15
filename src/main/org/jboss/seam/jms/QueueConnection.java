package org.jboss.seam.jms;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.naming.NamingException;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Naming;

/**
 * Manager for a JMS QueueConnection. By default, the JBoss MQ UIL2.
 * 
 * @author Gavin King
 * 
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Name("org.jboss.seam.jms.queueConnection")
@Install(precedence = BUILT_IN, genericDependencies = ManagedQueueSender.class)
public class QueueConnection
{
   
   @Logger
   private Log log;
   
   private String queueConnectionFactoryJndiName = "UIL2ConnectionFactory";
   private javax.jms.QueueConnection queueConnection;
   
   /**
    * The JNDI name of the QueueConnectionFactory
    */
   public String getQueueConnectionFactoryJndiName()
   {
      return queueConnectionFactoryJndiName;
   }
   
   public void setQueueConnectionFactoryJndiName(String jndiName)
   {
      this.queueConnectionFactoryJndiName = jndiName;
   }
   
   @Create
   public void init() throws NamingException, JMSException
   {
      queueConnection = getQueueConnectionFactory().createQueueConnection();
      queueConnection.start();
   }
   
   @Destroy
   public void destroy() throws JMSException
   {
      try
      {
         queueConnection.stop();
      }
      catch (javax.jms.IllegalStateException e)
      {
         // as for JEE v5 specs, section EE 6.6
         // At least WebSphere v7 enforce this
         log.warn("queueSession.stop() called during @Destroy in an invalid context for this container. Msg={0}", e.getMessage());
      }
      
      queueConnection.close();
   }
   
   private QueueConnectionFactory getQueueConnectionFactory() throws NamingException
   {
      return (QueueConnectionFactory) Naming.getInitialContext().lookup(queueConnectionFactoryJndiName);
   }
   
   @Unwrap
   public javax.jms.QueueConnection getQueueConnection()
   {
      return queueConnection;
   }
   
   public static javax.jms.QueueConnection instance()
   {
      return (javax.jms.QueueConnection) Component.getInstance(QueueConnection.class);
   }
   
   @Override
   public String toString()
   {
      return "QueueConnection(" + queueConnectionFactoryJndiName + ")";
   }
   
}
