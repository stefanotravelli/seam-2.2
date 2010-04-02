package org.jboss.seam.test.unit.web;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.seam.Seam;
import org.jboss.seam.mock.MockHttpServletRequest;
import org.jboss.seam.mock.MockHttpSession;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.SimplePrincipal;
import org.jboss.seam.web.IdentityRequestWrapper;
import org.testng.annotations.Test;

public class IdentityRequestWrapperTest
{
   private static final String JAAS_USER = "jaasUser";
   
   private static final String JAAS_ROLE = "jaasRole";
   
   private static final String SEAM_USER = "seamUser";
   
   private static final String SEAM_ROLE = "seamRole";
   
   @Test
   public void testWithSeamSecurityEnabled()
   {
      HttpServletRequest request = initializeWrappedRequest();
      if (!Identity.isSecurityEnabled())
      {
         Identity.setSecurityEnabled(true);
      }
      assert request.getUserPrincipal() != null && request.getUserPrincipal().getName().equals(SEAM_USER);
      assert request.getRemoteUser() != null && request.getRemoteUser().equals(SEAM_USER);
      assert request.isUserInRole(SEAM_ROLE);
   }
   
   @Test
   public void testWithSeamSecurityDisabled()
   {
      HttpServletRequest request = initializeWrappedRequest();
      Identity.setSecurityEnabled(false);
      assert request.getUserPrincipal() != null && request.getUserPrincipal().getName().equals(JAAS_USER);
      assert request.getRemoteUser() != null && request.getRemoteUser().equals(JAAS_USER);
      assert request.isUserInRole(JAAS_ROLE);
   }
   
   public HttpServletRequest initializeWrappedRequest() {
      HttpSession session = new MockHttpSession();
      Identity identity = new Identity() {

         @Override
         public Principal getPrincipal()
         {
            return new SimplePrincipal(SEAM_USER);
         }

         @Override
         public boolean hasRole(String role)
         {
            return SEAM_ROLE.equals(role);
         }
         
      };
      session.setAttribute(Seam.getComponentName(Identity.class), identity);
      HttpServletRequest request = new MockHttpServletRequest(session, JAAS_USER, new HashSet<String>(Arrays.asList(JAAS_ROLE)), null, "GET");
      return new IdentityRequestWrapper(request);
   }
   
}
