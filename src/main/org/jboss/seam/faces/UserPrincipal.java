package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.security.Principal;

import javax.faces.context.FacesContext;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

/**
 * Manager component for the current user Principal
 * exposed via the JSF ExternalContext.
 * 
 * @author Gavin King
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Name("org.jboss.seam.web.userPrincipal")
@Install(precedence=FRAMEWORK, classDependencies="javax.faces.context.FacesContext")
public class UserPrincipal extends org.jboss.seam.web.UserPrincipal
{
   @Unwrap @Override
   public Principal getUserPrincipal()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      if ( facesContext != null ) 
      {
         return facesContext.getExternalContext().getUserPrincipal();
      }
      
      return super.getUserPrincipal();
   }
   
   public static Principal instance()
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("No active application scope");
      }
      return (Principal) Component.getInstance(UserPrincipal.class, ScopeType.APPLICATION);
   }
   
}
