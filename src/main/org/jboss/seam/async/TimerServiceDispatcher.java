package org.jboss.seam.async;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;
import javax.interceptor.Interceptors;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.ejb.SeamInterceptor;
import org.jboss.seam.intercept.InvocationContext;

/**
 * Dispatcher implementation that uses the EJB
 * TimerService.
 * 
 * @author Gavin King
 *
 */
@Stateless
@Name("org.jboss.seam.async.dispatcher")
@Interceptors(SeamInterceptor.class)
@Install(value=false, precedence=BUILT_IN)
public class TimerServiceDispatcher 
   extends AbstractDispatcher<Timer, TimerSchedule>
   implements LocalTimerServiceDispatcher
{
   
   @Resource TimerService timerService;

   @PostConstruct 
   public void postConstruct() {} //workaround for a bug in EJB3
   
   @Timeout
   public void dispatch(Timer timer)
   {
      ( (Asynchronous) timer.getInfo() ).execute(timer);
   }
   
   public Timer scheduleTimedEvent(String type, TimerSchedule schedule, Object... parameters)
   {
      return new TimerProxy( scheduleWithTimerService( schedule, new AsynchronousEvent(type, parameters) ) );
   }
   
   public Timer scheduleAsynchronousEvent(String type, Object... parameters)
   {
      return new TimerProxy( timerService.createTimer( 0l, new AsynchronousEvent(type, parameters) ) );
   }
   
   public Timer scheduleInvocation(InvocationContext invocation, Component component)
   {
      return new TimerProxy( 
              scheduleWithTimerService( createTimerSchedule(invocation), 
              new AsynchronousInvocation(invocation, component) ) );
      
   }

   private Timer scheduleWithTimerService(TimerSchedule schedule, Asynchronous asynchronous)
   {
      if ( schedule.getIntervalDuration()!=null )
      {
         if ( schedule.getExpiration()!=null )
         {
             return timerService.createTimer( schedule.getExpiration(), schedule.getIntervalDuration(), asynchronous );
         }
         else if ( schedule.getDuration()!=null )
         {
             return timerService.createTimer( schedule.getDuration(), schedule.getIntervalDuration(), asynchronous );
         }
         else
         {
            return timerService.createTimer( 0l, schedule.getIntervalDuration(), asynchronous );
         }
      }
      else if ( schedule.getExpiration()!=null )
      {
          return timerService.createTimer( schedule.getExpiration(), asynchronous );
      }
      else if ( schedule.getDuration()!=null )
      {
          return timerService.createTimer( schedule.getDuration(), asynchronous );
      }
      else
      {
         return timerService.createTimer(0l, asynchronous);
      }
   }
   
    static class TimerProxy 
        implements Timer
    {
        Timer timer;

        public TimerProxy(Timer timer)    
            throws  IllegalStateException,
                    NoSuchObjectLocalException,
                    EJBException
        {
            this.timer = timer;
        }
        
        public void cancel() 
            throws
                IllegalStateException,
                NoSuchObjectLocalException,
                EJBException
        {
            instance().call(new Callable() {
                 public Object call() 
                 {
                     timer.cancel();
                     return null;
                 }
             });
        }

        public TimerHandle getHandle()
            throws
                IllegalStateException,
                NoSuchObjectLocalException,
                EJBException
        {
            TimerHandle handle = (TimerHandle) 
                instance().call(new Callable() {
                     public Object call() 
                     {
                         return timer.getHandle();
                     }
                 });
            return new TimerHandleProxy(handle);
        }

        public Serializable getInfo() 
            throws
                IllegalStateException,
                NoSuchObjectLocalException,
                EJBException
        {
            return (Serializable) 
                instance().call(new Callable() {
                     public Object call() 
                     {
                         return timer.getInfo();
                     }
                 });            
        }
        public Date getNextTimeout() 
            throws
                IllegalStateException,
                NoSuchObjectLocalException,
                EJBException
        {
            return (Date) 
                instance().call(new Callable() {
                     public Object call() 
                     {
                         return timer.getNextTimeout();
                     }
                 });            
        }
        
        public long getTimeRemaining()    
            throws IllegalStateException,
                   NoSuchObjectLocalException,
                   EJBException
        {
            return (Long) 
                instance().call(new Callable() {
                     public Object call() 
                     {
                         return timer.getTimeRemaining();
                     }
                 });  
        }
    }

    static class TimerHandleProxy
        implements TimerHandle, 
                   Serializable
    {
        private static final long serialVersionUID = 6913362944260154627L;
      
        TimerHandle handle;

        public TimerHandleProxy(TimerHandle handle) 
        {
            this.handle = handle;
        }
        
        public Timer getTimer() 
            throws IllegalStateException,
                   NoSuchObjectLocalException,
                   EJBException 
        {
            Timer timer = (Timer) instance().call(new Callable() {
                public Object call() 
                {
                    try
                    {
                        return handle.getTimer();
                    }
                    catch (NoSuchObjectLocalException nsoe)
                    {
                        return null;
                    }           
                }
            });
            if (timer==null)
            {
               throw new NoSuchObjectLocalException();
            }
            else
            {
               return new TimerProxy(timer);
            }
        }
    }

    public Object call(Callable task) 
    {
        try 
        {
            return task.call();
        } 
        catch (RuntimeException e) 
        {
            // just pass along runtime exceptions
            throw e;
        } 
        catch (Exception e) 
        {
            throw new RuntimeException(e);
        }
    }
    public static LocalTimerServiceDispatcher instance()
    {
       return ( (LocalTimerServiceDispatcher) AbstractDispatcher.instance() );
    }



}
