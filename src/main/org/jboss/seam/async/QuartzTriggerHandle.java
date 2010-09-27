package org.jboss.seam.async;

import java.io.Serializable;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * Provides control over the Quartz Job.
 * 
 * @author Michael Yuan
 *
 */
public class QuartzTriggerHandle implements Serializable
{
   private final String triggerName;
   private String triggerGroupName;
   
   // Hold a transient reference to the scheduler to allow control of the
   // scheduler outside of Seam contexts (useful in a testing context)
   private transient Scheduler scheduler;
     
   public QuartzTriggerHandle(String triggerName) 
   {
      this.triggerName = triggerName; 
   }

   public QuartzTriggerHandle(String triggerName, String triggerGroupName)
   {
      this(triggerName);
      this.triggerGroupName = triggerGroupName; 
   }

   public void cancel() throws SchedulerException
   {
      getScheduler().unscheduleJob(triggerName, triggerGroupName);
   }
   
   public void pause() throws SchedulerException
   {
      getScheduler().pauseTrigger(triggerName, triggerGroupName);  
   }
   
   public Trigger getTrigger() throws SchedulerException
   {
      return getScheduler().getTrigger(triggerName, triggerGroupName);
   }
   
   public void resume() throws SchedulerException
   {
      getScheduler().resumeTrigger(triggerName, triggerGroupName);
   }
   
   private Scheduler getScheduler()
   {
       if (scheduler == null)
       {
           scheduler = QuartzDispatcher.instance().getScheduler();
       }
       return scheduler;
   }
   
}
  
