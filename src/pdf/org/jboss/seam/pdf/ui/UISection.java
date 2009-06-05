package org.jboss.seam.pdf.ui;

import javax.faces.context.*;
import com.lowagie.text.*;

public class UISection extends ITextComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.pdf.ui.UISection";

   Section section;
   Integer numberDepth;

   public void setNumberDepth(Integer numberDepth)
   {
      this.numberDepth = numberDepth;
   }

   public Section getSection()
   {
      return section;
   }

   @Override
   public Object getITextObject()
   {
      return null; // don't add to parent - already added.
   }

   @Override
   public void removeITextObject()
   {
      section = null;
   }

   @Override
   public void handleAdd(Object o)
   {
      section.add(o);
   }

   @Override
   public void createITextObject(FacesContext context)
   {
      UISection uiParent = (UISection) findITextParent(getParent(), UISection.class);

      Section sectionParent = uiParent.getSection();
      if (sectionParent == null)
      {
         throw new RuntimeException("section must have a parent chapter/section");
      }

      numberDepth = (Integer) valueBinding(context, "numberDepth", numberDepth);
      if (numberDepth == null)
      {
         numberDepth = countSectionParents(this, 0);
      }

      section = sectionParent.addSection(new Paragraph(""), numberDepth);
   }

   private int countSectionParents(UISection component, int level)
   {
      if (component == null)
      {
         return level;
      }
      return countSectionParents((UISection) findITextParent(component.getParent(), UISection.class), level + 1);
   }

   public void setTitle(Paragraph title)
   {
      section.setTitle(title);
   }
}
