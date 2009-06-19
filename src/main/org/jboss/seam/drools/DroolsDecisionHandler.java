package org.jboss.seam.drools;

import java.util.List;

import org.drools.WorkingMemory;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.node.DecisionHandler;

/**
 * A jBPM DecisionHandler that delegates to a Drools WorkingMemory
 * held in a Seam context variable. The decision outcome is returned
 * by setting the outcome attribute of the global named "decision".
 * 
 * @author Gavin King
 *
 */
public class DroolsDecisionHandler extends DroolsHandler implements DecisionHandler
{
   private static final long serialVersionUID = -8900810376838166513L;
   
   public List<String> assertObjects;
   public List<String> retractObjects;
   public String workingMemoryName;
   public String startProcessId;
   
   public String decide(ExecutionContext executionContext) throws Exception
   {
      WorkingMemory workingMemory = getWorkingMemory(workingMemoryName, assertObjects, retractObjects, executionContext);
      workingMemory.setGlobal( "decision", new Decision() );
      if(startProcessId != null && startProcessId.trim().length() > 0 ) 
      {
         workingMemory.startProcess(startProcessId);
      }
      workingMemory.fireAllRules();
      return ( (Decision) workingMemory.getGlobal("decision") ).getOutcome();
   }
   
}