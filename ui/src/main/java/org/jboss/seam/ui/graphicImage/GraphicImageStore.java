package org.jboss.seam.ui.graphicImage;

import java.io.Serializable;
import java.rmi.server.UID;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.AbstractMutable;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

@Name("org.jboss.seam.ui.graphicImage.graphicImageStore")
@Scope(ScopeType.SESSION)
@Install(precedence = Install.BUILT_IN)
@BypassInterceptors
public class GraphicImageStore extends AbstractMutable implements Serializable
{
   private Log log = Logging.getLog(GraphicImageStore.class);

   public static class ImageWrapper implements Serializable
   {
      
      private byte[] image;

      private Image.Type contentType;

      public ImageWrapper(byte[] image, Image.Type contentType)
      {
         this.image = image;
         this.contentType = contentType;
      }

      public Image.Type getContentType()
      {
         return contentType;
      }

      public byte[] getImage()
      {
         return image;
      }
   }

   private Map<String, ImageWrapper> store = new HashMap<String, ImageWrapper>();

   /**
    * Put a image into the store
    * @param image
    * @return the key of the image
    */
   public String put(ImageWrapper image)
   {
      return put(image, null);
   }
   
   /**
    * Put an image into the store.
    * @param image
    * @param key The key to use, if null, a key will be generated
    * @return The key of the image
    */
   public String put(ImageWrapper image, String key) 
   {
      if (key == null)
      {
         key = "org.jboss.seam.ui.GraphicImageStore." + new UID().toString().replace(":", "-");
      }
      store.put(key, image);
      setDirty();
      log.debug("Put image into to session with key #0", key);
      return key;
   }

   public ImageWrapper get(String key)
   {
      log.debug("Get image into to session with key #0", key);
      ImageWrapper image = store.get(key);
      return image;
   }
   
   public ImageWrapper remove(String key)
   {
      log.debug("Get image from session with key #0", key);
      ImageWrapper imageWrapper = store.remove(key);
      if ( imageWrapper != null )
      {
         setDirty();
      }
      return imageWrapper;
   }
   
   public boolean contains(String key) 
   {
      return store.containsKey(key);
   }

   public static GraphicImageStore instance()
   {
      return (GraphicImageStore) Component.getInstance(GraphicImageStore.class);
   }

}