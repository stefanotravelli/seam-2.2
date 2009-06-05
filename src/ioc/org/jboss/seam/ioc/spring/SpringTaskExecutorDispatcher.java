package org.jboss.seam.ioc.spring;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.async.AbstractDispatcher;
import org.jboss.seam.async.Asynchronous;
import org.jboss.seam.async.AsynchronousEvent;
import org.jboss.seam.async.AsynchronousInvocation;
import org.jboss.seam.async.Dispatcher;
import org.jboss.seam.async.Schedule;
import org.jboss.seam.async.TimerSchedule;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.intercept.InvocationContext;
import org.springframework.core.task.TaskExecutor;

/**
 * Dispatcher that can utilizes SpringTaskExecutors for non scheduled
 * asynchronous events but defer to another ScheduledDispatcher for Scheduled
 * asynchronous events.
 * 
 * @author Mike Youngstrom
 */
@Scope(ScopeType.APPLICATION)
@Name("org.jboss.seam.async.dispatcher")
@Install(value=false, precedence=BUILT_IN)
public class SpringTaskExecutorDispatcher<T, S extends Schedule> extends AbstractDispatcher<T, S>
{

   private ValueExpression<Dispatcher<T, S>> scheduleDispatcher;
   private ValueExpression<TaskExecutor> taskExecutor;
   
   public T scheduleAsynchronousEvent(String type, Object... parameters)
   {
      taskExecutor.getValue().execute(
               new RunnableAsynchronous(new AsynchronousEvent(type, parameters)));
      return null;
   }

   public T scheduleInvocation(InvocationContext invocation, Component component)
   {
      Schedule schedule = createSchedule(invocation);
      if (!TimerSchedule.ONCE_IMMEDIATELY.equals(schedule))
      {
         return getScheduleDispatcher().scheduleInvocation(invocation, component);
      }
      taskExecutor.getValue().execute(
               new RunnableAsynchronous(new AsynchronousInvocation(invocation, component)));
      return null;
   }

   public T scheduleTimedEvent(String type, S schedule, Object... parameters)
   {
      return getScheduleDispatcher().scheduleTimedEvent(type, schedule, parameters);
   }

   protected Dispatcher<T, S> getScheduleDispatcher()
   {
      Dispatcher<T, S> dispatcher = (scheduleDispatcher==null) ? null : scheduleDispatcher.getValue();
      if (dispatcher == null) {
         throw new IllegalStateException(
                  "SpringTaskExecutorDispatcher does not support scheduled Events.  Provide a fallback scheduleDispatcher for timed events.");
      }
      return dispatcher;
   }

   /**
    * The dispatcher to handle scheduled events
    * 
    * @param scheduleDispatcher
    */
   public void setScheduleDispatcher(ValueExpression<Dispatcher<T, S>> scheduleDispatcher)
   {
      this.scheduleDispatcher = scheduleDispatcher;
   }

   /**
    * The Spring TaskExecutor to handle immediate asynchronous events
    * 
    * @param taskExecutor
    */
   public void setTaskExecutor(ValueExpression<TaskExecutor> taskExecutor)
   {
      this.taskExecutor = taskExecutor;
   }

   /**
    * Same as the one in ThreadPoolDispatcher. Perhaps area for reuse?
    */
   static class RunnableAsynchronous implements Runnable
   {
      private Asynchronous async;

      RunnableAsynchronous(Asynchronous async)
      {
         this.async = async;
      }

      public void run()
      {
         async.execute(null);
      }
   }
}
