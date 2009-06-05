package org.jboss.seam.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class PrintTask extends Task
{
   private String file;

   public void setFile(String file)
   {
      this.file = file;
   }
   
   @Override
   public void execute() throws BuildException
   {
      try
      {
         BufferedReader reader = new BufferedReader( new FileReader( new File(file) ) );
         while ( reader.ready() )
         {
            System.out.println( reader.readLine() );
         }
      }
      catch (Exception e)
      {
         throw new BuildException(e);
      }
   }
}
