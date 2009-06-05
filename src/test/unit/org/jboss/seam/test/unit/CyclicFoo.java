package org.jboss.seam.test.unit;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("cyclicFoo")
@Scope(ScopeType.APPLICATION)
public class CyclicFoo
{
   @In String cyclicFooBar; //from CyclicBar#provideCyclicFooBar
   
   public String getName() throws Exception
   {
      return "foo";
   }
   
   public String getFooBar() throws Exception
   {
      return cyclicFooBar;
   }
   
}
