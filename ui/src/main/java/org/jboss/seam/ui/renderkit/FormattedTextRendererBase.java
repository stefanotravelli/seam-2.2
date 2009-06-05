package org.jboss.seam.ui.renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.ui.component.UIFormattedText;
import org.jboss.seam.ui.util.cdk.RendererBase;

public class FormattedTextRendererBase extends RendererBase
{

   @Override
   protected Class getComponentClass()
   {
      return UIFormattedText.class;
   }
   
   @Override
   protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      String formattedText = ((UIFormattedText) component).getFormattedText();
      if (formattedText != null)
      {
         writer.write(formattedText);
      }
   }
}
