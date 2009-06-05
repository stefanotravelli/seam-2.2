package org.jboss.seam.pdf.ui;

import java.awt.Graphics2D;

import javax.faces.context.FacesContext;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;
import com.lowagie.text.ImgTemplate;
import com.lowagie.text.pdf.PdfTemplate;

public abstract class UIGraphics2D extends ITextComponent
{
   private Image image = null;

   private int height = 300;
   private int width = 400;

   public void setHeight(int height)
   {
      this.height = height;
   }

   public int getHeight()
   {
      return (Integer) valueBinding("height", height);
   }

   public void setWidth(int width)
   {
      this.width = width;
   }

   public int getWidth()
   {
      return (Integer) valueBinding("width", width);
   }

   @Override
   public void createITextObject(FacesContext context)
   {
      UIDocument doc = (UIDocument) findITextParent(getParent(), UIDocument.class);
      if (doc != null)
      {
         PdfTemplate tp = doc.createPdfTemplate(getWidth(), getHeight());
         Graphics2D g2 = tp.createGraphics(getWidth(), getHeight());

         render(g2);
         g2.dispose();

         try
         {
            image = new ImgTemplate(tp);
         }
         catch (BadElementException e)
         {
            throw new RuntimeException(e);
         }
      }
   }

   public abstract void render(Graphics2D g2);

   @Override
   public void handleAdd(Object arg0)
   {
      throw new RuntimeException("No children allowed");
   }

   @Override
   public void removeITextObject()
   {
      image = null;
   }

   @Override
   public Object getITextObject()
   {
      return image;
   }
}