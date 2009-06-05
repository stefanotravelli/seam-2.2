package org.jboss.seam.drools;

import java.util.List;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * A jBPM ActionHandler that delegates to a Drools WorkingMemory
 * held in a Seam context variable.
 * 
 * @author Jeff Delong
 * @author Gavin King
 *
 */
public class DroolsActionHandler extends DroolsHandler implements ActionHandler
{
   private static final long serialVersionUID = 7752070876220597913L;
   
   public List<String> assertObjects;
   public String workingMemoryName;
   
   public void execute(ExecutionContext executionContext) throws Exception
   {
      getWorkingMemory(workingMemoryName, assertObjects, executionContext).fireAllRules();
   }
   
}