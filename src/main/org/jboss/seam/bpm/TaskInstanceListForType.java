package org.jboss.seam.bpm;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.Unwrap;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * Support for a list of tasks of a particular type.
 * 
 * @see TaskInstanceList
 * @author Gavin King
 * @author <a href="mailto:steve@hibernate.org">Steve Ebersole </a>
 */
@Name("org.jboss.seam.bpm.taskInstanceListForType")
@Scope(APPLICATION)
@Install(precedence=BUILT_IN, dependencies="org.jboss.seam.bpm.jbpm")
public class TaskInstanceListForType
{
   
   @Unwrap
   @Transactional
   public Map<String,List<TaskInstance>> getTaskInstanceList()
   {
      return getTaskInstanceList( Actor.instance().getId() );
   }

   private Map<String,List<TaskInstance>> getTaskInstanceList(String actorId)
   {
      if ( actorId == null ) return null;

      Map<String, List<TaskInstance>> map = new HashMap<String, List<TaskInstance>>();
      List<TaskInstance> taskInstances = ManagedJbpmContext.instance().getTaskList(actorId);
      for ( TaskInstance task: taskInstances )
      {
         String name = task.getName();
         List<TaskInstance> list = map.get(name);
         if (list==null)
         {
            list = new ArrayList<TaskInstance>();
            map.put(name, list);
         }
         list.add(task);
      }
      return map;
   }
   
}
