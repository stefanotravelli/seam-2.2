package org.jboss.seam.wicket.ioc;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.LoaderClassPath;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

public class WicketInstrumentationTask extends Task
{
   Path buildPath;

   public Path getBuildPath()
   {
      return buildPath;
   }

   public void setBuildPath(Path buildPath)
   {
      this.buildPath = buildPath;
   }

   public void addClasspath(Path path)
   {
      buildPath = path;
   }

   private File outputDirectory;

   public File getOutputDirectory()
   {
      return outputDirectory;
   }

   public void setOutputDirectory(File outputDirectory)
   {
      this.outputDirectory = outputDirectory;
   }

   private FileSet fileset;

   public void addFileset(FileSet fileset)
   {
      this.fileset = fileset;
   }
   
   private boolean useAnnotations = false;

   public boolean isUseAnnotations()
   {
      return useAnnotations;
   }

   public void setUseAnnotations(boolean useAnnotations)
   {
      this.useAnnotations = useAnnotations;
   }

   @Override
   public void execute()
   {
      try
      {

         ClassPool classPool = new ClassPool();
         classPool.insertClassPath(new LoaderClassPath(getProject().createClassLoader(buildPath)));
         
         //List<CtClass> instrumentedClasses = new ArrayList<CtClass>();

         JavassistInstrumentor instrumentor = new JavassistInstrumentor(classPool,useAnnotations);

         Set<String> toInstrument = new HashSet<String>();
         for (String file : fileset.getDirectoryScanner(getProject()).getIncludedFiles())
         {
            if (file.endsWith(".class"))
            {
               toInstrument.add(filenameToClassname(file));
            }
         }
         instrumentor.instrumentClassSet(toInstrument,outputDirectory.getPath());
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   protected static String filenameToClassname(String filename)
   {
      return filename.substring(0, filename.lastIndexOf(".class")).replace('/', '.').replace('\\', '.');
   }
}
