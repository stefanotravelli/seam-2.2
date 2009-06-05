package org.jboss.seam.drools;

import java.util.List;

import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.jboss.seam.Component;
import org.jboss.seam.bpm.Actor;
import org.jboss.seam.core.Expressions;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.jpdl.el.ELException;

/**
 * Common functionality for jBPM handlers for Drools.
 * 
 * @author Jeff Delong
 * @author Gavin King
 *
 */
public class DroolsHandler
{
   protected WorkingMemory getWorkingMemory(String workingMemoryName, List<String> expressions, ExecutionContext executionContext) 
         throws ELException
   {
      WorkingMemory workingMemory = (WorkingMemory) Component.getInstance(workingMemoryName, true);
      
      for (String objectName: expressions)
      {
         Object object = Expressions.instance().createValueExpression(objectName).getValue();
         //Object object = new SeamVariableResolver().resolveVariable(objectName);
         // assert the object into the rules engine
         if (object instanceof Iterable)
         {
            for (Object element: (Iterable) object)
            {
               assertObject(workingMemory, element);
            }
         }
         else
         {
            assertObject(workingMemory, object);
         }
      }
      
      //workingMemory.setGlobal( "contextInstance", executionContext.getContextInstance() );
      workingMemory.insert(Actor.instance());

      return workingMemory;
   }

   private void assertObject(WorkingMemory workingMemory, Object element)
   {
      FactHandle fact = workingMemory.getFactHandle(element);
      if (fact==null)
      {
         workingMemory.insert(element);
      }
      else
      {
         workingMemory.update(fact, element);
      }
   }
}
