package org.jboss.seam.flex;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

@Name("org.jboss.seam.flex.remote")
@Startup
@Install(false)
@Scope(ScopeType.APPLICATION)
public class Remote 
{
   String destinationName;
   String componentName;
   
   public void setDestinationName(String destionationName) {
      this.destinationName = destionationName;
   }
   
   public String getDestinationName() {
      return destinationName;
   }
   
   public void setComponentName(String componentName) {
      this.componentName = componentName;
   }
   
   public String getComponentName() {
      return componentName;
   }
   
   
   @Create
   public void init() {
      //System.out.println("** remoting destination " + destinationName + " for " + componentName);
   }

}
