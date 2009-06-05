package org.jboss.seam.test.unit;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Factory;

@Name("cyclicBar")
@Scope(ScopeType.APPLICATION)
public class CyclicBar
{

   @In CyclicFoo cyclicFoo;
   
   @Factory(value = "cyclicFooBar", autoCreate = true)
   public String provideCyclicFooBar() throws Exception
   {
      return cyclicFoo.getName() + "bar";
   }

}
