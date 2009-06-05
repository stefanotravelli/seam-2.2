package org.jboss.seam.tool;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Encode the password provided using the same algorithm that IntelliJ IDEA uses
 * to encode a JDBC database password before storing it in the project file,
 * then assign it to the property.
 */
public class EncodePasswordTask extends Task
{
   private String property;
   private String password;

   @Override
   public void execute() throws BuildException
   {
      getProject().setProperty(property, encode(password));
   }

   protected String encode(String value)
   {
      if (value == null)
      {
         return "";
      }

      String result = "";
      for (int i = 0; i < value.length(); i++)
      {
         int c = value.charAt(i);
         c ^= 0xdfaa;
         result += Integer.toHexString(c);
      }
      
      return result;
   }

   public void setProperty(String property)
   {
      this.property = property;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }
}
