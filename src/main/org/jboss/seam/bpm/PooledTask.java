package org.jboss.seam.bpm;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.web.Parameters;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * Support for assigning tasks in the pooled task list.
 * 
 * @see TaskInstanceList
 * @author Gavin King
 */
@Name("org.jboss.seam.bpm.pooledTask")
@Scope(ScopeType.APPLICATION)
@Install(precedence=BUILT_IN, dependencies="org.jboss.seam.bpm.jbpm")
public class PooledTask
{
   
   /**
    * Assign the TaskInstance with the id passed
    * in the request parameter named "taskId" to
    * the current actor.
    * 
    * @see Actor
    * @return a null outcome only if the task was not found
    */
   @Transactional
   public String assignToCurrentActor()
   {
      Actor actor = Actor.instance();
      if ( actor.getId()==null )
      {
         throw new IllegalStateException("no current actor id defined");
      }
      TaskInstance taskInstance = getTaskInstance();
      if (taskInstance!=null)
      {
         taskInstance.setActorId( actor.getId() );
         return "taskAssignedToActor";
      }
      else
      {
         return null;
      }
   }
   
   /**
    * Assign the TaskInstance with the id passed
    * in the request parameter named "taskId" to
    * the given actor id.
    * 
    * @param actorId the jBPM actor id
    * @return a null outcome only if the task was not found
    */
   @Transactional
   public String assign(String actorId)
   {
      TaskInstance taskInstance = getTaskInstance();
      if (taskInstance!=null)
      {
         taskInstance.setActorId(actorId);
         return "taskAssigned";
      }
      else
      {
         return null;
      }
   }
   
   /**
    * Unassign the TaskInstance with the id passed
    * in the request parameter named "taskId" from
    * the actor to which it is assigned, and return
    * it to the pool it came from.
    * 
    * @return a null outcome only if the task was not found
    */
   @Transactional
   public String unassign()
   {
      TaskInstance taskInstance = getTaskInstance();
      if (taskInstance!=null)
      {
         taskInstance.setActorId(null);
         return "taskUnassigned";
      }
      else
      {
         return null;
      }
   }
   
   /**
    * @return the TaskInstance with the id passed
    * in the request parameter named "taskId".
    */
   @Transactional
   public TaskInstance getTaskInstance()
   {
      String[] values = Parameters.instance().getRequestParameters().get("taskId");
      if ( values==null || values.length!=1 ) 
      {
         return null;
      }
      else
      {
         String taskId = values[0];
         return taskId==null ? 
               null : 
               ManagedJbpmContext.instance().getTaskInstanceForUpdate( Long.parseLong(taskId) );
      }
   }
   
}
