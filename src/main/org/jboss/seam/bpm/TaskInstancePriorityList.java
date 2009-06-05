package org.jboss.seam.bpm;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.Unwrap;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * Support for a task list ordered by priority.
 * 
 * @see TaskInstanceList
 * @see PooledTask
 * @author Gavin King
 */
@Name("org.jboss.seam.bpm.taskInstancePriorityList")
@Scope(APPLICATION)
@Install(precedence=BUILT_IN, dependencies="org.jboss.seam.bpm.jbpm")
public class TaskInstancePriorityList
{
   
   //TODO: we really need to cache the list in the event context,
   //      but then we would need some events to refresh it
   //      when tasks end, which is non-trivial to do....
   
   @Unwrap
   @Transactional
   public List<TaskInstance> getTaskInstanceList()
   {
      return getTaskInstanceList( Actor.instance().getId() );
   }

   private List<TaskInstance> getTaskInstanceList(String actorId)
   {
      if ( actorId == null ) return null;

      return ManagedJbpmContext.instance().getSession()
         .createCriteria(TaskInstance.class)
         .add( Restrictions.eq("actorId", actorId) )
         .add( Restrictions.eq("isOpen", true) )
         .add( Restrictions.ne("isSuspended", true) )
         .addOrder( Order.asc("priority") )
         .setCacheable(true)
         .list();
   }
   
}
