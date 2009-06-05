package org.jboss.seam.mail.ui;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

/**
 * JSF component for rendering a Reply-to header
 */
public class UIReplyTo extends AddressComponent
{
   
   @Override
   public void encodeBegin(FacesContext facesContext) throws IOException
   {
      try
      {
         MimeMessage mimeMessage = findMimeMessage();
         if (mimeMessage.getReplyTo() != null && mimeMessage.getReplyTo().length > 1) {
            throw new AddressException("Email cannot have more than one Reply-to address", getAddress());
         }
         Address[] replyTo = {getInternetAddress(facesContext)}; 
         mimeMessage.setReplyTo(replyTo);
      }
      catch (AddressException e)
      {
        throw new FacesException(e.getMessage() + " " + "(" + e.getRef() + ")", e);
      }
      catch (MessagingException e)
      {
       throw new FacesException(e.getMessage(), e);
      }
   }
}
