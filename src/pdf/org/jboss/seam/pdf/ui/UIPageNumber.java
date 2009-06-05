package org.jboss.seam.pdf.ui;

import java.io.IOException;

import javax.faces.context.FacesContext;

public class UIPageNumber extends ITextComponent
{

   @Override
   public void createITextObject(FacesContext context)
   {
   }

   @Override
   public Object getITextObject()
   {
      return null;
   }

   @Override
   public void handleAdd(Object other)
   {
      throw new RuntimeException("header page number cannot contain other elements");
   }

   @Override
   public void removeITextObject()
   {
   }

   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      super.encodeEnd(context);
      UIHeaderFooter header = (UIHeaderFooter) findITextParent(this, UIHeaderFooter.class);
      if (header == null)
      {
         throw new RuntimeException("pageNumber can only be used in the context of a header or footer");
      }
      header.markPage();
   }

}
