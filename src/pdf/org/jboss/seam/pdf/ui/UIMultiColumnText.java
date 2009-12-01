package org.jboss.seam.pdf.ui;

import java.io.IOException;

import javax.faces.context.FacesContext;

import org.jboss.seam.pdf.ITextUtils;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.MultiColumnText;


public class UIMultiColumnText 
    extends ITextComponent
{

   float left = 36;
   float right = PageSize.LETTER.getWidth()-36;
   String direction = "default";
   
   MultiColumnText multiColumnText = null;

   
   public float getLeft()
   {
      return (Float) valueBinding("left", left);
   }

   public void setLeft(float left)
   {
      this.left = left;
   }

   public float getRight()
   {
      return (Float) valueBinding("right", right);
   }

   public void setRight(float right)
   {
      this.right = right;
   }

   public String getDirection()
   {
      return (String) valueBinding("direction", direction);
   }

   public void setDirection(String direction)
   {
      this.direction = direction;
   }

   @Override
   public void createITextObject(FacesContext context) throws IOException, DocumentException
   {
      multiColumnText = new MultiColumnText();
      
      multiColumnText.addSimpleColumn(getLeft(), getRight());
      multiColumnText.setRunDirection(ITextUtils.runDirection(getDirection()));
   }

   @Override
   public Object getITextObject()
   {
      return multiColumnText;
   }

   @Override
   public void handleAdd(Object other)
   {
      if (other instanceof Element) {
         try {
            multiColumnText.addElement((Element)other);
         } catch (DocumentException e) {
            throw new RuntimeException(e);
         }     
      } else {
         throw new RuntimeException("UIMultiColumnText only supports Element children");
      }
      
   }

   @Override
   public void removeITextObject()
   {
      multiColumnText = null;
   }
   
}
