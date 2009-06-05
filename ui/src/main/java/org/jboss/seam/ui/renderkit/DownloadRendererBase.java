package org.jboss.seam.ui.renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;

import org.jboss.seam.ui.component.UIDownload;
import org.jboss.seam.ui.component.UIResource;
import org.jboss.seam.ui.util.HTML;
import org.jboss.seam.ui.util.ViewUrlBuilder;
import org.jboss.seam.ui.util.cdk.RendererBase;

public class DownloadRendererBase extends RendererBase
{

   @Override
   protected Class getComponentClass()
   {
      return UIDownload.class;
   }

   @Override
   protected void doEncodeBegin(javax.faces.context.ResponseWriter writer, FacesContext ctx, UIComponent component) throws IOException
   {
      UIDownload download = (UIDownload) component;

      if (!download.isRendered())
         return;

      if (download.getSrc() != null)
      {

         ViewUrlBuilder builder = new ViewUrlBuilder(download.getSrc(), null);

         for (Object child : download.getChildren())
         {
            if (child instanceof UIParameter)
            {
               builder.addParameter((UIParameter) child);
            }

         }
         writeStartTag(writer, download, builder.getEncodedUrl());
         renderNonResourceChildren(ctx, download);
         writer.endElement(HTML.ANCHOR_ELEM);

      }

   }

   private void renderNonResourceChildren(FacesContext ctx, UIComponent component) throws IOException
   {
      for (Object child : component.getChildren())
      {
         if (!(child instanceof UIParameter) && !(child instanceof UIResource) && (child instanceof UIComponent))
         {
            renderChild(ctx, (UIComponent) child);
         }

      }
   }

   private void writeStartTag(javax.faces.context.ResponseWriter writer, UIDownload download, String url) throws IOException
   {
      writer.startElement(HTML.ANCHOR_ELEM, null);
      writer.writeAttribute(HTML.HREF_ATTR, url, null);
      if (download.getStyle() != null)
         writer.writeAttribute(HTML.STYLE_ATTR, download.getStyle(), null);
      if (download.getStyleClass() != null)
         writer.writeAttribute(HTML.STYLE_CLASS_ATTR, download.getStyleClass(), null);
   }

   @Override
   public boolean getRendersChildren()
   {
      return true;
   }

}
