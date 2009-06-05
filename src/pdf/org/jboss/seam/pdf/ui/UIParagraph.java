package org.jboss.seam.pdf.ui;

import javax.faces.context.*;
import com.lowagie.text.*;

public class UIParagraph extends ITextComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.pdf.ui.UIParagraph";

   Paragraph paragraph;
   String alignment;

   Float firstLineIndent;
   Float extraParagraphSpace;
   Float leading;
   Float multipliedLeading;
   Float spacingBefore;
   Float spacingAfter;
   Float indentationLeft;
   Float indentationRight;

   Boolean keepTogether;

   public void setAlignment(String alignment)
   {
      this.alignment = alignment;
   }

   public void setFirstLineIndent(Float firstLineIndent)
   {
      this.firstLineIndent = firstLineIndent;
   }

   public void setExtraParagraphSpace(Float extraParagraphSpace)
   {
      this.extraParagraphSpace = extraParagraphSpace;
   }

   public void setLeading(Float leading)
   {
      this.leading = leading;
   }

   public void setMultipliedLeading(Float multipliedLeading)
   {
      this.multipliedLeading = multipliedLeading;
   }

   public void setSpacingBefore(Float spacingBefore)
   {
      this.spacingBefore = spacingBefore;
   }

   public void setSpacingAfter(Float spacingAfter)
   {
      this.spacingAfter = spacingAfter;
   }

   public void setIndentationLeft(Float indentationLeft)
   {
      this.indentationLeft = indentationLeft;
   }

   public void setIndentationRight(Float indentationRight)
   {
      this.indentationRight = indentationRight;
   }

   public void setKeepTogether(Boolean keepTogether)
   {
      this.keepTogether = keepTogether;
   }

   @Override
   public Object getITextObject()
   {
      return paragraph;
   }

   @Override
   public void removeITextObject()
   {
      paragraph = null;
   }

   @Override
   public void createITextObject(FacesContext context)
   {
      Font font = getFont();
      if (font == null)
      {
         paragraph = new Paragraph();
      }
      else
      {
         paragraph = new Paragraph("", font);
      }

      alignment = (String) valueBinding(context, "alignment", alignment);
      if (alignment != null)
      {
         paragraph.setAlignment(alignment);
      }

      firstLineIndent = (Float) valueBinding(context, "firstLineIndent", firstLineIndent);
      if (firstLineIndent != null)
      {
         paragraph.setFirstLineIndent(firstLineIndent);
      }

      extraParagraphSpace = (Float) valueBinding(context, "extraParagraphSpace", extraParagraphSpace);
      if (extraParagraphSpace != null)
      {
         paragraph.setExtraParagraphSpace(extraParagraphSpace);
      }

      leading = (Float) valueBinding(context, "leading", leading);
      multipliedLeading = (Float) valueBinding(context, "multipliedLeading", multipliedLeading);
      if (leading != null)
      {
         if (multipliedLeading != null)
         {
            paragraph.setLeading(leading, multipliedLeading);
         }
         else
         {
            paragraph.setLeading(leading);
         }
      }

      spacingBefore = (Float) valueBinding(context, "spacingBefore", spacingBefore);
      if (spacingBefore != null)
      {
         paragraph.setSpacingBefore(spacingBefore);
      }

      spacingAfter = (Float) valueBinding(context, "spacingAfter", spacingAfter);
      if (spacingAfter != null)
      {
         paragraph.setSpacingAfter(spacingAfter);
      }

      indentationLeft = (Float) valueBinding(context, "indentationLeft", indentationLeft);
      if (indentationLeft != null)
      {
         paragraph.setIndentationLeft(indentationLeft);
      }

      indentationRight = (Float) valueBinding(context, "indentationRight", indentationRight);
      if (indentationRight != null)
      {
         paragraph.setIndentationRight(indentationRight);
      }

      keepTogether = (Boolean) valueBinding(context, "keepTogether", keepTogether);
      if (keepTogether != null)
      {
         paragraph.setKeepTogether(keepTogether);
      }
   }

   @Override
   public void handleAdd(Object o)
   {
      paragraph.add(o);
   }
}
