package org.jboss.seam.ui.graphicImage;

import java.io.IOException;

import javax.faces.component.UIComponentBase;


public abstract class UITransformImageType extends UIComponentBase implements ImageTransform
{
   
   public void applyTransform(Image image) throws IOException
   {
      if (!isRendered())
      {
         return;
      }
      Image.Type type = Image.Type.getTypeByMimeType(getContentType());
      if (type != null)
      {
         image.setContentType(type);
      }
   }
   
   public abstract String getContentType();
   
   public abstract void setContentType(String width);
   
}
