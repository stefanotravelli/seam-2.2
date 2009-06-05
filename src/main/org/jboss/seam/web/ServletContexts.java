/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.web;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

/**
 * A Seam component that binds the HttpServletRequest object
 * to the current thread.
 * 
 * @author Gavin King
 */
@Scope(ScopeType.EVENT)
@Name("org.jboss.seam.web.servletContexts")
@BypassInterceptors
@Install(precedence=BUILT_IN)
public class ServletContexts 
{
   
   private HttpServletRequest request;
   
   public static ServletContexts instance()
   {
      if ( !Contexts.isEventContextActive() ) 
      {
         throw new IllegalStateException("no event context active");
      }
      return (ServletContexts) Component.getInstance(ServletContexts.class, ScopeType.EVENT);
   }
   
   public static ServletContexts getInstance()
   {
      return Contexts.isEventContextActive() ? 
               (ServletContexts) Component.getInstance(ServletContexts.class, ScopeType.EVENT) : null;
   }

   public HttpServletRequest getRequest()
   {
      return request;
   }

   public void setRequest(HttpServletRequest request)
   {
      this.request = request;
   }
   
}
