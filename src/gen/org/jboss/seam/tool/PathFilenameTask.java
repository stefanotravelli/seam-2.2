package org.jboss.seam.tool;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class PathFilenameTask extends Task
{
   private String path;
   private String propertyName;
   
   @Override
   public void execute() throws BuildException
   {
      if ( path!=null && !"".equals(path) )
      {
         int fwdloc = path.lastIndexOf('/');
         int backloc = path.lastIndexOf('\\');
         int loc = ( fwdloc > backloc ? fwdloc : backloc ) + 1;
         String filename = loc>0 ? path.substring(loc) : path;
         getProject().setProperty(propertyName, filename);
      }
   }

   public void setPath(String packageName)
   {
      this.path = packageName;
   }

   public void setProperty(String propertyName)
   {
      this.propertyName = propertyName;
   }
}
