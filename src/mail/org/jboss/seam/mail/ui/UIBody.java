package org.jboss.seam.mail.ui;

import java.io.IOException;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.jboss.seam.mail.ui.context.MailResponseWriter;

/**
 * JSF component for rendering the body Supports plain text, html bodies and
 * setting an alternative (text) part using an alternative facet
 * 
 */
public class UIBody extends MailComponent
{
   
   public static final String HTML = "html";
   
   public static final String PLAIN = "plain";
   
   private String type = HTML;
   
   @Override
   public void encodeChildren(FacesContext facesContext) throws IOException
   {
      try
      {
         BodyPart bodyPart = new MimeBodyPart();
         
         MimeMultipart bodyMultipart;         
         
         if (containsInlineAttachment(findMessage().getAttachments()))
         {
            bodyMultipart = new MimeMultipart("related");
         }
         else
         {
            bodyMultipart = null;
         }
         
         if (PLAIN.equalsIgnoreCase(getType()))
         {
            String body = encode(facesContext, MailResponseWriter.TEXT_PLAIN_CONTENT_TYPE);
            bodyPart = getTextBody(facesContext, body);
         }
         else if (HTML.equals(getType()))
         {
            UIComponent alternative = getFacet("alternative");
            String body = encode(facesContext, MailResponseWriter.HTML_PLAIN_CONTENT_TYPE);
            
            if (alternative != null)
            {
               MimeMultipart multipartAlt = new MimeMultipart("alternative");
               multipartAlt.addBodyPart(getTextBody(facesContext, encode(facesContext,
               	alternative, MailResponseWriter.TEXT_PLAIN_CONTENT_TYPE)));
               multipartAlt.addBodyPart(getHtmlBody(facesContext, body));
               bodyPart = new MimeBodyPart();
               bodyPart.setContent(multipartAlt);
            }
            else
            {
               bodyPart = getHtmlBody(facesContext, body);
            }
         }
         
         if (findMessage().getAttachments().size() > 0)
         {
            for (MimeBodyPart attachment : findMessage().getAttachments())
            {
               if (attachment.getDisposition().equalsIgnoreCase("inline"))
               {
                  bodyMultipart.addBodyPart(attachment);
               }
               else
               {
                  getRootMultipart().addBodyPart(attachment);
               }
            }
         }
         if (bodyMultipart != null)
         {
            bodyMultipart.addBodyPart(bodyPart, 0);            
            BodyPart bodyPartContent = new MimeBodyPart();
            bodyPartContent.setContent(bodyMultipart);
            getRootMultipart().addBodyPart(bodyPartContent, 0);
         }
         else
         {
            getRootMultipart().addBodyPart(bodyPart, 0);
         }
         
      }
      catch (MessagingException e)
      {
         throw new FacesException(e.getMessage(), e);
      }
   }
   
   public void setType(String type)
   {
      this.type = type;
   }
   
   /**
    * The type of the body - plain or html
    */
   public String getType()
   {
      if (type == null)
      {
         return getString("type");
      }
      return type;
   }
   
   private BodyPart getTextBody(FacesContext facesContext, String body) throws MessagingException
   {
      MimeBodyPart bodyPart = new MimeBodyPart();
      bodyPart.setDisposition(new Header("inline").getSanitizedValue());
      String charset = findMessage().getCharset();
      if (charset != null)
      {
         // bodyPart.setContent(body, "text/plain; charset="
         // + charset + "; format=flowed");
         bodyPart.setText(body, new Header(charset).getSanitizedValue());
      }
      else
      {
         bodyPart.setText(body);
      }
      return bodyPart;
   }
   
   private BodyPart getHtmlBody(FacesContext facesContext, Object body) throws MessagingException
   {
      MimeBodyPart bodyPart = new MimeBodyPart();
      bodyPart.setDisposition(new Header("inline").getSanitizedValue());
      String charset = findMessage().getCharset();
      if (charset != null)
      {
         bodyPart.setContent(body, new Header("text/html; charset=" + charset).getSanitizedValue());
      }
      else
      {
         bodyPart.setContent(body, new Header("text/html").getSanitizedValue());
      }
      
      return bodyPart;
   }
   
   private static boolean containsInlineAttachment(List<MimeBodyPart> attachments) throws MessagingException
   {
      for (MimeBodyPart attachment : attachments)
      {
         if (attachment.getDisposition().equalsIgnoreCase("inline"))
         {
            return true;
         }
      }
      
      return false;
   }
}