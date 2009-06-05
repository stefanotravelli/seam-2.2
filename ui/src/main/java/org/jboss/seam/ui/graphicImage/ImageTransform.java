package org.jboss.seam.ui.graphicImage;

import java.io.IOException;


public interface ImageTransform
{
   public static final String FAMILY = "org.jboss.seam.ui.UIImageTransform";
   
   public abstract void applyTransform(Image image) throws IOException;
}
