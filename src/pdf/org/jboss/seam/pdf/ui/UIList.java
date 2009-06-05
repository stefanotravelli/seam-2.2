package org.jboss.seam.pdf.ui;

import javax.faces.context.*;
import com.lowagie.text.*;

public class UIList extends ITextComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.pdf.ui.UIList";

   public static final String STYLE_NUMBERED = "NUMBERED";
   public static final String STYLE_LETTERED = "LETTERED";
   public static final String STYLE_GREEK = "GREEK";
   public static final String STYLE_ROMAN = "ROMAN";
   public static final String STYLE_DINGBATS = "ZAPFDINGBATS";
   public static final String STYLE_DINGBATS_NUMBER = "ZAPFDINGBATS_NUMBER";

   List list;

   String style;
   String listSymbol;
   float indent = 20;
   Boolean lowerCase = false;
   Integer charNumber;
   Integer numberType = 0;

   public void setStyle(String style)
   {
      this.style = style;
   }

   public void setIndent(float indent)
   {
      this.indent = indent;
   }

   public void setListSymbol(String listSymbol)
   {
      this.listSymbol = listSymbol;
   }

   /* for dingbats symbol list */
   public void setCharNumber(Integer charNumber)
   {
      this.charNumber = charNumber;
   }

   /* for dingbats number list */
   public void setNumberType(Integer numberType)
   {
      this.numberType = numberType;
   }

   /* for ROMAN,GREEK */
   public void setLowerCase(Boolean lowerCase)
   {
      this.lowerCase = lowerCase;
   }

   @Override
   public Object getITextObject()
   {
      return list;
   }

   @Override
   public void removeITextObject()
   {
      list = null;
   }

   @Override
   public void createITextObject(FacesContext context)
   {
      style = (String) valueBinding(context, "style", style);
      lowerCase = (Boolean) valueBinding(context, "lowerCase", lowerCase);
      indent = (Float) valueBinding(context, "indent", indent);
      listSymbol = (String) valueBinding(context, "listSymbol", listSymbol);

      if (style != null)
      {
         if (style.equalsIgnoreCase(STYLE_ROMAN))
         {
            list = new RomanList((int) indent); // int? bug in text?
            if (lowerCase != null)
            {
               list.setLowercase(lowerCase);
               // ((RomanList) list).setRomanLower(lowerCase);
            }
         }
         else if (style.equalsIgnoreCase(STYLE_GREEK))
         {
            list = new GreekList((int) indent); // int? bug in itext?

            if (lowerCase != null)
            {
               list.setLowercase(lowerCase);
               // ((GreekList) list).setGreekLower(lowerCase);
            }
         }
         else if (style.equalsIgnoreCase(STYLE_DINGBATS))
         {
            charNumber = (Integer) valueBinding(context, "charNumber", charNumber);
            list = new ZapfDingbatsList(charNumber, (int) indent);
         }
         else if (style.equalsIgnoreCase(STYLE_DINGBATS_NUMBER))
         {
            numberType = (Integer) valueBinding(context, "numberType", numberType);
            list = new ZapfDingbatsNumberList(numberType, (int) indent);

         }
         else if (style.equalsIgnoreCase(STYLE_NUMBERED))
         {
            list = new List(true, indent);
            // setFirst(int)
         }
         else if (style.equalsIgnoreCase(STYLE_LETTERED))
         {
            list = new List(false, true, indent);
            // setFirst(char)
         }
      }

      if (list == null)
      {
         list = new List(false, indent);
         if (listSymbol != null)
         {
            list.setListSymbol(listSymbol);
         }
      }
   }

   @Override
   public void handleAdd(Object o)
   {
      list.add(o);
   }
}
