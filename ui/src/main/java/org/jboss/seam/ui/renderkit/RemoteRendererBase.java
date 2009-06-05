package org.jboss.seam.ui.renderkit;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.ui.component.UIRemote;
import org.jboss.seam.ui.util.cdk.RendererBase;

/**
 * Renderer for rendering the required &lt;script&gt; tags for Seam Remoting
 * 
 * @author Shane Bryzak
 */
public class RemoteRendererBase extends RendererBase
{
   @Override
   protected Class getComponentClass()
   {
      return UIRemote.class;
   }

   @Override
   protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component) 
      throws IOException
   {
      UIRemote remote = (UIRemote) component;
      
      writeScript(context, remote);
   }
   
   @Override
   protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) 
      throws IOException
   {
      writer.flush();
   }
   
   public void writeScript(FacesContext context, UIRemote remote) 
      throws IOException
   {
      ResponseWriter response = context.getResponseWriter();
      
      Map request = context.getExternalContext().getRequestMap();
      if (!request.containsKey("REMOTE_SCRIPT"))
      {
         response.startElement("script", null);
         response.writeAttribute("type", "text/javascript", null);
         response.writeAttribute("src", context.getExternalContext().getRequestContextPath()
                  + "/seam/resource/remoting/resource/remote.js", null);
         response.endElement("script");
         request.put("REMOTE_SCRIPT", true);
      }

      response.startElement("script", null);
      response.writeAttribute("type", "text/javascript", null);
      response.writeAttribute("src", context.getExternalContext().getRequestContextPath()
               + "/seam/resource/remoting/interface.js?" + 
               remote.getInclude().replace(',', '&'), null);
      response.endElement("script");
   }
}
