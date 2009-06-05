package org.jboss.seam.async;

import org.jboss.seam.core.Events;

/**
 * An asynchronous event
 * 
 * @author Gavin King
 *
 */
public class AsynchronousEvent extends Asynchronous
{
   static final long serialVersionUID = 2074586442931427819L;
   
   private String type;
   private Object[] parameters;

   public AsynchronousEvent(String type, Object[] parameters)
   {
      this.type = type;
      this.parameters = parameters;
   }

   @Override
   public void execute(Object timer)
   {
      new ContextualAsynchronousRequest(timer)
      {
         
         @Override
         protected void process()
         {
            Events.instance().raiseEvent(type, parameters);
         }
         
      }.run();
   }
   
   @Override
   public String toString()
   {
      return "AsynchronousEvent(" + type + ')';
   }
   
   protected String getType()
   {
      return type;
   }

   @Override
   protected void handleException(final Exception exception, Object timer)
   {
      new ContextualAsynchronousRequest(timer)
      {
         @Override
         protected void process()
         {
            AsynchronousExceptionHandler.instance().handleException(exception);
         }
      }.run();
      
   }
   
}