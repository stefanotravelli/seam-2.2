package org.jboss.seam.security.jaas;

import static org.jboss.seam.security.Identity.ROLES_GROUP;

import java.security.acl.Group;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.jboss.seam.core.Expressions.MethodExpression;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.SimpleGroup;
import org.jboss.seam.security.SimplePrincipal;
import org.jboss.seam.security.management.IdentityManager;

/**
 * Performs authentication using a Seam component
 * 
 * @author Shane Bryzak
 */
public class SeamLoginModule implements LoginModule
{   
   private static final LogProvider log = Logging.getLogProvider(SeamLoginModule.class);   
   
   protected Set<String> roles = new HashSet<String>();
   
   protected Subject subject;
   protected Map<String,?> options;
   protected CallbackHandler callbackHandler;
   
   protected String username;
   
   public boolean abort() throws LoginException
   {
      return true;
   }

   public boolean commit() throws LoginException
   {        
      subject.getPrincipals().add(new SimplePrincipal(username));
      
      Group roleGroup = null;
      
      for ( Group g : subject.getPrincipals(Group.class) )      
      {
         if ( ROLES_GROUP.equalsIgnoreCase( g.getName() ) )
         {
            roleGroup = g;
            break;
         }
      }

      if (roleGroup == null) roleGroup = new SimpleGroup(ROLES_GROUP);

      for (String role : roles)
      {
         roleGroup.addMember(new SimplePrincipal(role));
      }
      
      subject.getPrincipals().add(roleGroup);
      
      return true;
   }

   public void initialize(Subject subject, CallbackHandler callbackHandler,
         Map<String, ?> sharedState, Map<String, ?> options)
   {
      this.subject = subject;
      this.options = options;
      this.callbackHandler = callbackHandler;
   }

   public boolean login() 
      throws LoginException
   {
      try
      {
         NameCallback cbName = new NameCallback("Enter username");
         PasswordCallback cbPassword = new PasswordCallback("Enter password", false);
   
         // Get the username and password from the callback handler
         callbackHandler.handle(new Callback[] { cbName, cbPassword });
         username = cbName.getName();
      }
      catch (Exception ex)
      {
         log.warn("Error logging in", ex);
         LoginException le = new LoginException(ex.getMessage());
         le.initCause(ex);
         throw le;
      }
      
      // If an authentication method has been specified, use that to authenticate
      MethodExpression mb = Identity.instance().getAuthenticateMethod();
      if (mb != null)
      {
         try
         {
           return (Boolean) mb.invoke();      
         }
         catch (Exception ex)
         {
            log.warn("Error invoking login method", ex);            
            LoginException le = new LoginException(ex.getMessage());
            le.initCause(ex);            
            throw le;
         }
      }
      
      // Otherwise if identity management is enabled, use it.
      IdentityManager identityManager = IdentityManager.instance();
      if (identityManager != null && identityManager.isEnabled())
      {
         Identity identity = Identity.instance();
         
         try
         {
            boolean success = identityManager.authenticate(username, identity.getCredentials().getPassword());
            
            if (success)
            {
               for (String role : identityManager.getImpliedRoles(username))
               {
                  identity.addRole(role);
               }
            }
            
            return success;
         }
         catch (Exception ex)
         {
            log.warn("Error invoking login method");
            LoginException le = new LoginException(ex.getMessage());
            le.initCause(ex);
            throw le;
         }
      }
      else
      {
         log.error("No authentication method defined - " +
               "please define authenticate-method for <security:identity/> in components.xml");
         throw new LoginException("No authentication method defined");
      }

   }

   public boolean logout() throws LoginException
   {
      return true;
   }
}
