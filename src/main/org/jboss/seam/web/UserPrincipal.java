package org.jboss.seam.web;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.security.Principal;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

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
@Install(precedence=BUILT_IN)
public class UserPrincipal
{
   @Unwrap
   public Principal getUserPrincipal()
   {
      ServletRequest servletRequest = ServletContexts.instance().getRequest();
      if ( servletRequest != null )
      {
         return ( (HttpServletRequest) servletRequest ).getUserPrincipal();
      }
      
      return null;
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
