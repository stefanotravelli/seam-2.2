package org.jboss.seam.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.NotLoggedInException;

/**
 * An authorization strategy for Wicket backed by Seam Security
 * 
 * @author pmuir
 *
 */
public class SeamAuthorizationStrategy implements IAuthorizationStrategy
{
   
   private LogProvider log = Logging.getLogProvider(SeamAuthorizationStrategy.class);
   private Class loginPage;

   /**
    * Create the strategy, specifying the page to use for login
    * 
    * @param loginPage
    */
   public SeamAuthorizationStrategy(final Class loginPage)
   {
      this.loginPage = loginPage;
   }
   
   // TODO Use permission schemes for this?
   public boolean isActionAuthorized(Component component, Action action)
   {
      return isInstantiationAuthorized(component.getClass());
   }

   /**
    * Check whether access is allowed to the given wicket component.
    * 
    * Uses the @Restrict annotation to control access
    */
   public boolean isInstantiationAuthorized(Class componentClass)
   {
      try
      {
         WicketComponent instance = WicketComponent.getInstance(componentClass);
         if (instance != null) instance.checkRestrictions();
      }
      catch (NotLoggedInException e) 
      {
         log.error("Unauthorized access to " + componentClass.getName() + ", user not logged in", e);
         return handleException(componentClass);
      }
      catch (org.jboss.seam.security.AuthorizationException e) 
      {
         return false;
      }

      return true;
   }

   private boolean handleException(Class componentClass)
   {
      if (Page.class.isAssignableFrom(componentClass))
      {
         // Redirect to page to let the user sign in
         throw new RestartResponseAtInterceptPageException(loginPage);
      }
      return false;
   }
   
   
}
