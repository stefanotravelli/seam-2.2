package org.jboss.seam.jms;

import java.lang.reflect.ParameterizedType;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.jboss.seam.Component;
import org.jboss.seam.async.AbstractDispatcher;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;

/**
 * <p>
 * A class which provides a contextual-thread context for message-driven beans
 * in the scope of which Seam components can be instantiated and invoked.
 * </p>
 * 
 * <p>
 * NOTE: Users are still likely to get the error reported in <a
 * href="https://jira.jboss.org/jira/browse/EJBTHREE-898">EJBTHREE-898</a> if a
 * transaction operation is performed in the message handler.
 * </p>
 * 
 * @author Dan Allen
 */
public abstract class ContextualMessageHandlerRequest<T>
{
   private Message message;
   private boolean setupRequired = false;
   private Object result = null;
   private T delegate;
   
   public ContextualMessageHandlerRequest(Message message)
   {
      this.message = message;
      this.setupRequired = !Contexts.isApplicationContextActive() && !Contexts.isEventContextActive();
   }
   
   public void run() throws Exception
   {
      setup();
      try
      {
         process();
      }
      finally
      {
         cleanup();
      }
   }
   
   public abstract void process() throws Exception;
   
   protected void setResult(Object result)
   {
      this.result = result;
   }
   
   public T getDelegate()
   {
      return delegate;
   }
   
   public Object getResult()
   {
      return result;
   }
   
   public Message getMessage()
   {
      return message;
   }
   
   /**
    * A convenience method to return the message as a TextMessage. If the
    * message is not a TextMessage, a null value is returned.
    */
   public TextMessage getTextMessage()
   {
      return message instanceof TextMessage ? (TextMessage) message : null;
   }
   
   @SuppressWarnings("unchecked")
   private void setup()
   {
      if (setupRequired)
      {
         Lifecycle.beginCall();
      }
      
      Contexts.getEventContext().set(AbstractDispatcher.EXECUTING_ASYNCHRONOUS_CALL, true);
      Class<T> delegateClass = getDelegateClass();
      if (delegateClass != null)
      {
         delegate = (T) Component.getInstance(delegateClass);
      }
   }
   
   private void cleanup()
   {
      Contexts.getEventContext().remove(AbstractDispatcher.EXECUTING_ASYNCHRONOUS_CALL);
      if (setupRequired)
      {
         Lifecycle.endCall();
      }
   }
   
   /**
    * Get the type specified in the type parameter of the class. TODO This could
    * be made safer
    */
   @SuppressWarnings("unchecked")
   public Class<T> getDelegateClass()
   {
      return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
   }
}
