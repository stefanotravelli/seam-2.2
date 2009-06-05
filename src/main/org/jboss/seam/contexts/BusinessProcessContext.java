/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.contexts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.bpm.ProcessInstance;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Init;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jbpm.context.exe.ContextInstance;

/**
 * Exposes a jbpm variable context instance for reading/writing.
 *
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * @author <a href="mailto:steve@hibernate.org">Steve Ebersole</a>
 * @author Gavin King
 */
public class BusinessProcessContext implements Context 
{

   private static final LogProvider log = Logging.getLogProvider(BusinessProcessContext.class);

   private final Map<String, Object> additions = new HashMap<String, Object>();
   private final Set<String> removals = new HashSet<String>();
   private final boolean enabled;

   public ScopeType getType()
   {
      return ScopeType.BUSINESS_PROCESS;
   }

   public BusinessProcessContext()
   {
      Init init = Init.instance();
      if (init == null)
      {
         enabled = false;
      }
      else
      {
         enabled = init.isJbpmInstalled();
      }
   }
   
   public Object get(String name) 
   {
      
      Object result = additions.get(name);
      if (result!=null) return result;
      if ( removals.contains(name) ) return null;
      
      org.jbpm.taskmgmt.exe.TaskInstance taskInstance = getTaskInstance();
      if (taskInstance==null)
      {
         ContextInstance context = getContextInstance();
         return context==null ? null : context.getVariable(name);
      }
      else
      {
         return taskInstance.getVariable(name);
      }
      
   }

   public void set(String name, Object value) 
   {
      if ( Events.exists() ) Events.instance().raiseEvent("org.jboss.seam.preSetVariable." + name);
      if (value==null)
      {
         //yes, we need this
         remove(name);
      }
      else
      {
         removals.remove(name);
         additions.put(name, value);
      }
      if ( Events.exists() ) Events.instance().raiseEvent("org.jboss.seam.postSetVariable." + name);
   }

   public boolean isSet(String name) 
   {
      return get(name)!=null;
   }
   
   public void remove(String name) 
   {
      if ( Events.exists() ) Events.instance().raiseEvent("org.jboss.seam.preRemoveVariable." + name);
      additions.remove(name);
      removals.add(name);
      if ( Events.exists() ) Events.instance().raiseEvent("org.jboss.seam.postRemoveVariable." + name);
   }

   public String[] getNames() 
   {
      Set<String> results = getNamesFromContext();
      results.removeAll(removals);
      results.addAll( additions.keySet() ); //after, to override
      return results.toArray(new String[]{});
   }

   private Set<String> getNamesFromContext() 
   {
       //TODO: note that this is called from Contexts.destroy(), 
       //      after the Seam-managed txn was committed, but 
       //      this implementation requires a hit to the database!
       HashSet<String> results = new HashSet<String>();
       org.jbpm.taskmgmt.exe.TaskInstance taskInstance = getTaskInstance();
       if (taskInstance==null) {
           ContextInstance context = getContextInstance();
           if (context!=null) {
               Map variables = context.getVariables();
               if (variables != null) {
                   results.addAll(variables.keySet());
               }
           }
       } else {
           results.addAll( taskInstance.getVariables().keySet() );
       }
       return results;
   }

   public Object get(Class clazz)
   {
      return get( Component.getComponentName(clazz) );
   }
   
   public void clear()
   {
      additions.clear();
      removals.addAll( getNamesFromContext() );
   }

   /**
    * Propagate all additions and removals to the jBPM database if
    * there is a current process instance, or do nothing if there
    * is no current process instance.
    */
   public void flush()
   {
      if ( !additions.isEmpty() || !removals.isEmpty() )
      {
         
         org.jbpm.taskmgmt.exe.TaskInstance taskInstance = getTaskInstance();
         if (taskInstance==null)
         {
            org.jbpm.graph.exe.ProcessInstance processInstance = getProcessInstance();
            if ( processInstance==null )
            {
               log.debug( "no process instance to persist business process state" );
               return; //don't clear the additions and removals
            }
            else 
            {
               flushToProcessInstance(processInstance);
            }
         }
         else
         {
            flushToTaskInstance(taskInstance);
         }
         
         additions.clear();
         removals.clear();
         
      }
      
   }

   private void flushToTaskInstance(org.jbpm.taskmgmt.exe.TaskInstance taskInstance)
   {
      log.debug( "flushing to task instance: " + taskInstance.getId() );
      
      for ( Map.Entry<String, Object> entry: additions.entrySet() )
      {
         taskInstance.setVariableLocally( entry.getKey(), entry.getValue() );
      }

      for ( String name: removals )
      {
         taskInstance.deleteVariableLocally(name);
      }
   }

   private void flushToProcessInstance(org.jbpm.graph.exe.ProcessInstance processInstance)
   {
      log.debug( "flushing to process instance: " + processInstance.getId() );
  
      ContextInstance contextInstance = processInstance.getContextInstance();
  
      for ( Map.Entry<String, Object> entry: additions.entrySet() )
      {
         contextInstance.setVariable( entry.getKey(), entry.getValue() );
      }
  
      for ( String name: removals )
      {
         contextInstance.deleteVariable(name);
      }
   }

   private ContextInstance getContextInstance()
   {
      org.jbpm.graph.exe.ProcessInstance processInstance = getProcessInstance();
      return processInstance==null ? null : processInstance.getContextInstance(); 
   }

   private org.jbpm.graph.exe.ProcessInstance getProcessInstance()
   {
      if (!enabled)
      {
         return null;
      }
      else
      {
         return ProcessInstance.instance();
      }
   }
   
   private org.jbpm.taskmgmt.exe.TaskInstance getTaskInstance()
   {
      if (!enabled)
      {
         return null;
      }
      else
      {
         return TaskInstance.instance();
      }
   }
   
}
