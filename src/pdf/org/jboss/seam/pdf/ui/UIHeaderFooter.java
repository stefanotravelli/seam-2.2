package org.jboss.seam.pdf.ui;

import java.io.IOException;

import javax.faces.context.FacesContext;

import org.jboss.seam.pdf.ITextUtils;

import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Phrase;

public abstract class UIHeaderFooter extends UIRectangle
{
   HeaderFooter header;
   Phrase before;
   Phrase after;

   String alignment;

   public UIHeaderFooter()
   {
      super();
   }

   public void setAlignment(String alignment)
   {
      this.alignment = alignment;
   }

   @Override
   public void createITextObject(FacesContext context)
   {
      before = defaultPhrase();
      after = null;
   }

   @Override
   public Object getITextObject()
   {
      return null;
   }

   @Override
   public void handleAdd(Object other)
   {
      if (after == null)
      {
         before.add(other);
      }
      else
      {
         after.add(other);
      }
   }

   @Override
   public void removeITextObject()
   {
      before = null;
      after = null;
   }

   public void markPage()
   {
      after = defaultPhrase();
   }

   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      HeaderFooter header;

      if (after == null)
      {
         header = new HeaderFooter(before, false);
      }
      else
      {
         header = new HeaderFooter(before, after);
      }

      alignment = (String) valueBinding(context, "alignment", alignment);
      if (alignment != null)
      {
         header.setAlignment(ITextUtils.alignmentValue(alignment));
      }

      applyRectangleProperties(context, header);

      super.encodeEnd(context);
      handleHeaderFooter(header);
   }

   public abstract void handleHeaderFooter(HeaderFooter item);

   /**
    * HeaderFooter derives the font for the number from the font of the before
    * phrase. Worse still, there is no way to set the font after the phrase is
    * created. The best we can do is get the surrounding font context and hope
    * for the best.
    */
   private Phrase defaultPhrase()
   {
      Font font = getFont();
      if (font == null)
      {
         return new Phrase();
      }
      else
      {
         return new Phrase("", font);
      }
   }
}