package org.jboss.seam.deployment;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface ClassDeploymentMetadata extends DeploymentMetadata
{
   
   /**
    * An array of class annotations this deployment handler is interested in
    * 
    * All classes with any of these annotations should be considered part of the
    * match
    */
   public Set<Class<? extends Annotation>> getClassAnnotatedWith();

}
