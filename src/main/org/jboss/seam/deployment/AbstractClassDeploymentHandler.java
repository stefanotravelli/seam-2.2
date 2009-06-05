package org.jboss.seam.deployment;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractClassDeploymentHandler extends AbstractDeploymentHandler
      implements ClassDeploymentHandler
{
   
   private Set<ClassDescriptor> classes;
   
   public AbstractClassDeploymentHandler()
   {
      classes = new HashSet<ClassDescriptor>();
   }
   
   public Set<ClassDescriptor> getClasses()
   {
      return classes;
   }
   
   public void setClasses(Set<ClassDescriptor> classes)
   {
      this.classes = classes;
   }

}
