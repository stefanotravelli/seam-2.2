package org.jboss.seam.deployment;

import java.io.File;

/**
 * The Scanner is used to find resources to be processed by Seam
 * 
 * The processing is done by {@link DeploymentHandler}s
 * 
 * @author Pete Muir
 *
 */
public interface Scanner
{
   
   /**
    * Recursively scan directories, skipping directories in the exclusion list.
    * 
    * @param directories An array of the roots of the directory trees to scan
    */
   public void scanDirectories(File[] directories);
   
   /**
    * Recursively scan directories, skipping directories in the exclusion list.
    * 
    * @param directories An array of the roots of the directory trees to scan
    * @param excludedDirectories Directories to skip over during the recursive scan
    */
   public void scanDirectories(File[] directories, File[] excludedDirectories);
   
   /**
    * Scan for structures which contain any of the given resources in their root
    * 
    * @param resources The resources to scan for
    */
   public void scanResources(String[] resources);
   
   /**
    * Get the deployment strategy this scanner is used by
    */
   public DeploymentStrategy getDeploymentStrategy();
   
   public long getTimestamp();
   
}
