package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.io.InputStream;
import java.net.URL;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.FacesResources;

/**
 * Access to application resources in tye JSF environment.
 * 
 * @author Gavin King
 *
 */
@Scope(ScopeType.STATELESS)
@BypassInterceptors
@Install(precedence=FRAMEWORK, classDependencies="javax.faces.context.FacesContext")
@Name("org.jboss.seam.core.resourceLoader")
public class ResourceLoader extends org.jboss.seam.core.ResourceLoader
{
   
   @Override
   public InputStream getResourceAsStream(String resource)
   {
      javax.faces.context.FacesContext context = javax.faces.context.FacesContext.getCurrentInstance();
      if (context!=null)
      {
         return FacesResources.getResourceAsStream( resource, context.getExternalContext() );
      }
      else
      {
         return super.getResourceAsStream(resource);
      }
   }

   @Override
   public URL getResource(String resource) 
   {
      javax.faces.context.FacesContext context = javax.faces.context.FacesContext.getCurrentInstance();
      if (context!=null)
      {
         return FacesResources.getResource( resource, context.getExternalContext() );
      }
      else
      {
         return super.getResource(resource);
      }
   }
   
}
