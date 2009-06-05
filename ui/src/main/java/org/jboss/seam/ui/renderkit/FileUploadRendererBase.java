package org.jboss.seam.ui.renderkit;


import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.ServletRequest;

import org.jboss.seam.ui.component.UIFileUpload;
import org.jboss.seam.ui.util.HTML;
import org.jboss.seam.ui.util.cdk.RendererBase;
import org.jboss.seam.web.MultipartRequest;

public class FileUploadRendererBase extends RendererBase
{

   @Override
   protected Class getComponentClass()
   {
      return UIFileUpload.class;
   }
   
   @Override
   protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      UIFileUpload fileUpload = (UIFileUpload) component;
      
      writer.startElement(HTML.INPUT_ELEM, fileUpload);      
      writer.writeAttribute(HTML.TYPE_ATTR, HTML.FILE_ATTR, null);      
      
      String clientId = fileUpload.getClientId(context);      
      writer.writeAttribute(HTML.ID_ATTR, clientId, null);     
      writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
      
      
      /*if (fileUpload.getAccept() != null)
      {
         writer.writeAttribute(HTML.ACCEPT_ATTR, fileUpload.getAccept(), "accept");
      }
      
      if (fileUpload.getStyleClass() != null)
      {
         writer.writeAttribute(HTML.CLASS_ATTR, fileUpload.getStyleClass(), JSF.STYLE_CLASS_ATTR);
      }
      
      if (fileUpload.getStyle() != null)
      {
         writer.writeAttribute(HTML.STYLE_ATTR, fileUpload.getStyle(),  "style");
      }*/
      
      HTML.renderHTMLAttributes(writer, component, HTML.INPUT_FILE_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED);
      
      
      writer.endElement(HTML.INPUT_ELEM);
   }

   @Override
   protected void doDecode(FacesContext context, UIComponent component)
   {
      UIFileUpload fileUpload = (UIFileUpload) component;
      ServletRequest request = (ServletRequest) context.getExternalContext().getRequest();

      if (!(request instanceof MultipartRequest))
      {
         request = unwrapMultipartRequest(request);
      }

      if (request instanceof MultipartRequest)
      {
         MultipartRequest multipartRequest = (MultipartRequest) request;

         String clientId = component.getClientId(context);
         fileUpload.setLocalInputStream(multipartRequest.getFileInputStream(clientId));
         fileUpload.setLocalContentType(multipartRequest.getFileContentType(clientId));
         fileUpload.setLocalFileName(multipartRequest.getFileName(clientId));
         fileUpload.setLocalFileSize(multipartRequest.getFileSize(clientId));
      }
   }

   /**
    * Finds an instance of MultipartRequest wrapped within a request or its
    * (recursively) wrapped requests.
    */
   private static ServletRequest unwrapMultipartRequest(ServletRequest request)
   {
      while (!(request instanceof MultipartRequest))
      {
         boolean found = false;

         for (Method m : request.getClass().getMethods())
         {
            if (ServletRequest.class.isAssignableFrom(m.getReturnType())
                     && m.getParameterTypes().length == 0)
            {
               try
               {
                  request = (ServletRequest) m.invoke(request);
                  found = true;
                  break;
               }
               catch (Exception ex)
               { /* Ignore, try the next one */
               }
            }
         }

         if (!found)
         {
            for (Field f : request.getClass().getDeclaredFields())
            {
               if (ServletRequest.class.isAssignableFrom(f.getType()))
               {
                  try
                  {
                     request = (ServletRequest) f.get(request);
                  }
                  catch (Exception ex)
                  { /* Ignore */
                  }
               }
            }
         }

         if (!found) break;
      }

      return request;
   }

}
