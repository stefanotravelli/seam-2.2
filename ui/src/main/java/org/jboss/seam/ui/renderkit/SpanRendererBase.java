package org.jboss.seam.ui.renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.ui.component.UISpan;
import org.jboss.seam.ui.component.UIStyle;

public class SpanRendererBase extends StyleRendererBase
{

   @Override
   protected Class getComponentClass()
   {
      return UISpan.class;
   }

   @Override
   public void endElement(ResponseWriter writer) throws IOException
   {
      writer.endElement("span");
   }
   
   @Override
   protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      UISpan span = (UISpan) component;
      
      startElement(writer, span);

      writer.writeAttribute("id", component.getClientId(context), "id");
      
      if (span.getStyleClass() != null) 
      {
         writer.writeAttribute("class", span.getStyleClass(), "styleClass");
      }

      if (span.getStyle() != null) 
      {
         writer.writeAttribute("style", span.getStyle(), "style");
      }

      if (span.getTitle() != null) 
      {
         writer.writeAttribute("title", span.getTitle(), "title");
      }      
   }   
   
   @Override
   public void startElement(ResponseWriter writer, UIStyle style) throws IOException
   {
      writer.startElement("span", style);
   }
   
}
