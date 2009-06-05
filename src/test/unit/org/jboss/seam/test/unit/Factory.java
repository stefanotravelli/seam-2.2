package org.jboss.seam.test.unit;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;

@Name("factory")
public class Factory 
{
   @Out(scope=ScopeType.CONVERSATION, required=true) 
   String name;
   
   @org.jboss.seam.annotations.Factory("name")
   public void createName()
   {
      name="Gavin King";
   }
}
