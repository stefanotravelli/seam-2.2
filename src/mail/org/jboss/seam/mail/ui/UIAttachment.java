package org.jboss.seam.mail.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.faces.FacesException;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.ui.util.JSF;
import org.jboss.seam.util.FacesResources;
import org.jboss.seam.util.RandomStringUtils;
import org.jboss.seam.util.Reflections;

public class UIAttachment extends MailComponent implements ValueHolder
{

   public class AttachmentStatus 
   {
      
      private String contentId;
      
      public String getContentId()
      {
         return contentId;
      }
      
      public void setContentId(String contentId)
      {
         this.contentId = contentId;
      }
      
   }
   
   private Object value;

   private String contentType;

   private String fileName;
   
   private String status;
   
   private String disposition = "attachment";

   public Object getValue()
   {
      if (value != null)
      {
         return value;
      }
      else
      {
         return getValue("value");
      }
   }

   public void setValue(Object value)
   {
      this.value = value;
   }
   
   public String getStatus()
   {
      return status;
   }
   
   public void setStatus(String status)
   {
      this.status = status;
   }
   
   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (this.getChildCount() > 0) {
         if (Reflections.isInstanceOf(this.getChildren().get(0).getClass(), "org.jboss.seam.pdf.ui.UIDocument") ||
             Reflections.isInstanceOf(this.getChildren().get(0).getClass(), "org.jboss.seam.excel.ui.UIWorkbook")) 
         {
            Method method = Reflections.getSetterMethod(this.getChildren().get(0).getClass(), "sendRedirect");
            Reflections.invokeAndWrap(method, this.getChildren().get(0), false);
            JSF.renderChildren(context, this);
         } else {
            setValue(encode(context).getBytes());
            if (getContentType() == null) {
               // User hasn't specified content, assume html
               setContentType("text/html");
            }
         }
      }
   }
   
   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      DataSource ds = null;
      try
      {
         if (getValue() instanceof URL)
         {
            URL url = (URL) getValue();
            ds = new URLDataSource(url);
         }
         else if (getValue() instanceof File)
         {
            File file = (File) getValue();
            ds = new FileDataSource(file);
         }
         else if (getValue() instanceof String)
         {
            String string = (String) getValue();
            ds = new URLDataSource( FacesResources.getResource( string, context.getExternalContext() ) );
         }
         else if (getValue() instanceof InputStream)
         {
            InputStream is = (InputStream) getValue();
            ds = new ByteArrayDataSource(is, getContentType());
         }
         else if (getValue() != null && Reflections.isInstanceOf(getValue().getClass(), "org.jboss.seam.document.DocumentData"))
         {
            Method dataGetter = Reflections.getGetterMethod(getValue().getClass(), "data");
            Method docTypeGetter = Reflections.getGetterMethod(getValue().getClass(), "documentType");
            Object docType = Reflections.invokeAndWrap(docTypeGetter, getValue());
            Method mimeTypeGetter = Reflections.getGetterMethod(docType.getClass(), "mimeType");
            ds = new ByteArrayDataSource((byte[]) Reflections.invokeAndWrap(dataGetter, getValue()), (String) Reflections.invokeAndWrap(mimeTypeGetter, docType));
         }
         else if (getValue() != null && getValue().getClass().isArray())
         {
            if (getValue().getClass().getComponentType().isAssignableFrom(Byte.TYPE))
            {
               byte[] b = (byte[]) getValue();
               ds = new ByteArrayDataSource(b, getContentType());
            }
         }
         if (ds != null)
         {
             // Check the DataSource is available
             try
             {
                 ds.getInputStream();
             }
             catch (Exception e) 
             {
                 if (value != null)
                 {
                     throw new NullPointerException("Error accessing " + value);
                 }
                 else
                 {
                     throw new NullPointerException("Error accessing " + getValueExpression("value").getExpressionString());
                 }
            }
            MimeBodyPart attachment = new MimeBodyPart();
            // Need to manually set the contentid
            String contentId = RandomStringUtils.randomAlphabetic(20).toLowerCase();
            if(disposition.equals("inline"))
            {
               attachment.setContentID(new Header("<" + contentId + ">").getSanitizedValue());
            }
            attachment.setDataHandler(new DataHandler(ds));
            attachment.setFileName(new Header(getName(ds.getName())).getSanitizedValue());
            attachment.setDisposition(new Header(getDisposition()).getSanitizedValue());
            findMessage().getAttachments().add(attachment);
            if (getStatus() != null)
            {
               AttachmentStatus attachmentStatus = new AttachmentStatus();
               if(disposition.equals("inline"))
               {
                  attachmentStatus.setContentId(contentId);
               }
               Contexts.getEventContext().set(getStatus(), attachmentStatus);
            }
         }
      }
      catch (MessagingException e)
      {
         throw new FacesException(e.getMessage(), e);
      }
   }

   public String getContentType()
   {
      if (contentType == null)
      {
         return getString("contentType");
      }
      else
      {
         return contentType;
      }
   }

   public void setContentType(String contentType)
   {
      this.contentType = contentType;
   }

   public String getFileName()
   {
      if (fileName == null)
      {
         return getString("fileName");
      }
      else
      {
         return fileName;
      }
   }

   public void setFileName(String fileName)
   {
      this.fileName = fileName;
   }

   private String removePath(String fileName)
   {
      if (fileName.lastIndexOf("/") > 0)
      {
         return fileName.substring(fileName.lastIndexOf("/") + 1);
      }
      else
      {
         return fileName;
      }
   }

   private String getName(String name)
   {
      if (getFileName() != null)
      {
         return getFileName();
      }
      else
      {
         return removePath(name);
      }
   }

   public Converter getConverter()
   {
      return null;
   }

   public Object getLocalValue()
   {
      return value;
   }

   public void setConverter(Converter converter)
   {
      throw new UnsupportedOperationException("Cannot attach a converter to an attachment");
   }
   
   public String getDisposition()
   {
      return disposition;
   }
   
   public void setDisposition(String disposition)
   {
      if ("attachment".equals(disposition) || "inline".equals(disposition))
      {
         this.disposition = disposition;
      }
   }
}
