package org.jboss.seam.deployment;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The {@link ComponentsXmlDeploymentHandler} components.xml and .component.xml files 
 * 
 * @author Pete Muir
 *
 */
public class ComponentsXmlDeploymentHandler extends AbstractDeploymentHandler
{
   
   private Pattern INF_PATTERN = Pattern.compile("(WEB-INF/components.xml$)|(META-INF/components.xml$)");
   
   private static DeploymentMetadata COMPONENTSXML_SUFFIX_FILE_METADATA = new DeploymentMetadata()
   {

      public String getFileNameSuffix()
      {
         return "components.xml";
      }
      
   };
   
   /**
    * Name under which this {@link DeploymentHandler} is registered
    */
   public static final String NAME = "org.jboss.seam.deployment.ComponentsXmlDeploymentHandler";
   
   public String getName()
   {
      return NAME;
   }
   
   public DeploymentMetadata getMetadata()
   {
      return COMPONENTSXML_SUFFIX_FILE_METADATA;
   }
   
   @Override
   public void postProcess(ClassLoader classLoader)
   {
      Set<FileDescriptor> resources = new HashSet<FileDescriptor>();
      for (FileDescriptor fileDescriptor : getResources())
      {
         // we want to skip over known meta-directories since Seam will auto-load these without a scan
         String path = fileDescriptor.getName();
         if (!INF_PATTERN.matcher(path).matches()) 
         {
            resources.add(fileDescriptor);
         }
      }
      setResources(resources);
   }
   
}
