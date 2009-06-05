package org.jboss.seam.pdf.ui;

import javax.faces.context.*;

import java.io.*;

import com.lowagie.text.*;

public class UIPage extends ITextComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.pdf.ui.UIPage";

   @Override
   public Object getITextObject()
   {
      return null;
   }

   @Override
   public void removeITextObject()
   {
      // nothing to do
   }

   @Override
   public void createITextObject(FacesContext context)
   {
      // nothing to do
   }

   @Override
   public void handleAdd(Object o)
   {
      addToITextParent(o);
   }

   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      super.encodeBegin(context);
      Document document = findDocument();
      if (document != null)
      {
         document.newPage();
      }
      else
      {
         throw new IllegalArgumentException("Cannot find parent document");
      }
   }

}
