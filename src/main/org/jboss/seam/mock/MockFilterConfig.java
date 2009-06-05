package org.jboss.seam.mock;

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.jboss.seam.util.IteratorEnumeration;

/**
 * @author Gavin King
 */
public class MockFilterConfig implements FilterConfig
{
   private ServletContext servletContext;
   
   public MockFilterConfig(ServletContext servletContext)
   {
      this.servletContext = servletContext;
   }
   
   public String getFilterName()
   {
      return "Seam Filter";
   }

   public ServletContext getServletContext()
   {
      return servletContext;
   }

   public String getInitParameter(String name)
   {
      return null;
   }

   public Enumeration getInitParameterNames()
   {
      return new IteratorEnumeration( Collections.EMPTY_LIST.iterator() );
   }
}