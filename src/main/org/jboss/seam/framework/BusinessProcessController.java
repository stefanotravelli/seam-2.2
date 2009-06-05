package org.jboss.seam.framework;

import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * Superclass for controller objects that control
 * the business process context programmatically.
 * Adds convenience methods for control of the
 * jBPM business process.
 * 
 * @author Gavin King
 *
 */
public class BusinessProcessController extends Controller
{
   protected BusinessProcess getBusinessProcess()
   {
      return BusinessProcess.instance();
   }
   
   protected JbpmContext getJbpmContext()
   {
      return ManagedJbpmContext.instance();
   }
   
   protected TaskInstance getTaskInstance()
   {
      return org.jboss.seam.bpm.TaskInstance.instance();
   }
   
   protected ProcessInstance getProcessInstance()
   {
      return org.jboss.seam.bpm.ProcessInstance.instance();
   }
     
}
