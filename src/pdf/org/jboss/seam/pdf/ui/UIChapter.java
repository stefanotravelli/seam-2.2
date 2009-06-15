package org.jboss.seam.pdf.ui;

import javax.faces.context.*;
import com.lowagie.text.*;

public class UIChapter extends UISection
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.pdf.ui.UIChapter";

   Integer number = 1;
   Integer numberDepth = 1;

   public Chapter getChapter()
   {
      return (Chapter) getSection();
   }

   public void setNumber(Integer number)
   {
      this.number = number;
   }
   
   public void setNumberDepth(Integer numberDepth)
   {
      this.numberDepth = numberDepth;
   }

   @Override
   public Object getITextObject()
   {
      return section;
   }

   // removeITextObject, handleAdd set by parent

   @Override
   public void createITextObject(FacesContext context)
   {
      number = (Integer) valueBinding(context, "number", number);
      numberDepth = (Integer) valueBinding(context, "numberDepth", numberDepth);
      section = new Chapter("", number);
      section.setNumberDepth(numberDepth);
   }
}
