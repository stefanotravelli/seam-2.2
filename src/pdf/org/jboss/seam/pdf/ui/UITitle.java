package org.jboss.seam.pdf.ui;

import javax.faces.context.FacesContext;

import com.lowagie.text.Paragraph;

public class UITitle extends ITextComponent
{
   @Override
   public void createITextObject(FacesContext context)
   {
      //
   }

   @Override
   public Object getITextObject()
   {
      return null;
   }

   @Override
   public void handleAdd(Object other)
   {
      Paragraph paragraph = null;

      if (other instanceof Paragraph)
      {
         paragraph = (Paragraph) other;
      }

      if (paragraph == null)
      {
         throw new RuntimeException("title must be a paragraph");
      }

      UISection section = (UISection) findITextParent(this, UISection.class);
      if (section == null)
      {
         throw new RuntimeException("cannot find parent chapter/section for title");
      }

      section.getSection().setTitle(paragraph);
   }

   @Override
   public void removeITextObject()
   {
      //
   }

}
