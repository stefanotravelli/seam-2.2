package org.jboss.seam.test.integration.bpm;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.bpm.CreateProcess;

@Name("seamExpressionEvaluatorTestController")
public class SeamExpressionEvaluatorTestController {
       
   private String name = "foo";
   
   @Factory(value="testBoolean", scope=ScopeType.BUSINESS_PROCESS)
   public Boolean testNameFactory()
   {
      return false;
   }
   
   @CreateProcess(definition="TestProcess2") 
   public void createProcess2() 
   {            
   }
   
   @CreateProcess(definition="TestProcess3") 
   public void createProcess3() 
   {            
   }
   
   @CreateProcess(definition="TestProcess4") 
   public void createProcess4() 
   {
   }
   
   public void logTrue()
   {
      System.out.println("true");
   }
   
   public String getName()
   {
      return this.name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
    
    
}