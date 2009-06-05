package org.jboss.seam.mail.ui;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;

public class UIHeader extends MailComponent
{
   
   private String name;
   private String value;
   
   @Override
   public void encodeBegin(FacesContext facesContext) throws IOException
   {
      try
      {
         if (getValue() != null) 
         {
            Header header = new Header(getName(), getValue());
            findMimeMessage().addHeader(header.getSanitizedName(), header.getSanitizedValue());
         }
         else 
         {
            Header header = new Header(getName(), encode(facesContext));
            findMimeMessage().addHeader(header.getSanitizedName(), header.getSanitizedValue());
         }
      }
      catch (MessagingException e)
      {
         throw new FacesException(e.getMessage(), e);
      }
   }
   
   
   public String getName()
   {
      if (name == null)
      {
         return getString("name");
      }
      else 
      {
         return name;
      }
   }
   
   public void setName(String header)
   {
      this.name = header;
   }
   
   public String getValue()
   {
      if (value == null)
      {
         return getString("value");
      }
      else 
      {
         return value;
      }
   }
   
   public void setValue(String value)
   {
      this.value = value;
   }
}
