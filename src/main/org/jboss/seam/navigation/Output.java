package org.jboss.seam.navigation;

public class Output extends Put
{
   public void out()
   {
      getScope().getContext().set( getName(), getValue().getValue() );
   }
   
}
