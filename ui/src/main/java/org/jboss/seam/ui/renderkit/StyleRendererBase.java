package org.jboss.seam.ui.renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.ui.component.UIStyle;
import org.jboss.seam.ui.util.cdk.RendererBase;

public abstract class StyleRendererBase extends RendererBase
{
   
   @Override
   protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      UIStyle style = (UIStyle) component;
      
      startElement(writer, style);

      writer.writeAttribute("id", component.getClientId(context), "id");
      
      if (style.getStyleClass() != null) 
      {
         writer.writeAttribute("class", style.getStyleClass(), "styleClass");
      }

      if (style.getStyle() != null) 
      {
         writer.writeAttribute("style", style.getStyle(), "style");
      }
   }
   
   @Override
   protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      endElement(writer);
   }


   public abstract void startElement(ResponseWriter writer, UIStyle style) throws IOException;
   public abstract void endElement(ResponseWriter writer) throws IOException;
   
}
