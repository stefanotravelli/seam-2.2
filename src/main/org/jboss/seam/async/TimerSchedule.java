package org.jboss.seam.async;

import java.util.Date;

/**
 * A "schedule" for a timed event executed by
 * the EJB timer service or some other timer
 * service which supports delayed and/or periodic
 * timed events.
 * 
 * @author Gavin King
 *
 */
public class TimerSchedule extends Schedule
{
   private Long intervalDuration;
   
   public Long getIntervalDuration()
   {
      return intervalDuration;
   }
   
   /**
    * @param duration the delay before the event occurs
    */
   public TimerSchedule(Long duration)
   {
      super(duration);
   }

   /**
    * @param expiration the datetime at which the event occurs
    */
   public TimerSchedule(Date expiration)
   {
      super(expiration);
   }

   /**
    * @param duration the delay before the first event occurs
    * @param intervalDuration the period between the events
    */
   public TimerSchedule(Long duration, Long intervalDuration)
   {
      super(duration);
      this.intervalDuration = intervalDuration;
   }

   /**
    * @param expiration the datetime at which the first event occurs
    * @param intervalDuration the period between the events
    */
   public TimerSchedule(Date expiration, Long intervalDuration)
   {
      super(expiration);
      this.intervalDuration = intervalDuration;
   }

   public TimerSchedule(Long duration, Date expiration, Long intervalDuration)
   {
      super(duration, expiration);
      this.intervalDuration = intervalDuration;
   }

   public TimerSchedule(Long duration, Date expiration, Long intervalDuration, Date finalExpiration)
   {
      super(duration, expiration, finalExpiration);
      this.intervalDuration = intervalDuration;
   }

   private TimerSchedule() {}
   
   
   
   public static final TimerSchedule ONCE_IMMEDIATELY = new TimerSchedule();

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((intervalDuration == null) ? 0 : intervalDuration.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (!super.equals(obj)) return false;
      final TimerSchedule other = (TimerSchedule) obj;
      if (intervalDuration == null)
      {
         if (other.intervalDuration != null) return false;
      }
      else if (!intervalDuration.equals(other.intervalDuration)) return false;
      return true;
   }
}
