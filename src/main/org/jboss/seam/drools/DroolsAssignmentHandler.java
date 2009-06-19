package org.jboss.seam.drools;

import java.util.List;

import org.drools.WorkingMemory;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.def.AssignmentHandler;
import org.jbpm.taskmgmt.exe.Assignable;

/**
 * A jBPM AssignmentHandler that delegates to a Drools WorkingMemory
 * held in a Seam context variable.
 * 
 * @author Jeff Delong
 * @author Gavin King
 *
 */
public class DroolsAssignmentHandler extends DroolsHandler implements AssignmentHandler
{
   private static final long serialVersionUID = -7114640047036854546L;
   
   public String workingMemoryName;
   public List<String> assertObjects;
   public List<String> retractObjects;
   public String startProcessId;
   
   public void assign(Assignable assignable, ExecutionContext executionContext) throws Exception
   {
      WorkingMemory workingMemory = getWorkingMemory(workingMemoryName, assertObjects, retractObjects, executionContext);
      workingMemory.setGlobal( "assignable", assignable );
      if(startProcessId != null && startProcessId.trim().length() > 0 ) 
      {
         workingMemory.startProcess(startProcessId);
      }
      workingMemory.fireAllRules();
   }
   
}