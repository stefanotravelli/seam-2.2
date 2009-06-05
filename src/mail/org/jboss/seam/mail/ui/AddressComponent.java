package org.jboss.seam.mail.ui;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public abstract class AddressComponent extends MailComponent
{

   private String name;
   private String address;

   public AddressComponent()
   {
      super();
   }

   /**
    * get an InternetAddress object based upon name, address
    * @throws IOException 
    * @throws AddressException 
    */
   protected InternetAddress getInternetAddress(FacesContext facesContext) throws IOException, AddressException 
   {
      InternetAddress address = new InternetAddress();
      address.setAddress(new Header(getAddress() != null ? getAddress() : encode(facesContext)).getSanitizedValue());
      String charset = findMessage().getCharset();
      String personal = new Header(getName()).getSanitizedValue();
      if (charset == null)
      {
         address.setPersonal(personal);
      }
      else
      {
         address.setPersonal(personal, charset);
      }
      address.validate();
      return address;
   }

   @Override
   public void encodeChildren(FacesContext arg0) throws IOException
   {
     // Disable encoding of children
   }

   /**
    * the email address part of the address
    */
   public String getAddress()
   {
      if (address == null)
      {
         return getString("address");
      }
      else
      {
         return address;
      }
   }

   public void setAddress(String address)
   {
      this.address = address;
   }

   /**
    *  the name part of the address
    */
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

   public void setName(String name)
   {
      this.name = name;
   }

}