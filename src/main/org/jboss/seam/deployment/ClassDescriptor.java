package org.jboss.seam.deployment;

import java.net.URL;

import javax.servlet.ServletContext;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

public class ClassDescriptor extends FileDescriptor
{
   
   private static LogProvider log = Logging.getLogProvider(ClassDescriptor.class);
   
   private Class<?> clazz;
   
   public ClassDescriptor(String name, URL url, Class<?> clazz)
   {
      super(name, url);
      this.clazz = clazz;
   }
   
   public ClassDescriptor(String name, ClassLoader classLoader,ServletContext servletContext)
   {
      super(name, classLoader,servletContext);
      String classname = filenameToClassname(name);
      log.trace("Trying to load class " + classname);
      try 
      {
         clazz = classLoader.loadClass(classname);
         // IBM JVM will throw a TypeNotPresentException if any annotation on the class is not on
         // the classpath, rendering the class virtually unusable (given Seam's heavy use of annotations)
         clazz.getAnnotations();
      }
      catch (ClassNotFoundException cnfe) 
      {
         log.debug("could not load class: " + classname, cnfe);
      }
      catch (NoClassDefFoundError ncdfe) 
      {
         log.debug("could not load class (missing dependency): " + classname, ncdfe);
      }
      catch (TypeNotPresentException tnpe)
      {
         clazz = null;
         log.debug("could not load class (annotation missing dependency): " + classname, tnpe);
      }
   }

   public Class<?> getClazz()
   {
      return clazz;
   }
   
   @Override
   public String toString()
   {
      return clazz.getName();
   }
   
   /**
    * Convert a path to a class file to a class name
    */
   public static String filenameToClassname(String filename)
   {
      return filename.substring( 0, filename.lastIndexOf(".class") )
            .replace('/', '.').replace('\\', '.');
   }
   
   @Override
   public boolean equals(Object other)
   {
      if (other instanceof ClassDescriptor)
      {
         ClassDescriptor that = (ClassDescriptor) other;
         return this.getClazz().equals(that.getClazz());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      return getClazz().hashCode();
   }

}
