package org.jboss.seam.wicket.ioc;

import static org.jboss.seam.deployment.ClassDescriptor.filenameToClassname;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.Loader;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

public class WicketClassLoader extends Loader
{
   
   private static LogProvider log = Logging.getLogProvider(WicketClassLoader.class);
   
   private Set<String> classes;
   private File wicketComponentDirectory;
   private ClassPool classPool;
   private ClassLoader parentLoader;

   private JavassistInstrumentor instrumentor;


   public WicketClassLoader(ClassLoader parent, ClassPool classPool, File wicketComponentDirectory)
   {
      super(parent, classPool);
      this.classes = new HashSet<String>();
      this.wicketComponentDirectory = wicketComponentDirectory;
      this.classPool = classPool;
      this.parentLoader = parent;
      this.instrumentor = new JavassistInstrumentor(classPool);
   }

   public WicketClassLoader instrument() throws NotFoundException, CannotCompileException, ClassNotFoundException
   {
      classPool.insertClassPath(wicketComponentDirectory.getAbsolutePath());
      classPool.insertClassPath(new LoaderClassPath(parentLoader));

      // Scan for classes
      if (wicketComponentDirectory.exists()) 
      { 
         handleDirectory(wicketComponentDirectory, null, classes);
         instrumentor.instrumentClassSet(classes,null);
      }

      // Ensure classes' static initializers have run, to register the classes
      // with WicketComponent
      for (String className : classes)
      {
         loadClass(className);
      }
      return this;
   }

   /**
    * Recursively collect all class names for class files found in this directory.
    * @param file which directory
    * @param path parent path
    * @param collectedClasses where to store the classes
    * @throws NotFoundException
    * @throws CannotCompileException
    */
   private void handleDirectory(File file, String path, Set<String> collectedClasses) throws NotFoundException, CannotCompileException
   {
      log.trace("directory: " + file);
      for (File child : file.listFiles())
      {
         String newPath = path == null ? child.getName() : path + '/' + child.getName();
         if (child.isDirectory())
         {
            handleDirectory(child, newPath, collectedClasses);
         }
         else
         {
            if (newPath.endsWith(".class"))
            {
               collectedClasses.add(filenameToClassname(newPath));
            }
         }
      }
   }


   @Override
   protected Class loadClassByDelegation(String name) throws ClassNotFoundException
   {
      Class clazz = super.loadClassByDelegation(name);
      if (clazz == null)
      {
         if (!classes.contains(name))
         {
            clazz = delegateToParent(name);
         }
      }
      return clazz;
   }
   
   @Override
   public URL getResource(String name)
   {
      File file = new File(wicketComponentDirectory, name);
      if (file.exists())
      {
         try
         {
            return file.toURL();
         }
         catch (MalformedURLException e)
         {
            throw new RuntimeException(e);
         }
      }
      else
      {
         if (getParent() != null)
         {
            return getParent().getResource(name);
         }
         else
         {
            return null;
         }
      }
   }
}
