package org.jboss.seam.mail.ui;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

/**
 * Encode a recipient.  Work is done here, subclasses simply need to
 * specify a RecipientType 
 */
public abstract class RecipientAddressComponent extends AddressComponent
{
   
   @Override
   public void encodeBegin(FacesContext facesContext) throws IOException
   {
      try
      {
         MimeMessage mimeMessage = findMimeMessage();
         mimeMessage.addRecipient(getRecipientType(), getInternetAddress(facesContext));
      }
      catch (AddressException e)
      {
         throw new FacesException(e.getMessage() + " (" + e.getRef() +")", e);
      }
      catch (MessagingException e)
      {
         throw new FacesException(e.getMessage(), e);
      }
   }
  
   protected abstract RecipientType getRecipientType();
   
   

}
