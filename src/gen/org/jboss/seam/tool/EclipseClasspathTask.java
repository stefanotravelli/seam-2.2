package org.jboss.seam.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

/**
 * @author Pete Muir
 * 
 */
public class EclipseClasspathTask extends Task
{
   private String file;
   private String toFile;
   private String filterProperty;

   private List<Path> paths = new ArrayList<Path>();

   @Override
   public void execute() throws BuildException
   {
      Path uberPath = new Path(getProject());
      for (Path path : paths)
      {
         uberPath.add(path);
      }
      String eclipsepaths = "";
      
      for (String path : uberPath.list())
      {
         // avoid placing modules on classpath
         if (!path.contains("jboss-seam"))
         {
            String sourcePath = path.substring(0, path.lastIndexOf(".jar")) + "-sources.jar";
            String javadocPath = path.substring(0, path.lastIndexOf(".jar")) + "-javadoc.jar";
            String eclipsepath = "\t<classpathentry kind=\"lib\" path=\""
                  + path + "\"";
            if (new File(sourcePath).exists())
            {
               eclipsepath += " sourcepath=\"" + sourcePath + "\"";
            }
            
            if (new File(javadocPath).exists())
            {
               eclipsepath += ">\r\n";
               eclipsepath += "\t\t<attributes>\n";
               eclipsepath += "\t\t\t<attribute name=\"javadoc_location\" value=\"jar:file:" + javadocPath + "!/\"/>\n";
               eclipsepath += "\t\t</attributes>\n";
               eclipsepath += "\t</classpathentry>\n";
            }
            else
            {
               eclipsepath += "/>\r\n";
            }
            eclipsepaths += eclipsepath;
         }
      }
      try
      {
         BufferedReader reader = new BufferedReader(new FileReader(new File(
               file)));
         BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
               toFile)));
         while (reader.ready())
         {
            String line = reader.readLine();
            if (line.contains(filterProperty))
            {
               line = line.replace("@" + filterProperty + "@", eclipsepaths);
            }
            writer.write(line + "\r\n");
         }
         writer.flush();
         writer.close();
      } catch (IOException e)
      {
         throw new BuildException(e);
      }
   }

   /**
    * @param file the file to set
    */
   public void setFile(String file)
   {
      this.file = file;
   }

   /**
    * @param toFile the toFile to set
    */
   public void setToFile(String toFile)
   {
      this.toFile = toFile;
   }

   /**
    * @param filterProperty the filterProperty to set
    */
   public void setFilterProperty(String filterProperty)
   {
      this.filterProperty = filterProperty;
   }

   public void addPath(Path path)
   {
      paths.add(path);
   }

}
