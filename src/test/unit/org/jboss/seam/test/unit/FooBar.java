package org.jboss.seam.test.unit;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("fooBar")
@Scope(ScopeType.APPLICATION)
public class FooBar
{
   @In Foo foo;
   
   public Foo delayedGetFoo(InvocationControl invocationControl)
   {
      //System.out.println("enter: " + invocationControl.getName() + " " + foo);
      invocationControl.init();
      invocationControl.markStarted();
      //System.out.println("exit: " + invocationControl.getName() + " " + foo);
      return foo;
   }
}
