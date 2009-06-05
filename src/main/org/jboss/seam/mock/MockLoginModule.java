package org.jboss.seam.mock;

import javax.security.auth.login.LoginException;

import org.jboss.seam.security.Identity;
import org.jboss.seam.security.jaas.SeamLoginModule;

public class MockLoginModule extends SeamLoginModule 
{
   @Override
   public boolean login() throws LoginException
   {
      Identity.instance().addRole("foo");
      
      return true;
   }
   
}