package org.jboss.seam.deployment;

/**
 * The {@link DotComponentDotXmlDeploymentHandler} .component.xml files 
 * 
 * @author Pete Muir
 *
 */
public class DotComponentDotXmlDeploymentHandler extends AbstractDeploymentHandler
{
   
   private static DeploymentMetadata DOTCOMPONENTDOTXML_SUFFIX_FILE_METADATA = new DeploymentMetadata()
   {
      
      public String getFileNameSuffix()
      {
         return ".component.xml";
      }
      
   };
   
   /**
    * Name under which this {@link DeploymentHandler} is registered
    */
   public static final String NAME = "org.jboss.seam.deployment.DotComponentDotXmlDeploymentHandler";
   
   public String getName()
   {
      return NAME;
   }
   
   public DeploymentMetadata getMetadata()
   {
      return DOTCOMPONENTDOTXML_SUFFIX_FILE_METADATA;
   }
   
}
