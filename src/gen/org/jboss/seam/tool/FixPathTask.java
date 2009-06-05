package org.jboss.seam.tool;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class FixPathTask extends Task
{   
   private String propertyName;
   
   @Override
   public void execute() throws BuildException
   {
      String path = getProject().getProperty(propertyName);
      
      if ( path!=null )
      {
         getProject().setProperty( propertyName, path.replace('\\', '/') );  
      }      
   }

   public void setProperty(String propertyName)
   {
      this.propertyName = propertyName;
   }    
}
