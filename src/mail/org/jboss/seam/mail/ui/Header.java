package org.jboss.seam.mail.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.mail.internet.MimeUtility;

public class Header
{
   
   private final String name;
   private final String value;
   
   private String sanitizedName;
   private String sanitizedValue;
   
   public Header(String name, String value)
   {
      this.name = name;
      this.value = value;
   }
   
   public Header(String value)
   {
      this.value = value;
      this.name = null;
   }
   
   public String getSanitizedName()
   {
      if (sanitizedName == null && name != null)
      {
         try
         {
            sanitizedName = sanitize(name);
         }
         catch (IOException e)
         {
            throw new IllegalStateException("Error santizing Header name " + name, e);
         }
      }
      return sanitizedName;
   }
   
   public String getSanitizedValue()
   {
      if (sanitizedValue == null && value != null)
      {
         try
         {
            sanitizedValue = sanitizeValue(value);
         }
         catch (IOException e)
         {
            throw new IllegalStateException("Error santizing Header " + name + " value " + value, e);
         }
      }
      return sanitizedValue;
   }
   
   /**
    * Remove any line feed/new line characters
    * @throws IOException 
    */
   public static String sanitize(String value) throws IOException
   {
      BufferedReader  reader = new BufferedReader(new StringReader(value));
      return reader.readLine();
   }
   
   /**
    * Remove any line feed/new line characters from a (possibly) folded header
    * @throws IOException 
    */
   public static String sanitizeValue(String value) throws IOException
   {
      // The user might have folded the header (stupid SMTP idiocy)
      return sanitize(MimeUtility.unfold(value));
   }

}
