package org.jboss.seam.deployment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.seam.annotations.Namespace;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * A deployment handler for namespaces
 * 
 * @author Pete Muir
 *
 */
public class NamespaceDeploymentHandler extends AbstractDeploymentHandler
{
   
   private static DeploymentMetadata NAMESPACE_METADATA = new DeploymentMetadata()
   {

      public String getFileNameSuffix()
      {
         return "/package-info.class";
      }
      
   };
   
   public static final String NAME = "org.jboss.seam.deployment.NamespaceDeploymentHandler";
   
   private static final LogProvider log = Logging.getLogProvider(NamespaceDeploymentHandler.class);
   
   private Set<Package> packages;

   public NamespaceDeploymentHandler()
   {
      packages = new HashSet<Package>();
   }
   
   /**
    * Returns packages with @Namespace declarations
    */
   public Set<Package> getPackages()
   {
       return Collections.unmodifiableSet(packages);
   }
   
   @Override
   public void postProcess(ClassLoader classLoader)
   {
      for (FileDescriptor fileDescriptor : getResources())
      {
         String packageName = filenameToPackageName(fileDescriptor.getName());
         Package pkg = getPackage(packageName, classLoader);
         if (pkg == null) 
         {
             log.warn("Cannot load package info for " + packageName);
         } 
         else 
         {
             if (pkg.getAnnotation(Namespace.class) != null) 
             {
                 packages.add(pkg);
             }
         }
      }
      
      
   }
   
   private static String filenameToPackageName(String filename)
   {
      return filename.substring(0, filename.lastIndexOf("/package-info.class"))
         .replace('/', '.').replace('\\', '.');
   }
   
   private static Package getPackage(String name, ClassLoader classLoader) 
   {
       try 
       {
           Class c = classLoader.loadClass(name + ".package-info");
           return c != null ? c.getPackage() : null;
       } 
       catch (Exception e) 
       {
           return null;
       }
   }
   
   public String getName()
   {
      return NAME;
   }
   
   public DeploymentMetadata getMetadata()
   {
      return NAMESPACE_METADATA;
   }

}
