package org.jboss.seam.tool;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Defines a new property name with the value converted to uppercase.
 * 
 * @author Dan Allen
 */
public class UppercasePropertyTask extends Task
{
   private String name;

   private String value;

   @Override
   public void execute() throws BuildException
   {
      if (value != null && !"".equals(value))
      {
         getProject().setProperty(name, upper(value));
      }
   }

   protected String upper(String value)
   {
      return value.toUpperCase();
   }

   public void setValue(String value)
   {
      this.value = value;
   }

   public void setName(String propertyName)
   {
      this.name = propertyName;
   }
}
