package org.jboss.seam.web;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Superclass of Seam components that serve up 
 * "resources" to the client via the Seam 
 * resource servlet. Note that since a filter is
 * potentially called outside of a set of Seam
 * contexts, it is not a true Seam component. 
 * However, we are able to reuse the functionality
 * for component scanning, installation and 
 * configuration for filters. All resources
 * must extend this class.
 * 
 * @author Shane Bryzak
 *
 */
public abstract class AbstractResource
{
   private ServletContext context;
   
   protected ServletContext getServletContext()
   {
      return context;
   }
   
   public void setServletContext(ServletContext context)
   {
      this.context = context;
   }
         
   public abstract void getResource(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException;
   
   public abstract String getResourcePath();
}
