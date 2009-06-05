package org.jboss.seam.deployment;

import org.jboss.seam.contexts.Contexts;

/**
 * The {@link DotPageDotXmlDeploymentHandler} process .page.xml files
 *  
 * @author Pete Muir
 *
 */
public class DotPageDotXmlDeploymentHandler extends AbstractDeploymentHandler
{
   
   private static DeploymentMetadata DOTPAGEDOTXML_SUFFIX_FILE_METADATA = new DeploymentMetadata()
   {

      public String getFileNameSuffix()
      {
         return ".page.xml";
      }
      
   };
   
   /**
    * Name under which this {@link DeploymentHandler} is registered
    */
   public static final String NAME = "org.jboss.seam.deployment.DotPageDotXmlDeploymentHandler";
   
   public String getName()
   {
      return NAME;
   }
   
   public static DotPageDotXmlDeploymentHandler instance()
   {
      if (Contexts.isEventContextActive())
      {
         if (Contexts.getEventContext().isSet(WarRootDeploymentStrategy.NAME))
         {
            DeploymentStrategy deploymentStrategy = (DeploymentStrategy) Contexts.getEventContext().get(WarRootDeploymentStrategy.NAME); 
            Object deploymentHandler = deploymentStrategy.getDeploymentHandlers().get(NAME);
            if (deploymentHandler != null)
            {
               return (DotPageDotXmlDeploymentHandler) deploymentHandler;
            }
         }
         return null;
      }
      else
      {
         throw new IllegalStateException("Event context not active");
      }
   }

   public DeploymentMetadata getMetadata()
   {
      return DOTPAGEDOTXML_SUFFIX_FILE_METADATA;
   }
   
}
