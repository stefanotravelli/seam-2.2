/**
 * 
 */
package org.jboss.seam.web;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.jboss.seam.util.EnumerationEnumeration;

public class FilterConfigWrapper implements FilterConfig
{
   
   private FilterConfig delegate;
   private Map<String, String> parameters;
   
   public FilterConfigWrapper(FilterConfig filterConfig, Map<String, String> parameters)
   {
      delegate = filterConfig;
      this.parameters = parameters;
   }

   public String getFilterName()
   {
      return delegate.getFilterName();
   }

   public String getInitParameter(String name)
   {
      if ( parameters.containsKey(name) )
      {
         return parameters.get(name);
      }
      else
      {
         return delegate.getInitParameter(name);
      }
   }

   public Enumeration getInitParameterNames()
   {
      Enumeration[] enumerations = {
               delegate.getInitParameterNames(), 
               Collections.enumeration( parameters.keySet() )
            };
      return new EnumerationEnumeration(enumerations);
   }

   public ServletContext getServletContext()
   {
      return delegate.getServletContext();
   }
   
}