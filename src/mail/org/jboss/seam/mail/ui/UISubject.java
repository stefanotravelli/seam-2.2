package org.jboss.seam.mail.ui;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;

import org.jboss.seam.mail.ui.context.MailResponseWriter;

/**
 * JSF component for rendering subject line
 */
public class UISubject extends MailComponent
{
   @Override
   public void encodeChildren(FacesContext facesContext) throws IOException
   {
      try
      {
         String subject = encode(facesContext, MailResponseWriter.TEXT_PLAIN_CONTENT_TYPE);
         String charset = findMessage().getCharset();
         if (charset == null)
         {
            findMimeMessage().setSubject(new Header(subject).getSanitizedValue());
         }
         else
         {
            findMimeMessage().setSubject(new Header(subject).getSanitizedValue(), charset);
         }
      }
      catch (MessagingException e)
      {
         throw new FacesException(e.getMessage(), e);
      }
   }
}
