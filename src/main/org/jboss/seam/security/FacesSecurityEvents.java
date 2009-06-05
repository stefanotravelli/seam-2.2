package org.jboss.seam.security;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import javax.security.auth.login.LoginException;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.international.StatusMessage.Severity;

/**
 * Produces FacesMessages in response of certain security events, and helps to decouple the
 * Identity component from JSF.
 * 
 * @author Shane Bryzak
 */
@Name("org.jboss.seam.security.facesSecurityEvents")
@Scope(APPLICATION)
@Install(precedence = BUILT_IN, classDependencies = "javax.faces.context.FacesContext")
@BypassInterceptors
@Startup
public class FacesSecurityEvents 
{  
   @Observer(Identity.EVENT_POST_AUTHENTICATE)
   public void postAuthenticate(Identity identity)
   {         
      //org.jboss.security.saml.SSOManager.processManualLoginNotification(
            //ServletContexts.instance().getRequest(), identity.getPrincipal().getName());
   }
   
   @Observer(Identity.EVENT_LOGIN_FAILED)
   public void addLoginFailedMessage(LoginException ex)
   {
      StatusMessages.instance().addFromResourceBundleOrDefault(
               getLoginFailedMessageSeverity(), 
               getLoginFailedMessageKey(), 
               getLoginFailedMessage(), 
               ex);
   }

   public String getLoginFailedMessage()
   {
      return "Login failed";
   }

   public Severity getLoginFailedMessageSeverity()
   {
      return Severity.INFO;
   }

   public String getLoginFailedMessageKey()
   {
      return "org.jboss.seam.loginFailed";
   }

   @Observer(Identity.EVENT_LOGIN_SUCCESSFUL)
   public void addLoginSuccessfulMessage()
   {
      StatusMessages.instance().addFromResourceBundleOrDefault(
               getLoginSuccessfulMessageSeverity(), 
               getLoginSuccessfulMessageKey(), 
               getLoginSuccessfulMessage(), 
               Identity.instance().getCredentials().getUsername());
   }
   
   @Observer(Identity.EVENT_NOT_LOGGED_IN)
   public void addNotLoggedInMessage()
   {      
      StatusMessages.instance().addFromResourceBundleOrDefault( 
            Severity.WARN, 
            "org.jboss.seam.NotLoggedIn", 
            "Please log in first" 
         );      
   }

   public Severity getLoginSuccessfulMessageSeverity()
   {
      return Severity.INFO;
   }

   public String getLoginSuccessfulMessage()
   {
      return "Welcome, #0";
   }

   public String getLoginSuccessfulMessageKey()
   {
      return "org.jboss.seam.loginSuccessful";
   }   
   
   @Observer(Identity.EVENT_ALREADY_LOGGED_IN)
   public void addAlreadyLoggedInMessage()
   {
      StatusMessages.instance().addFromResourceBundleOrDefault (
         Severity.WARN,
         "org.jboss.seam.AlreadyLoggedIn",
         "You are already logged in, please log out first if you wish to log in again"
      );
   }
}
