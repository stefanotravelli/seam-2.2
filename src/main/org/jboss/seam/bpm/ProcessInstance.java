/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.bpm;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.util.Work;

/**
 * A Seam component that allows injection of the current
 * jBPM ProcessInstance.
 * 
 * @author Gavin King
 */
@Scope(ScopeType.STATELESS)
@Name("org.jboss.seam.bpm.processInstance")
@BypassInterceptors
@Install(precedence=BUILT_IN, dependencies="org.jboss.seam.bpm.jbpm")
public class ProcessInstance 
{
   
   @Unwrap
   public org.jbpm.graph.exe.ProcessInstance getProcessInstance() throws Exception
   {
      if ( !Contexts.isConversationContextActive() ) return null;
      
      return new Work<org.jbpm.graph.exe.ProcessInstance>()
      {
         
         @Override
         protected org.jbpm.graph.exe.ProcessInstance work() throws Exception
         {         
            Long processId = BusinessProcess.instance().getProcessId();
            if (processId!=null)
            {
               //TODO: do we need to cache this??
               //return ManagedJbpmContext.instance().getProcessInstanceForUpdate(processId);
               //JBSEAM-4629:
               return ManagedJbpmContext.instance().getProcessInstance(processId); 
            }
            else
            {
               return null;
            }
         }
         
      }.workInTransaction();
      
   }
   
   public static org.jbpm.graph.exe.ProcessInstance instance()
   {
      if ( !Contexts.isConversationContextActive() || !BusinessProcess.instance().hasCurrentProcess() ) return null; //so we don't start a txn
      
      return (org.jbpm.graph.exe.ProcessInstance) Component.getInstance(ProcessInstance.class, ScopeType.STATELESS);
   }
}
