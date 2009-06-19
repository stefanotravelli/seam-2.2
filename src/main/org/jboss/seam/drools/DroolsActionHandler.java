package org.jboss.seam.drools;

import java.util.List;

import org.drools.WorkingMemory;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * A jBPM ActionHandler that delegates to a Drools WorkingMemory
 * held in a Seam context variable.
 * 
 * @author Jeff Delong
 * @author Gavin King
 * @author Tihomir Surdilovic
 *
 */
public class DroolsActionHandler extends DroolsHandler implements ActionHandler
{
   private static final long serialVersionUID = 7752070876220597913L;
   
   public List<String> assertObjects;
   public List<String> retractObjects;
   public String workingMemoryName;
   public String startProcessId;
   
   public void execute(ExecutionContext executionContext) throws Exception
   {
      WorkingMemory workingMemory = getWorkingMemory(workingMemoryName, assertObjects, retractObjects, executionContext);
      if(startProcessId != null && startProcessId.trim().length() > 0 ) 
      {
         workingMemory.startProcess(startProcessId);
      }
      workingMemory.fireAllRules();
   }
   
}