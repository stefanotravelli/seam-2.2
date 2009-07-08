package org.jboss.seam.deployment;

import org.jboss.seam.contexts.Contexts;

/**
 * The {@link PagesDotXmlDeploymentHandler} process pages.xml files
 * Its only purpose is to make sure pages.xml gets updated by hot deploy
 * 
 * @author Stuart Douglas
 * 
 */
public class PagesDotXmlDeploymentHandler extends AbstractDeploymentHandler
{
   
   private static DeploymentMetadata PAGESDOTXML_SUFFIX_FILE_METADATA = new DeploymentMetadata()
   {
      
      public String getFileNameSuffix()
      {
         return "WEB-INF/pages.xml";
      }
      
   };
   
   /**
    * Name under which this {@link DeploymentHandler} is registered
    */
   public static final String NAME = "org.jboss.seam.deployment.PagesDotXmlDeploymentHandler";
   
   public String getName()
   {
      return NAME;
   }
   
   public static PagesDotXmlDeploymentHandler instance()
   {
      if (Contexts.isEventContextActive())
      {
         if (Contexts.getEventContext().isSet(WarRootDeploymentStrategy.NAME))
         {
            DeploymentStrategy deploymentStrategy = (DeploymentStrategy) Contexts.getEventContext().get(WarRootDeploymentStrategy.NAME);
            Object deploymentHandler = deploymentStrategy.getDeploymentHandlers().get(NAME);
            if (deploymentHandler != null)
            {
               return (PagesDotXmlDeploymentHandler) deploymentHandler;
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
      return PAGESDOTXML_SUFFIX_FILE_METADATA;
   }
   
}
