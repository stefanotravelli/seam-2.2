package org.jboss.seam.async;

import org.jboss.seam.Component;
import org.jboss.seam.intercept.InvocationContext;

/**
 * Interface to be implemented by any strategy for dispatching
 * asynchronous method calls and asynchronous events.
 * 
 * @author Gavin King
 *
 * @param <T> the type of the timer object
 */
public interface Dispatcher<T, S extends Schedule>
{
   /**
    * Schedule an asynchronous method call, examining annotations
    * upon the method to determine the schedule
    * 
    * @return some kind of timer object, or null
    */
   public T scheduleInvocation(InvocationContext invocation, Component component);
   /**
    * Schedule a timed (delayed and/or periodic) event
    * 
    * @param type the event type
    * @param schedule the schedule
    * @param parameters parameters to pass to the event listener method
    * @return some kind of timer object, or null
    */
   public T scheduleTimedEvent(String type, S schedule, Object... parameters);
   
   /**
    * Schedule an immediate asynchronous event
    * 
    * @param type the event type
    * @param parameters parameters to pass to the event listener method
    * @return some kind of timer object, or null
    */
   public T scheduleAsynchronousEvent(String type, Object... parameters);
   
   /**
    * Schedule an event to be processed if and when the current transaction 
    * completes successfully
    * 
    * @param type the event type
    * @param parameters parameters to pass to the event listener method
    */
   public void scheduleTransactionSuccessEvent(String type, Object... parameters);
   
   /**
    * Schedule an event to be processed when the current transaction ends
    * 
    * @param type the event type
    * @param parameters parameters to pass to the event listener method
    */
   public void scheduleTransactionCompletionEvent(String type, Object... parameters);
   
}
