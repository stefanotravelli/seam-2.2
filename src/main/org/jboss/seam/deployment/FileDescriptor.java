package org.jboss.seam.deployment;

import java.net.URL;

import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.util.Resources;

public class FileDescriptor
{
   
   private String name;
   private URL url;
   
   public FileDescriptor(String name, URL url)
   {
      this.name = name;
      this.url = url;
   }
   
   public FileDescriptor(String name, ClassLoader classLoader)
   {
      this.name = name;
      if (name == null)
      {
         throw new NullPointerException("Name cannot be null, loading from " + classLoader);
      }
      this.url = classLoader.getResource(name);
      if (url == null)
      {
         this.url = Resources.getResource(name, ServletLifecycle.getServletContext());
      }
      if (this.url == null)
      {
         throw new NullPointerException("Cannot find URL from classLoader for " + name + ", loading from " + classLoader);
      }
   }

   public String getName()
   {
      return name;
   }
   
   public URL getUrl()
   {
      return url;
   }
   
   @Override
   public String toString()
   {
      return url.getPath();
   }
   
   @Override
   public boolean equals(Object other)
   {
      if (other instanceof FileDescriptor)
      {
         FileDescriptor that = (FileDescriptor) other;
         return this.getUrl().equals(that.getUrl());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      return getUrl().hashCode();
   }
   
}
