package org.jboss.seam.deployment;

import java.util.Set;

/**
 * A deployment handler is responsible for processing found resources
 * 
 * All deployment handlers should specify a unique name under which they
 * will be registered with the {@link DeploymentStrategy}
 * 
 * @author Pete Muir
 *
 */
public interface DeploymentHandler
{
   
   /**
    * A key used to identify the deployment handler
    */
   public String getName();
   
   /**
    * Get DeploymentHandlerMetadata for resources this deployment handler is
    * interested in processing.
    * 
    * If a deployment handler is interested in a number of files it should
    * define multiple pieces of metadata
    */
   public DeploymentMetadata getMetadata();
   
   public void postProcess(ClassLoader classLoader);
  
   public Set<FileDescriptor> getResources();
   
   public void setResources(Set<FileDescriptor> resources);
}
