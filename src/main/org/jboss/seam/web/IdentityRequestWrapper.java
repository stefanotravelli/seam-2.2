package org.jboss.seam.web;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.jboss.seam.Seam;
import org.jboss.seam.security.Identity;

/**
 * An HttpServletRequestWrapper implementation that provides integration
 * between Servlet Security and the Seam identity component.
 *
 * @author Dan Allen
 */
public class IdentityRequestWrapper extends HttpServletRequestWrapper {

   private Identity identity;

   public IdentityRequestWrapper(HttpServletRequest request) {
      super(request);
      identity = (Identity) request.getSession().
         getAttribute(Seam.getComponentName(Identity.class));
   }

   @Override
   public String getRemoteUser() {
      return getUserPrincipal() != null ? getUserPrincipal().getName() : null;
   }

   @Override
   public Principal getUserPrincipal() 
   {
      return seamSecurityIsActive() ? identity.getPrincipal() : super.getUserPrincipal();
   }

   @Override
   public boolean isUserInRole(String role) {
      return seamSecurityIsActive() ? identity.hasRole(role) : super.isUserInRole(role);
   }
   
   private boolean seamSecurityIsActive()
   {
      return Identity.isSecurityEnabled() && identity != null;
   }
}
