package org.jboss.seam.ui.facelet;

import static org.jboss.seam.ScopeType.APPLICATION;

import javax.servlet.ServletContext;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.mock.MockServletContext;

@Name("org.jboss.seam.ui.facelet.mockServletContext")
@Scope(APPLICATION)
@BypassInterceptors
@Install(dependencies="org.jboss.seam.faces.renderer")
@AutoCreate
public class ServletContextManager
{
   
   private ServletContext servletContext;
   
   @Create
   public void create()
   {
      // TODO A bit of a hack, we should store the servlet context properly
      if (ServletLifecycle.getCurrentServletContext() != null)
      {
         servletContext = ServletLifecycle.getCurrentServletContext();
      }
      else
      {
         this.servletContext = new MockServletContext();
      }
   }
   
   @Unwrap
   public ServletContext getServletContext()
   {
      return servletContext;
   }
   
   public static ServletContext instance()
   {
      if (!Contexts.isApplicationContextActive())
      {
         throw new IllegalStateException("Application context is not active");
      }
      return (ServletContext) Component.getInstance(ServletContextManager.class, APPLICATION);
   }

}
