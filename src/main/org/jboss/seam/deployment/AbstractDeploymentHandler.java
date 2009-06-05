package org.jboss.seam.deployment;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class for {@link DeploymentHandler} providing common functionality
 * 
 * @author Pete Muir
 *
 */
public abstract class AbstractDeploymentHandler implements DeploymentHandler
{
   
   private Set<FileDescriptor> resources;
   
   public AbstractDeploymentHandler()
   {
      resources = new HashSet<FileDescriptor>();
   }

   @Override
   public String toString()
   {
      return getName();
   }
   
   public void setResources(Set<FileDescriptor> resources)
   {
      this.resources = resources;
   }
   
   public Set<FileDescriptor> getResources()
   {
      return resources;
   }
   
   public void postProcess(ClassLoader classLoader) {}
   
}
