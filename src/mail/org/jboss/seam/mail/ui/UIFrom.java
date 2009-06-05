package org.jboss.seam.mail.ui;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

/**
 * JSF Component for rendering a from address
 */
public class UIFrom extends AddressComponent
{
   
   @Override
   public void encodeBegin(FacesContext facesContext) throws IOException
   {
      try
      {
         
         MimeMessage mimeMessage = findMimeMessage();
        if (mimeMessage.getFrom() != null && mimeMessage.getFrom().length > 0) {
           throw new AddressException("Email cannot have more than one from address", getAddress());
        }
         mimeMessage.setFrom(getInternetAddress(facesContext));
      }
      catch (AddressException e)
      {
        throw new FacesException(e.getMessage() +"(" + e.getRef() + ")", e);
      }
      catch (MessagingException e)
      {
       throw new FacesException(e.getMessage(), e);
      }
   }
}
