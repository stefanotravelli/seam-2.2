package org.jboss.seam.web;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import javax.servlet.ServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Manager component for the current locale that is
 * aware of the HTTP request locale
 * 
 * @author Gavin King
 */
@Scope(ScopeType.STATELESS)
@Name("org.jboss.seam.core.locale")
@Install(precedence=FRAMEWORK-1)
@BypassInterceptors
public class Locale extends org.jboss.seam.core.Locale
{

   @Unwrap @Override
   public java.util.Locale getLocale()
   {
      ServletContexts servletContexts = ServletContexts.getInstance();
      ServletRequest request = servletContexts==null ? null : servletContexts.getRequest();
      return request==null ? super.getLocale() : request.getLocale();
   }
   
}