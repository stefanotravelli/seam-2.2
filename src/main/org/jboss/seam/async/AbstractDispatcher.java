package org.jboss.seam.async;

import java.lang.annotation.Annotation;
import java.util.Date;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.async.Duration;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.FinalExpiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.transaction.Transaction;

/**
 * Abstract Dispatcher implementation
 * 
 * @author Gavin King
 *
 */
public abstract class AbstractDispatcher<T, S extends Schedule> implements Dispatcher<T, S>
{
   
   public class DispatcherParameters
   {
      private Date expiration;
      private Date finalExpiration;
      private Long duration;
      private Long intervalDuration;
      private String intervalCron;
      
      public String getIntervalCron()
      {
         return intervalCron;
      }
      public Long getDuration()
      {
         return duration;
      }
      public Date getExpiration()
      {
         return expiration;
      }
      public Date getFinalExpiration()
      {
         return finalExpiration;
      }
      public Long getIntervalDuration()
      {
         return intervalDuration;
      }
      public void setIntervalCron(String cron)
      {
         this.intervalCron = cron;
      }
      public void setDuration(Long duration)
      {
         this.duration = duration;
      }
      public void setExpiration(Date expiration)
      {
         this.expiration = expiration;
      }
      public void setFinalExpiration(Date finalExpiration)
      {
         this.finalExpiration = finalExpiration;
      }
      public void setIntervalDuration(Long intervalDuration)
      {
         this.intervalDuration = intervalDuration;
      }
      
      
   }
   
   public static final String EXECUTING_ASYNCHRONOUS_CALL = "org.jboss.seam.core.executingAsynchronousCall";
      
   public static Dispatcher instance()
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("no application context active");
      }
      return (Dispatcher) Component.getInstance("org.jboss.seam.async.dispatcher");         
   }
   
   public void scheduleTransactionSuccessEvent(String type, Object... parameters)
   {
      Transaction.instance().registerSynchronization( new TransactionSuccessEvent(type, parameters) );
   }

   public void scheduleTransactionCompletionEvent(String type, Object... parameters)
   {
      Transaction.instance().registerSynchronization( new TransactionCompletionEvent(type, parameters) );
   }
   
   protected Schedule createSchedule(InvocationContext invocation)
   {
      DispatcherParameters dispatcherParameters = extractAndValidateParameters(invocation);
      if (dispatcherParameters.getIntervalCron() == null)
      {
         return new TimerSchedule(dispatcherParameters.getDuration(), dispatcherParameters.getExpiration(), dispatcherParameters.getIntervalDuration(), dispatcherParameters.getFinalExpiration());
      }
      else
      {
         return new CronSchedule(dispatcherParameters.getDuration(), dispatcherParameters.getExpiration(), dispatcherParameters.getIntervalCron(), dispatcherParameters.getFinalExpiration());
      }
   }
   
   protected TimerSchedule createTimerSchedule(InvocationContext invocation)
   {
      DispatcherParameters dispatcherParameters = extractAndValidateParameters(invocation);
      return createTimerSchedule(dispatcherParameters);
   }
   
   private TimerSchedule createTimerSchedule(DispatcherParameters dispatcherParameters)
   {
      return new TimerSchedule(dispatcherParameters.getDuration(), dispatcherParameters.getExpiration(), dispatcherParameters.getIntervalDuration(), dispatcherParameters.getFinalExpiration());
   }
   
   protected DispatcherParameters extractAndValidateParameters(InvocationContext invocation)
   {
      DispatcherParameters dispatcherParameters = new DispatcherParameters();
      for ( int i=0; i < invocation.getMethod().getParameterAnnotations().length; i++ )
      {
         Annotation[] annotations = invocation.getMethod().getParameterAnnotations()[i];
         for (Annotation annotation: annotations)
         {
            if ( annotation.annotationType().equals(Duration.class) )
            {
               if (invocation.getParameters()[i] instanceof Long)
               {
                  dispatcherParameters.setDuration((Long) invocation.getParameters()[i]);
               }
               else if (invocation.getParameters()[i] != null)
               {
                  throw new IllegalArgumentException("@Duration on " + invocation.getTarget().getClass() + ":" + invocation.getMethod().getName() + " must be a Long");
               }
            }
            else if ( annotation.annotationType().equals(Expiration.class) )
            {
               if (invocation.getParameters()[i] instanceof Date)
               {
                  dispatcherParameters.setExpiration((Date) invocation.getParameters()[i]);
               }
               else if (invocation.getParameters()[i] != null)
               {
                  throw new IllegalArgumentException("@Expiration on " + invocation.getTarget().getClass() + ":" + invocation.getMethod().getName() + " must be a java.util.Date");
               }
            }
            else if ( annotation.annotationType().equals(FinalExpiration.class) )
            {
               if (!( this instanceof QuartzDispatcher ))
               {
                  throw new IllegalArgumentException("Can only use @FinalExpiration with the QuartzDispatcher");
               }
               else if (invocation.getParameters()[i] instanceof Date)
               {
                  dispatcherParameters.setFinalExpiration((Date) invocation.getParameters()[i]);
               }
               else if (invocation.getParameters()[i] != null)
               {
                  throw new IllegalArgumentException("@FinalExpiration on " + invocation.getTarget().getClass() + ":" + invocation.getMethod().getName() + " must be a java.util.Date");
               }
            }
            else if ( annotation.annotationType().equals(IntervalCron.class) )
            {
               if (!( this instanceof QuartzDispatcher ))
               {
                  throw new IllegalArgumentException("Can only use @IntervalCron with the QuartzDispatcher");
               }
               else if (invocation.getParameters()[i] instanceof String)
               {
                  dispatcherParameters.setIntervalCron((String) invocation.getParameters()[i]);
               }
               else if (invocation.getParameters()[i] != null)
               {
                  throw new IllegalArgumentException("@IntervalCron on " + invocation.getTarget().getClass() + ":" + invocation.getMethod().getName() + " must be a String");
               }
            }
            else if ( annotation.annotationType().equals(IntervalDuration.class) )
            {
               if (invocation.getParameters()[i] instanceof Long)
               {
                  dispatcherParameters.setIntervalDuration((Long) invocation.getParameters()[i]);
               }
               else if (invocation.getParameters()[i] != null)
               {
                  throw new IllegalArgumentException("@IntervalDuration on " + invocation.getTarget().getClass() + ":" + invocation.getMethod().getName() + " must be a Long");
               }
            }
         }
      }

      if ( dispatcherParameters.getIntervalCron() != null && dispatcherParameters.getIntervalDuration() != null )
      {
         throw new IllegalArgumentException("Can only use one of @IntervalCron and @IntervalDuration");
      }
      
      return dispatcherParameters;
   }
   
}
