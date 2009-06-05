package org.jboss.seam.ui.renderkit;



import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.ui.component.UIButton;
import org.jboss.seam.ui.util.HTML;
import org.jboss.seam.ui.util.cdk.RendererBase;

public class ButtonRendererBase extends RendererBase
{

   @Override
   protected Class getComponentClass()
   {
      return UIButton.class;
   }
   
   private String getType(UIButton button)
   {
      if (button.getImage() == null) {
         return "button";
     } else {
         return "image";
     }
   }
   

   private String getOnClick(UIButton button) throws IOException
   {
      String onclick = button.getOnclick();
      String url = button.getUrl();
      if (onclick == null)
      {
         onclick = "";
      }
      else if (onclick.length() > 0 && !onclick.endsWith(";"))
      {
         onclick += "; ";
      }
      if (url != null)
      {
         onclick += "location.href='" + url + "'; ";
      }
      onclick += "return false;";
      if (!button.isDisabled())
      {
         return onclick;
      }
      else
      {
         return null;
      }
   }
   
   @Override
   protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      UIButton button = (UIButton) component;
      writer.startElement(HTML.INPUT_ELEM, button);
      String onclick = getOnClick(button);
      if (!("").equals(onclick) && !button.isDisabled())
      {
         writer.writeAttribute(HTML.ONCLICK_ATTR, onclick, HTML.ONCLICK_ATTR);
      }
      if (button.getId() != null)
      {
         writer.writeAttribute(HTML.ID_ATTR, button.getClientId(context), HTML.ID_ATTR);
      }
      HTML.renderHTMLAttributes(writer, button, HTML.BUTTON_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_ONCLICK);
      if (button.getValue() != null)
      {
         writer.writeAttribute(HTML.VALUE_ATTR, button.getValue(), HTML.VALUE_ATTR);
      }
      if (button.isDisabled())
      {
         writer.writeAttribute(HTML.DISABLED_ATTR, true, HTML.DISABLED_ATTR);
      }
      writer.writeAttribute(HTML.TYPE_ATTR, getType(button), HTML.TYPE_ATTR);
      if (button.getImage() != null)
      {
         writer.writeAttribute(HTML.SRC_ATTR, button.getImage(), HTML.SRC_ATTR);
      } 
   }
   
   @Override
   protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      writer.endElement(HTML.INPUT_ELEM);
   }

}
