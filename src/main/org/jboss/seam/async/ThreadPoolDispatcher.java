package org.jboss.seam.async;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.intercept.InvocationContext;

/**
 * Dispatcher implementation that uses a java.util.concurrent
 * ScheduledThreadPoolExecutor.
 * 
 * @author Gavin King
 *
 */
@Scope(ScopeType.APPLICATION)
@Name("org.jboss.seam.async.dispatcher")
@Install(precedence=BUILT_IN)
public class ThreadPoolDispatcher extends AbstractDispatcher<Future, TimerSchedule>
{
   private int threadPoolSize = 10; 
   
   private ScheduledExecutorService executor;
   
   @Create
   public void startup() {
       executor = Executors.newScheduledThreadPool(threadPoolSize);
   }
    
   public Future scheduleAsynchronousEvent(String type, Object... parameters)
   {  
      RunnableAsynchronous runnableAsynchronous = new RunnableAsynchronous( new AsynchronousEvent(type, parameters) ); 
      Future future = executor.submit(runnableAsynchronous);
      runnableAsynchronous.setFuture(future);
      return future;
   }
    
   public Future scheduleTimedEvent(String type, TimerSchedule schedule, Object... parameters)
   {
      return scheduleWithExecutorService( schedule, new RunnableAsynchronous( new AsynchronousEvent(type, parameters) ) );
   }
   
   public Future scheduleInvocation(InvocationContext invocation, Component component)
   {
      return scheduleWithExecutorService( 
               createTimerSchedule(invocation), 
               new RunnableAsynchronous( new AsynchronousInvocation(invocation, component) ) 
            );
   }
   
   private static long toDuration(Date expiration)
   {
      return expiration.getTime() - new Date().getTime();
   }
   
   private Future scheduleWithExecutorService(TimerSchedule schedule, RunnableAsynchronous runnable)
   {
      Future future = null;
      if ( schedule.getIntervalDuration()!=null )
      {
         if ( schedule.getExpiration()!=null )
         {
            future = executor.scheduleAtFixedRate( runnable, 
                    toDuration( schedule.getExpiration() ), 
                    schedule.getIntervalDuration(), 
                    TimeUnit.MILLISECONDS );
         }
         else if ( schedule.getDuration()!=null )
         {
            future = executor.scheduleAtFixedRate( runnable, 
                     schedule.getDuration(), 
                     schedule.getIntervalDuration(), 
                     TimeUnit.MILLISECONDS );
         }
         else
         {
            future = executor.scheduleAtFixedRate( runnable, 0l, 
                    schedule.getIntervalDuration(), 
                    TimeUnit.MILLISECONDS );
         }
      }
      else if ( schedule.getExpiration()!=null )
      {
         future = executor.schedule( runnable, 
                  toDuration( schedule.getExpiration() ), 
                  TimeUnit.MILLISECONDS );
      }
      else if ( schedule.getDuration()!=null )
      {
         future = executor.schedule( runnable, 
                  schedule.getDuration(), 
                  TimeUnit.MILLISECONDS );
      }
      else
      {
         future = executor.schedule(runnable, 0l, TimeUnit.MILLISECONDS);
      }
      runnable.setFuture(future);
      return future;
   }
   
   @Destroy
   public void destroy()
   {
      executor.shutdown();
      try
      {
         executor.awaitTermination(5, TimeUnit.SECONDS);
      }
      catch (InterruptedException ie)
      {
         
      }
   }
   
   static class RunnableAsynchronous implements Runnable
   {
      private Asynchronous async;
      
      private Future future;
      
      RunnableAsynchronous(Asynchronous async)
      {
         this.async = async;
      }
      
      public void run()
      {
         try
         {
            async.execute(future);
         }
         catch (Exception exception) 
         {
            async.handleException(exception, future); 
         }
      }
      
      public void setFuture(Future future)
      {
         this.future = future;
      }
      
   }

   public int getThreadPoolSize()
   {
      return threadPoolSize;
   }

   public void setThreadPoolSize(int threadPoolSize)
   {
      this.threadPoolSize = threadPoolSize;
   }
   
}
