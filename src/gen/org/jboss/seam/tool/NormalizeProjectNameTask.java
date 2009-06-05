package org.jboss.seam.tool;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class NormalizeProjectNameTask extends Task
{
   private String propertyName;
   
   @Override
   public void execute() throws BuildException
   {
      String projectName = getProject().getProperty(propertyName);
      
      if ( projectName!=null && projectName.length() > 0 )
      {
         getProject().setProperty( propertyName, normalize(projectName) );
      }
   }

   protected String normalize(String value)
   {
      return value.trim().replaceAll("[ -]", "_").replaceAll("_+", "_");
   }

   public void setProperty(String propertyName)
   {
      this.propertyName = propertyName;
   }
}
