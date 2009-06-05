package org.jboss.seam.bpm;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.List;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.Unwrap;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * Support for the task list.
 * 
 * @see TaskInstanceListForType
 * @see PooledTask
 * @author <a href="mailto:steve@hibernate.org">Steve Ebersole</a>
 * @author Gavin King
 */
@Name("org.jboss.seam.bpm.taskInstanceList")
@Scope(APPLICATION)
@Install(precedence=BUILT_IN, dependencies="org.jboss.seam.bpm.jbpm")
public class TaskInstanceList
{
   
   @Unwrap
   @Transactional
   public List<TaskInstance> getTaskInstanceList()
   {
      return getTaskInstanceList( Actor.instance().getId() );
   }

   private List<TaskInstance> getTaskInstanceList(String actorId)
   {
      if ( actorId == null ) return null;

      return ManagedJbpmContext.instance().getTaskList(actorId);
   }
   
}
