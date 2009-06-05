package org.jboss.seam.jms;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.naming.NamingException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.Naming;

/**
 * Manager for a JMS QueueSender for a named JMS queue
 * 
 * @author Gavin King
 *
 */
@Scope(ScopeType.EVENT)
@BypassInterceptors
public class ManagedQueueSender
{
   private String queueJndiName;
   
   private QueueSender queueSender;

   /**
    * The JNDI name of the queue
    */
   public String getQueueJndiName()
   {
      return queueJndiName;
   }

   public void setQueueJndiName(String jndiName)
   {
      this.queueJndiName = jndiName;
   }
   
   public Queue getQueue() throws NamingException
   {
      return (Queue) Naming.getInitialContext().lookup(queueJndiName);
   }
   
   @Create
   public void create() throws JMSException, NamingException
   {
      queueSender = org.jboss.seam.jms.QueueSession.instance().createSender( getQueue() );
   }
   
   @Destroy
   public void destroy() throws JMSException
   {
      queueSender.close();
   }
   
   @Unwrap
   public QueueSender getQueueSender()
   {
      return queueSender;
   }
   
   @Override
   public String toString()
   {
      return "ManagedQueueSender(" + queueJndiName + ")";
   }

}
