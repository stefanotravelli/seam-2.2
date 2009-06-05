package org.jboss.seam.pdf.ui;

import javax.faces.context.FacesContext;

import com.lowagie.text.Element;

public class UIElement extends ITextComponent
{
   Element element;

   public void setValue(Element element)
   {
      this.element = element;
   }

   @Override
   public void createITextObject(FacesContext context)
   {
      element = (Element) valueBinding(context, "value", element);
   }

   @Override
   public Object getITextObject()
   {
      return element;
   }

   @Override
   public void handleAdd(Object other)
   {
      throw new RuntimeException("No children allowed");
   }

   @Override
   public void removeITextObject()
   {
      element = null;
   }

}
