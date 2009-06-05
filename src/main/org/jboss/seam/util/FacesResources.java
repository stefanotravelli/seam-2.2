package org.jboss.seam.util;

import java.io.InputStream;
import java.net.URL;

import javax.faces.context.ExternalContext;

public class FacesResources 
{

   public static InputStream getResourceAsStream(String resource, ExternalContext context) 
   {
      String stripped = resource.startsWith("/") ? 
            resource.substring(1) : resource;
   
      InputStream stream = null; 

      try
      {
         if (context!=null)
         {
            stream = context.getResourceAsStream(resource);
         }
      }
      catch (Exception e) {}
      
      if (stream==null)
      {
         stream = Resources.getResourceAsStream(resource, stripped);
      }
      
      return stream;
   }

   public static URL getResource(String resource, ExternalContext context) 
   {
      String stripped = resource.startsWith("/") ? 
               resource.substring(1) : resource;
      
         URL url = null; 

         try
         {
            if (context!=null)
            {
               url = context.getResource(resource);
            }
         }
         catch (Exception e) {}
         
         if (url==null)
         {
            url = Resources.getResource(resource, stripped);
         }
         
         return url;
   }
   
}
