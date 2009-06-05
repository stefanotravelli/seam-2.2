package org.jboss.seam.pdf.ui;

import javax.faces.context.*;
import com.lowagie.text.*;

public class UIAnchor extends ITextComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.pdf.ui.UIAnchor";

   Anchor anchor;

   String name;
   String reference;

   public void setName(String name)
   {
      this.name = name;
   }

   public void setReference(String reference)
   {
      this.reference = reference;
   }

   @Override
   public Object getITextObject()
   {
      return anchor;
   }

   @Override
   public void removeITextObject()
   {
      anchor = null;
   }

   @Override
   public void createITextObject(FacesContext context)
   {
      anchor = new Anchor();

      name = (String) valueBinding(context, "name", name);
      if (name != null)
      {
         anchor.setName(name);
      }

      reference = (String) valueBinding(context, "reference", reference);
      if (reference != null)
      {
         anchor.setReference(reference);
      }
   }

   @Override
   public void handleAdd(Object o)
   {
      anchor.add(o);
   }
}
