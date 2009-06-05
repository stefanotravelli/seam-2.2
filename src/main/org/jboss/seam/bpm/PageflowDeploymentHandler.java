package org.jboss.seam.bpm;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.jboss.seam.deployment.AbstractDeploymentHandler;
import org.jboss.seam.deployment.DeploymentMetadata;
import org.jboss.seam.deployment.FileDescriptor;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Resources;
import org.jboss.seam.util.XML;


public class PageflowDeploymentHandler extends AbstractDeploymentHandler
{
   
   private static DeploymentMetadata NAMESPACE_METADATA = new DeploymentMetadata()
   {

      public String getFileNameSuffix()
      {
         return ".jpdl.xml";
      }
      
   };

   private static LogProvider log = Logging.getLogProvider(PageflowDeploymentHandler.class);
   
   public static final String NAME = "org.jboss.seam.bpm.PageflowDeploymentHandler";
   
   public String getName()
   {
      return NAME;
   }

   @Override
   public void postProcess(ClassLoader classLoader)
   {
      Set<FileDescriptor> files = new HashSet<FileDescriptor>();
      for (FileDescriptor fileDescriptor : getResources())
      {
         try
         {
            InputStream inputStream = fileDescriptor.getUrl().openStream();
            try 
            {
               
               Element root = XML.getRootElementSafely(inputStream);
               if ("pageflow-definition".equals(root.getName()))
               {
                  files.add(fileDescriptor);
               }
            }
            catch (DocumentException e) 
            {
               log.debug("Unable to parse " + fileDescriptor.getName(), e);
            }
            finally 
            {
               Resources.closeStream(inputStream);
            }
         } catch (IOException e)
         {
            log.trace("Error loading " + fileDescriptor.getName());
         }
         
      }
      setResources(files);
   }
   
   public DeploymentMetadata getMetadata()
   {
      return NAMESPACE_METADATA;
   }

}
