package org.jboss.seam.ui.renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.ui.component.UILink;
import org.jboss.seam.ui.util.HTML;
import org.jboss.seam.ui.util.cdk.RendererBase;

public class LinkRendererBase extends RendererBase
{

   @Override
   protected Class getComponentClass()
   {
      return UILink.class;
   }
   
   @Override
   protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      UILink link = (UILink) component;
      String url = link.getUrl();
      writer.startElement(HTML.ANCHOR_ELEM, link);
      if (url != null && !link.isDisabled())
      {
         writer.writeAttribute(HTML.HREF_ATTR, url, HTML.HREF_ATTR);
      }
      if (link.getId() != null)
      {
         writer.writeAttribute(HTML.ID_ATTR, link.getClientId(context), HTML.ID_ATTR);
      }
      HTML.renderHTMLAttributes(writer, link, HTML.ANCHOR_PASSTHROUGH_ATTRIBUTES);
      if (link.getValue() != null)
      {
         writer.writeText(link.getValue(), null);
      }
   }
   
   @Override
   protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      writer.endElement(HTML.ANCHOR_ELEM);
   }
   
   
}
