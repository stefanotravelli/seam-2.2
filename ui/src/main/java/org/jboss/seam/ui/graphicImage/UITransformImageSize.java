package org.jboss.seam.ui.graphicImage;

import java.io.IOException;

import javax.faces.component.UIComponentBase;


public abstract class UITransformImageSize extends UIComponentBase implements ImageTransform
{

   public void applyTransform(Image image) throws IOException
   {
      if (!isRendered())
      {
         return;
      }
      // TODO reduce number of decimal places
      if (isMaintainRatio())
      {
         if (getWidth() != null && getHeight() != null)
         {
            throw new UnsupportedOperationException(
                     "Cannot maintain ratio and specify height and width");
         }
         else if (getWidth() != null)
         {
            image.scaleToWidth(new Integer(getWidth()));
         }
         else if (getHeight() != null)
         {
            image.scaleToHeight(new Integer(getHeight()));
         }
      }
      else if (getFactor() != null)
      {
         if (getWidth() != null || getHeight() != null)
         {
            throw new UnsupportedOperationException(
                     "Cannot scale by a factor and specify height and width");
         }
         image.scale(getFactor());
      }
      else
      {
         if (getWidth() == null || getHeight() == null)
         {
            throw new UnsupportedOperationException(
            "If not specifying a factor or maintain ratio you must specify width and heigh");
         }
         image.resize(new Integer(getWidth()), new Integer(getHeight()));
      }
   }

   public abstract boolean isMaintainRatio();

   public abstract void setMaintainRatio(boolean maintainRatio);

   public abstract Integer getWidth();

   public abstract void setWidth(Integer width);

   public abstract Integer getHeight();

   public abstract void setHeight(Integer height);

   public abstract Double getFactor();

   public abstract void setFactor(Double factor);

}
