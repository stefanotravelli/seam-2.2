package org.jboss.seam.deployment;

/**
 * Metadata about resources the deployment handler is interested in
 * 
 * @author pmuir
 *
 */
public interface DeploymentMetadata
{
   
   /**
    * A file name suffixes that this deployment handler is interested in
    */
   public String getFileNameSuffix();

}

