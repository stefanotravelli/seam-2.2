package org.jboss.seam.async;

import java.util.Date;

/**
 * A "cron schedule" for a timed event executed by
 * the Quartz CronTrigger.
 * 
 * @author Michael Yuan
 *
 */
public class CronSchedule extends Schedule
{
   private String cron;
   
   String getCron()
   {
      return cron;
   }
   
   /**
    * @param duration the delay before the first event occurs
    * @param cron the unix cron string to control how the events are repeated
    */
   public CronSchedule(Long duration, String cron)
   {
      super(duration);
      this.cron = cron;
   }

   /**
    * @param expiration the datetime at which the first event occurs
    * @param cron the unix cron string to control how the events are repeated
    */
   public CronSchedule(Date expiration, String cron)
   {
      super(expiration);
      this.cron = cron;
   }

   CronSchedule(Long duration, Date expiration, String cron, Date finalExpiration)
   {
      super(duration, expiration, finalExpiration);
      this.cron = cron;
   }
   
}
