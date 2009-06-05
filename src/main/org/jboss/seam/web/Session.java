package org.jboss.seam.web;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.AbstractMutable;

/**
 * Controls HttpSession invalidation in any
 * servlet or JSF environment. Since Seam
 * keeps internal state in the HttpSession,
 * it is illegal to call HttpSession.invalidate()
 * while Seam contexts are active.
 * 
 * Applications using Seam security should call
 * Identity.logout() instead of calling this
 * component directly.
 * 
 * @author Gavin King
 *
 */
@Scope(ScopeType.SESSION)
@Name("org.jboss.seam.web.session")
@BypassInterceptors
@Startup
public class Session extends AbstractMutable
{
   private boolean isInvalid;
   private boolean invalidateOnSchemeChange;
   private String currentScheme;

   /**
    * Is HttpSession invalidation scheduled
    * for the end of this request?
    */
   public boolean isInvalid()
   {
      return isInvalid;
   }

   /**
    * Schedule HttpSession invalidation at the
    * end of the request.
    *
    */
   public void invalidate()
   {
      this.isInvalid = true;
      setDirty();
   }
   
   /**
    * Should we invalidate the session due to a change in
    * the request scheme?
    * 
    * @param requestScheme the scheme of the current request
    * @return true if we should invalidate the session
    */
   public boolean isInvalidDueToNewScheme(String requestScheme)
   {
      if (invalidateOnSchemeChange)
      {
         if ( currentScheme==null )
         {
            currentScheme = requestScheme;
            setDirty();
            return false;
         }
         else if ( !currentScheme.equals(requestScheme) )
         {
            currentScheme = requestScheme;
            setDirty();
            return true;
         }
         else
         {
            return false;
         }
      }
      else
      {
         return false;
      }
   }

   /**
    * Is session invalidation on scheme change enabled?
    */
   public boolean isInvalidateOnSchemeChange()
   {
      return invalidateOnSchemeChange;
   }

   /**
    * Enable or disable session invalidation on scheme change?
    */
   public void setInvalidateOnSchemeChange(boolean invalidateOnSchemeChange)
   {
      setDirty();
      this.invalidateOnSchemeChange = invalidateOnSchemeChange;
   }
   
   public static Session instance()
   {
      if ( !Contexts.isSessionContextActive() )
      {
         throw new IllegalStateException("No active session context");
      }
      return (Session) Component.getInstance(Session.class, ScopeType.SESSION);
   }

   public static Session getInstance()
   {
      if ( !Contexts.isSessionContextActive() )
      {
         throw new IllegalStateException("No active session context");
      }
      return (Session) Component.getInstance(Session.class, ScopeType.SESSION, false);
   }

}
