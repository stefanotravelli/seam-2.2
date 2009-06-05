package org.jboss.seam.navigation;

import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.core.Expressions.ValueExpression;

public class TaskControl
{

   private boolean isBeginTask;

   private boolean isStartTask;

   private boolean isEndTask;

   private ValueExpression<Long> taskId;

   private ValueExpression<String> transition;

   public void beginOrEndTask()
   {
      if ( endTask() )
      {
         BusinessProcess.instance().validateTask();
         BusinessProcess.instance().endTask(transition == null ? null : transition.getValue());
      }
      if ( beginTask() || startTask() )
      {
         if (taskId==null || taskId.getValue() == null)
         {
            throw new NullPointerException("task id may not be null");
         }
         BusinessProcess.instance().resumeTask(taskId.getValue());
      }
      if ( startTask() )
      {
         BusinessProcess.instance().startTask();
      }
   }

   private boolean beginTask()
   {
      return isBeginTask && taskId.getValue() != null;
   }

   private boolean startTask()
   {
      return isStartTask && taskId.getValue() != null;
   }

   private boolean endTask()
   {
      return isEndTask;
   }

   public boolean isBeginTask()
   {
      return isBeginTask;
   }

   public void setBeginTask(boolean isBeginTask)
   {
      this.isBeginTask = isBeginTask;
   }

   public boolean isEndTask()
   {
      return isEndTask;
   }

   public void setEndTask(boolean isEndTask)
   {
      this.isEndTask = isEndTask;
   }

   public boolean isStartTask()
   {
      return isStartTask;
   }

   public void setStartTask(boolean isStartTask)
   {
      this.isStartTask = isStartTask;
   }

   public void setTaskId(ValueExpression<Long> taskId)
   {
      this.taskId = taskId;
   }

   public ValueExpression<Long> getTaskId()
   {
      return taskId;
   }
   
   public ValueExpression<String> getTransition()
   {
      return transition;
   }
   
   public void setTransition(ValueExpression<String> transition)
   {
      this.transition = transition;
   }

}