package org.jboss.seam.pdf.ui;

import javax.faces.context.*;

import org.jboss.seam.pdf.ITextUtils;

import com.lowagie.text.*;

public class UIListItem extends ITextComponent
{
   public static final String COMPONENT_TYPE = "org.jboss.seam.pdf.ui.UIListItem";

   ListItem listItem;

   String alignment;
   Float indentationLeft;
   Float indentationRight;
   Integer listSymbol;

   public void setListSymbol(Integer listSymbol)
   {
      this.listSymbol = listSymbol;
   }

   public void setAlignment(String alignment)
   {
      this.alignment = alignment;
   }

   public void setIndentationLeft(Float indentationLeft)
   {
      this.indentationLeft = indentationLeft;
   }

   public void setIndentationRight(Float indentationRight)
   {
      this.indentationRight = indentationRight;
   }

   @Override
   public Object getITextObject()
   {
      return listItem;
   }

   @Override
   public void createITextObject(FacesContext context)
   {
      listItem = new ListItem();

      // should listSymbol be a facet?
      listSymbol = (Integer) valueBinding(context, "listSymbol", listSymbol);
      if (listSymbol != null)
      {
         int symbol = listSymbol;
         Font font = getFont();
         if (font == null)
         {
            listItem.setListSymbol(new Chunk((char) symbol));
         }
         else
         {
            listItem.setListSymbol(new Chunk((char) symbol, font));
         }
      }

      alignment = (String) valueBinding(context, "alignment", alignment);
      if (alignment != null)
      {
         listItem.setAlignment(ITextUtils.alignmentValue(alignment));
      }

      indentationLeft = (Float) valueBinding(context, "indentationLeft", indentationLeft);
      if (indentationLeft != null)
      {
         listItem.setIndentationLeft(indentationLeft);
      }

      indentationRight = (Float) valueBinding(context, "indentationRight", indentationRight);
      if (indentationRight != null)
      {
         listItem.setIndentationRight(indentationRight);
      }
   }

   @Override
   public void removeITextObject()
   {
      listItem = null;
   }

   @Override
   public void handleAdd(Object o)
   {
      listItem.add(o);
   }
}
