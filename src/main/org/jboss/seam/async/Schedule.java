package org.jboss.seam.async;

import java.io.Serializable;
import java.util.Date;

/**
 * A "schedule" for a timed event executed by
 * a timer service which supports delayed
 * timed events. It is the base class for the more
 * useful TimerSchedule, NthBusinessDay and CronSchedule classes.
 * 
 * @author Michael Yuan
 *
 */
public class Schedule implements Serializable
{
   private Long duration;
   private Date expiration;
   private Date finalExpiration;
   
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

   /**
    * @param duration the delay before the event occurs
    * @param expiration the datetime at which the event occurs
    */
   public Schedule(Long duration, Date expiration)
   {
      this.duration = duration;
      this.expiration = expiration;
   }

   /**
    * @param duration the delay before the event occurs
    * @param expiration the datetime at which the event occurs
    * @param finalExpiration the datetime at which the event ends
    */
   public Schedule(Long duration, Date expiration, Date finalExpiration)
   {
      this.duration = duration;
      this.expiration = expiration;
      this.finalExpiration = finalExpiration;
   }

   /**
    * @param duration the delay before the event occurs
    */
   public Schedule(Long duration)
   {
      this.duration = duration;
   }

   /**
    * @param expiration the datetime at which the event occurs
    */
   public Schedule(Date expiration)
   {
      this.expiration = expiration;
   }

   public Schedule () { }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((duration == null) ? 0 : duration.hashCode());
      result = prime * result + ((expiration == null) ? 0 : expiration.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      final Schedule other = (Schedule) obj;
      if (duration == null)
      {
         if (other.duration != null) return false;
      }
      else if (!duration.equals(other.duration)) return false;
      if (expiration == null)
      {
         if (other.expiration != null) return false;
      }
      else if (!expiration.equals(other.expiration)) return false;
      return true;
   }
   
   
}
