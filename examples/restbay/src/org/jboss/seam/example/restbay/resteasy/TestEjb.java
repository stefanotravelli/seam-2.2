package org.jboss.seam.example.restbay.resteasy;

import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Stateful
@Scope(ScopeType.EVENT)
@Name("securedEjb")
public class TestEjb implements TestEjbLocal
{
   
   public boolean foo()
   {
      return true;
   }
   
   @Remove
   public void remove() {}
}
