package org.jboss.seam.deployment;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.codehaus.groovy.control.CompilerConfiguration;


/**
 * A deployment strategy for hot deploying Seam groovy components
 * 
 * @author Pete Muir
 *
 */
public class GroovyHotDeploymentStrategy extends HotDeploymentStrategy
{
   
   private static final String DEFAULT_SCRIPT_EXTENSION = new CompilerConfiguration().getDefaultScriptExtension();
   
   private ClassLoader classLoader;
   
   private GroovyDeploymentHandler groovyDeploymentHandler;
   
   /**
    * @param classLoader The parent classloader of the hot deployment classloader
    * @param hotDeployDirectory The directory in which hot deployable java and 
    * groovy Seam components are placed
    * 
    */
   public GroovyHotDeploymentStrategy(ClassLoader classLoader, File hotDeployDirectory, ServletContext servletContext, boolean enabled)
   {
      super(classLoader, hotDeployDirectory,servletContext, enabled);
      if (enabled)
      {
         groovyDeploymentHandler = new GroovyDeploymentHandler(DEFAULT_SCRIPT_EXTENSION);
         getDeploymentHandlers().put(GroovyDeploymentHandler.NAME, groovyDeploymentHandler);
      }
   }
   
   @Override
   public ClassLoader getClassLoader()
   {
      if (classLoader == null && super.getClassLoader() != null)
      {
         this.classLoader = new GroovyClassLoader(super.getClassLoader());
      }
      return classLoader;
   }

   @Override
   public boolean isFromHotDeployClassLoader(Class componentClass)
   {
      //loaded by groovy or java
      if (getClassLoader() == null)
      {
         return false;
      }
      else
      {
         if (super.isFromHotDeployClassLoader(componentClass)) return true; //Java
         ClassLoader classClassLoader = componentClass.getClassLoader().getParent(); //Groovy use an Inner Delegate CL
         return classClassLoader == getClassLoader() || classClassLoader == getClassLoader().getParent();
      }
   }
   
   @Override
   public Set<ClassDescriptor> getScannedComponentClasses()
   {
      Set<ClassDescriptor> set = new HashSet<ClassDescriptor>();
      set.addAll(super.getScannedComponentClasses());
      set.addAll(groovyDeploymentHandler.getClasses());
      return Collections.unmodifiableSet(set);
   }
   
}
