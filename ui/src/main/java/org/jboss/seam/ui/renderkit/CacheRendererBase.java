package org.jboss.seam.ui.renderkit;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.ui.component.UICache;
import org.jboss.seam.ui.util.cdk.RendererBase;

public class CacheRendererBase extends RendererBase {

   private static final LogProvider log = Logging.getLogProvider(UICache.class);

   /**
    * last time we logged the failure of the cache
    */
   private static Calendar lastLog = null;

   @Override
   protected Class getComponentClass()
   {
      return UICache.class;
   }

   @Override
   protected void doEncodeChildren(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      UICache cache = (UICache) component;
      if (cache.isEnabled())
      {
         String key = cache.getKey();
         String cachedContent = (String) cache.getCacheProvider().get(cache.getRegion(), key); 
         if (cachedContent == null)
         {
            log.debug("rendering from scratch: " + key);
            StringWriter stringWriter = new StringWriter();
            ResponseWriter cachingResponseWriter = writer.cloneWithWriter(stringWriter);
            context.setResponseWriter(cachingResponseWriter);
            renderChildren(context, component);
            context.setResponseWriter(writer);
            String output = stringWriter.getBuffer().toString();
            writer.write(output);
            cache.getCacheProvider().put(cache.getRegion(), key, output);
         }
         else
         {
            log.debug("rendering from cache: " + key);
            writer.write(cachedContent);
         }
      }
      else
      {
         renderChildren(context, component);
      }
   }

   @Override
   public boolean getRendersChildren()
   {
      return true;
   }

}
