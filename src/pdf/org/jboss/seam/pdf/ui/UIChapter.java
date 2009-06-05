package org.jboss.seam.pdf.ui;

import javax.faces.context.*;
import com.lowagie.text.*;

public class UIChapter extends UISection
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.pdf.ui.UIChapter";

   Integer number = 1;

   public Chapter getChapter()
   {
      return (Chapter) getSection();
   }

   public void setNumber(Integer number)
   {
      this.number = number;
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
      section = new Chapter("", number);
   }
}
