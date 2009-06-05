package org.jboss.seam.ui.facelet;

import static org.jboss.seam.ScopeType.APPLICATION;

import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;


/**
 * A copy of the FacesServlet for use in our Renderer.
 * 
 * Shamelessly adapted from the RI
 * 
 * @author Pete Muir
 *
 */

@Name("org.jboss.seam.ui.facelet.facesContextFactory")
@Scope(APPLICATION)
@BypassInterceptors
@Install(dependencies="org.jboss.seam.faces.renderer")
@AutoCreate
public class RendererFacesContextFactory
{

   private javax.faces.context.FacesContextFactory facesContextFactory;
   private Lifecycle lifecycle;
   
   @Create
   public void create()
   {
      // Acquire our FacesContextFactory instance
      facesContextFactory = (javax.faces.context.FacesContextFactory) FactoryFinder.getFactory (FactoryFinder.FACES_CONTEXT_FACTORY);

      // Acquire our Lifecycle instance
      LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
      lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
   }

   public FacesContext getFacesContext(ServletRequest request, ServletResponse response)
   {
      return facesContextFactory.getFacesContext(ServletContextManager.instance(), request, response, lifecycle);
   }
   
   @Destroy
   public void destroy()
   {
      facesContextFactory = null;
      lifecycle = null;
   }

   public static RendererFacesContextFactory instance()
   {
      if (!Contexts.isApplicationContextActive())
      {
         throw new IllegalStateException("Application context is not active");
      }
      return (RendererFacesContextFactory) Component.getInstance(RendererFacesContextFactory.class, APPLICATION);
   }
   
}
