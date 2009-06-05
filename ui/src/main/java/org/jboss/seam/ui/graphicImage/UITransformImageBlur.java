package org.jboss.seam.ui.graphicImage;

import java.io.IOException;

import javax.faces.component.UIComponentBase;


public abstract class UITransformImageBlur extends UIComponentBase implements ImageTransform
{
   
   public void applyTransform(Image image) throws IOException
   {
      if (!isRendered())
      {
         return;
      }
      image.blur(new Integer(getRadius()));
   }
   
   public abstract String getRadius();
   
   public abstract void setRadius(String width);
   
  
   
}
