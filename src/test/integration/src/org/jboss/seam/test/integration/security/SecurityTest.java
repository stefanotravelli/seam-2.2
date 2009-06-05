package org.jboss.seam.test.integration.security;

import java.util.HashMap;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.mock.AbstractSeamTest;
import org.jboss.seam.mock.MockLoginModule;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;
import org.jboss.seam.web.Session;
import org.testng.annotations.Test;

/**
 * Seam Security Unit Tests
 * 
 * @author Shane Bryzak
 */
public class SecurityTest extends AbstractSeamTest
{     
   private Configuration createMockJAASConfiguration()
   {
      return new Configuration()
      {
         private AppConfigurationEntry[] aces = { new AppConfigurationEntry( 
               MockLoginModule.class.getName(), 
               LoginModuleControlFlag.REQUIRED, 
               new HashMap<String,String>() 
            ) };
         
         @Override
         public AppConfigurationEntry[] getAppConfigurationEntry(String name)
         {
            return aces;
         }
         
         @Override
         public void refresh() {}
      };      
   }
   
   public class MockIdentity extends Identity
   {
      @Override
      protected LoginContext getLoginContext() throws LoginException
      {
         return new LoginContext("default", getSubject(), getCredentials().createCallbackHandler(), 
               createMockJAASConfiguration());
      }            
   }

   @Test
   public void testLogin()
   {      
      try
      {
         Lifecycle.beginApplication(new HashMap<String,Object>());
         Lifecycle.beginCall();
         
         // Create a mock session
         Contexts.getSessionContext().set(Component.getComponentName(Session.class), new Session());
         
         Identity identity = new MockIdentity();
         identity.create();
         
         // Put the identity into our session context 
         Contexts.getSessionContext().set(Component.getComponentName(Identity.class), identity);         
         
         // Test addRole()
         identity.addRole("admin");
         
         assert(!identity.hasRole("admin"));
         
         try
         {
            // This should throw a NotLoggedInException
            identity.checkRole("admin");
            assert(false);
         }
         catch (NotLoggedInException ex)
         {
            // expected
         }         
                  
         identity.getCredentials().setUsername("foo");
         identity.getCredentials().setPassword("bar");
         
         assert("foo".equals(identity.getCredentials().getUsername()));
         assert("bar".equals(identity.getCredentials().getPassword()));
         
         assert("loggedIn".equals(identity.login()));
         assert(identity.isLoggedIn());
         
         // Pre-authenticated roles are cleared before authenticating, 
         // so this should still return false
         assert(!identity.hasRole("admin"));
         
         // The foo role is added by MockLoginModule
         assert(identity.hasRole("foo"));
         
         identity.removeRole("foo");
         assert(!identity.hasRole("foo"));
         
         try
         {
            // This should throw an AuthorizationException
            identity.checkRole("foo");
            assert(false);
         }
         catch (AuthorizationException ex)
         {
            // expected
         }
         
         // Now that we're authenticated, adding a role should have an immediate effect
         identity.addRole("admin");
         assert(identity.hasRole("admin"));
                  
         identity.logout();
         
         assert(!identity.hasRole("admin"));         
         assert(!identity.isLoggedIn());
      }
      finally
      {
         Lifecycle.endApplication();
      }
   }
   
   @Test
   public void testDisableSecurity()
   {
      try
      {      
         Identity identity = new Identity();
         identity.create();
         
         // Disable security
         Identity.setSecurityEnabled(false);
         
         assert(!Identity.isSecurityEnabled());
         assert(identity.hasRole("admin"));
         assert(identity.hasPermission("foo", "bar"));
   
         // This shouldn't throw an exception while security is disabled
         identity.checkRestriction("foo");
         
         // Enable security
         Identity.setSecurityEnabled(true);
         assert(Identity.isSecurityEnabled());
         assert(!identity.hasRole("admin"));
         assert(!identity.hasPermission("foo", "bar"));
      }
      finally
      {
         Identity.setSecurityEnabled(true);
      }      
   }

}
