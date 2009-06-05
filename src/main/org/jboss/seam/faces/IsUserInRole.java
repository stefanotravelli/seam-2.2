package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Manager component for a map of roles assigned
 * to the current user, as exposed via the JSF
 * ExternalContext.
 * 
 * @author Gavin King
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Name("org.jboss.seam.web.isUserInRole")
@Install(precedence=FRAMEWORK, classDependencies="javax.faces.context.FacesContext")
public class IsUserInRole extends org.jboss.seam.web.IsUserInRole
{
   @Override
   protected Boolean isUserInRole(String role)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      if ( facesContext != null ) 
      {
         return facesContext.getExternalContext().isUserInRole(role);
      }
      
      return super.isUserInRole(role);
   }
}
