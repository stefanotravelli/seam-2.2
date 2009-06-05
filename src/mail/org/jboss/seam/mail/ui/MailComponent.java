package org.jboss.seam.mail.ui;

import java.io.IOException;
import java.io.StringWriter;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.jboss.seam.mail.ui.context.MailResponseWriter;
import org.jboss.seam.ui.util.JSF;

/**
 * Abstract base class for mail ui components
 * 
 */
public abstract class MailComponent extends UIComponentBase
{

   // Cache Message

   private UIMessage message;

   private static final String FAMILY = "org.jboss.seam.mail";

   @Override
   public String getFamily()
   {
      return FAMILY;
   }

   @Override
   public boolean getRendersChildren()
   {
      return true;
   }

   protected String encode(FacesContext facesContext) throws IOException
   {
      return encode(facesContext, this, null, false);
   }

  
   /**
    * Deprecated, use {@link MailComponent#encode(FacesContext, UIComponent, String)}
    */
   @Deprecated
   protected String encode(FacesContext facesContext, UIComponent cmp) throws IOException
   {
      return encode(facesContext, cmp, null, true);
   }
   
   protected String encode(FacesContext facesContext, String contentType) throws IOException
   {
      return encode(facesContext, this, contentType, false);
   }
   
   protected String encode(FacesContext facesContext, UIComponent cmp, String contentType) throws IOException
   {
      return encode(facesContext, cmp, contentType, true);
   }

   /**
    * Encode the children of cmp, writing to a string (rather than the http
    * response object) and return the string
    */
   protected String encode(FacesContext facesContext, UIComponent cmp, String contentType, boolean root)
            throws IOException
   {
      ResponseWriter response = facesContext.getResponseWriter();
      StringWriter stringWriter = new StringWriter();
      ResponseWriter cachingResponseWriter = ((MailResponseWriter) response).cloneWithWriter(
               stringWriter, contentType);
      facesContext.setResponseWriter(cachingResponseWriter);
      if (root)
      {
         JSF.renderChild(facesContext, cmp);
      }
      else
      {
         JSF.renderChildren(facesContext, cmp);
      }
      facesContext.setResponseWriter(response);
      String output = stringWriter.getBuffer().toString();
      return output;
   }

   /**
    * look up the tree for mail message
    * 
    * @throws MessagingException
    */
   public MimeMessage findMimeMessage() throws MessagingException
   {
      return findMessage().getMimeMessage();
   }

   /**
    * look up the tree for UIMessage
    */
   public UIMessage findMessage()
   {
      if (message == null)
      {
         message = (UIMessage) findParent(this, UIMessage.class);
         if (message == null)
         {
            throw new UnsupportedOperationException("Must have a m:message tag in the tree");
         }
      }
      return message;
   }

   public MimeMultipart getRootMultipart() throws IOException, MessagingException
   {
      return (MimeMultipart) findMimeMessage().getContent();
   }

   /**
    * Deprecated, use {@link MailComponent#findParent(UIComponent, Class)}
    */
   @Deprecated
   public MailComponent findParent(UIComponent parent)
   {
      return findParent(parent, null);
   }

   /**
    * find the first parent that is a mail component of a given type
    */
   public MailComponent findParent(UIComponent parent, Class<?> c)
   {
      if (parent == null)
      {
         return null;
      }

      if (parent instanceof MailComponent)
      {
         if (c == null || c.isAssignableFrom(parent.getClass()))
         {
            return (MailComponent) parent;
         }
      }

      return findParent(parent.getParent(), c);
   }

   /**
    * Get a valuebinding as a string
    */
   protected String getString(String localName)
   {
      if (getValue(localName) != null)
      {
         return getValue(localName).toString();
      }
      else
      {
         return null;
      }
   }

   /**
    * Get a vauebinding
    */
   protected Object getValue(String localName)
   {
      if (getValueExpression(localName) == null)
      {
         return null;
      }
      else
      {
         return getValueExpression(localName).getValue(getFacesContext().getELContext());
      }
   }

   /**
    * Get a valuebinding as a Boolean
    */
   protected Boolean getBoolean(String localName)
   {
      Object o = getValue(localName);
      if (o != null)
      {
         if (o instanceof Boolean)
         {
            return (Boolean) o;
         }
         else
         {
            return Boolean.valueOf(o.toString());
         }
      }
      else
      {
         return null;
      }
   }
}
