package org.jboss.seam.deployment;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;

/**
 * The {@link ComponentDeploymentHandler} process Seam's component annotated 
 * with {@link org.jboss.seam.annotations.Name} 
 *  
 * @author Pete Muir
 *
 */
public class ComponentDeploymentHandler extends AbstractClassDeploymentHandler
{
   
   private static Set<Class<? extends Annotation>> ANNOTATION_TYPES = new HashSet<Class<? extends Annotation>>(Arrays.asList(Name.class));
   
   public static ClassDeploymentMetadata NAME_ANNOTATED_CLASS_METADATA = new ClassDeploymentMetadata()
   {

      public Set<Class<? extends Annotation>> getClassAnnotatedWith()
      {
         return ANNOTATION_TYPES;
      }

      public String getFileNameSuffix()
      {
         return null;
      }
      
   };
   
   /**
    * Name under which this {@link DeploymentHandler} is registered
    */
   public static final String NAME = "org.jboss.seam.deployment.ComponentDeploymentHandler";
   
   public String getName()
   {
      return NAME;
   }

   public ClassDeploymentMetadata getMetadata()
   {
      return NAME_ANNOTATED_CLASS_METADATA;
   }
   
   @Override
   public void postProcess(ClassLoader classLoader)
   {
      Set<ClassDescriptor> classes = new HashSet<ClassDescriptor>();
      for (ClassDescriptor classDescriptor : getClasses())
      {
         if (classDescriptor.getClazz().isAnnotationPresent(Install.class))
         {
            if (classDescriptor.getClazz().getAnnotation(Install.class).value())
            {
               classes.add(classDescriptor);
            }
         }
         else
         {
            classes.add(classDescriptor);
         }
      }
      setClasses(classes);
   }
   
}
