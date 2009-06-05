package org.jboss.seam.bpm;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.instantiation.UserCodeInterceptor;
import org.jbpm.taskmgmt.def.AssignmentHandler;
import org.jbpm.taskmgmt.def.TaskControllerHandler;
import org.jbpm.taskmgmt.exe.Assignable;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * Intercepts calls to user code coming from jBPM, sets up
 * Seam contexts and associates the process and task instances
 * with the contexts.
 * 
 * @author Gavin King
 *
 */
class SeamUserCodeInterceptor implements UserCodeInterceptor
{
   abstract static class ContextualCall
   {
      abstract void process() throws Exception;
      
      void run() throws Exception
      {
         if ( Contexts.isEventContextActive() || Contexts.isApplicationContextActive() ) //not sure about the second bit (only needed at init time!)
         {
            process();
         }
         else
         {
            Lifecycle.beginCall();
            try
            {
               process();
            }
            finally
            {
               Lifecycle.endCall();
            }
         }
      }
      
      void runAndWrap()
      {
         try
         {
            run();
         }
         catch (RuntimeException re)
         {
            throw re;
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
   }

   public void executeAction(final Action action, final ExecutionContext context) throws Exception
   {
      if ( isPageflow(context) )
      {
         action.execute(context);
      }
      else
      {
         new ContextualCall()
         {
            @Override
            void process() throws Exception
            {
               initProcessAndTask(context);
               action.execute(context);
            }
         }.run();
      }
   }

   private boolean isPageflow(final ExecutionContext context)
   {
      return Contexts.isConversationContextActive() && 
            Jbpm.instance().isPageflowProcessDefinition( context.getProcessDefinition().getName() );
   }

   public void executeAssignment(final AssignmentHandler handler, final Assignable assignable, 
            final ExecutionContext context)
            throws Exception
   {
      new ContextualCall()
      {
         @Override
         void process() throws Exception
         {
            initProcessAndTask(context);
            handler.assign(assignable, context);
         }
      }.run();
   }

   public void executeTaskControllerInitialization(final TaskControllerHandler handler, final TaskInstance task,
            final ContextInstance context, final Token token)
   {
      new ContextualCall()
      {
         @Override
         void process() throws Exception
         {
            initProcessAndTask(task);
            handler.initializeTaskVariables(task, context, token);
         }
      }.runAndWrap();
   }

   public void executeTaskControllerSubmission(final TaskControllerHandler handler, final TaskInstance task,
            final ContextInstance context, final Token token)
   {
      new ContextualCall()
      {
         @Override
         void process() throws Exception
         {
            initProcessAndTask(task);
            handler.submitTaskVariables(task, context, token);
         }
      }.runAndWrap();
   }

   private static void initProcessAndTask(ExecutionContext context)
   {
      BusinessProcess businessProcess = BusinessProcess.instance();
      businessProcess.setProcessId( context.getProcessInstance().getId() );
      TaskInstance taskInstance = context.getTaskInstance();
      if (taskInstance!=null)
      {
         businessProcess.setTaskId( taskInstance.getId() );
      }
   }

   private static void initProcessAndTask(TaskInstance task)
   {
      BusinessProcess businessProcess = BusinessProcess.instance();
      businessProcess.setProcessId( task.getProcessInstance().getId() );
      businessProcess.setTaskId( task.getId() );
   }

}
