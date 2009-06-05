package org.jboss.seam.security;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.HashMap;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.jaas.SeamLoginModule;

/**
 * Factory for the JAAS Configuration used by Seam Security.
 * 
 * @author Shane Bryzak
 *
 */
@Name("org.jboss.seam.security.configurationFactory")
@BypassInterceptors
@Scope(ScopeType.STATELESS)
@Install(precedence = BUILT_IN)
public class Configuration
{
   static final String DEFAULT_JAAS_CONFIG_NAME = "default";   

   protected javax.security.auth.login.Configuration createConfiguration()
   {
      return new javax.security.auth.login.Configuration()
      {
         private AppConfigurationEntry[] aces = { createAppConfigurationEntry() };
         
         @Override
         public AppConfigurationEntry[] getAppConfigurationEntry(String name)
         {
            return DEFAULT_JAAS_CONFIG_NAME.equals(name) ? aces : null;
         }
         
         @Override
         public void refresh() {}
      };
   }

   protected AppConfigurationEntry createAppConfigurationEntry()
   {
      return new AppConfigurationEntry( 
            SeamLoginModule.class.getName(), 
            LoginModuleControlFlag.REQUIRED, 
            new HashMap<String,String>() 
         );
   }
   
   @Factory(value="org.jboss.seam.security.configuration", autoCreate=true, scope=APPLICATION)
   public javax.security.auth.login.Configuration getConfiguration()
   {
      return createConfiguration();
   }

   public static javax.security.auth.login.Configuration instance()
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("No active application scope");
      }
      return (javax.security.auth.login.Configuration) Component.getInstance("org.jboss.seam.security.configuration");
   }
}
