/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam;

import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;

/**
 * The scopes defined by Seam.
 * 
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 */
public enum ScopeType
{
   /**
    * The stateless pseudo-context.
    */
   STATELESS,
   /**
    * The method context. Each call to a session bean or 
    * JavaBean component puts a new method context onto
    * the stack of method contexts associated with the
    * current thread. The context is destroyed (and the
    * stack popped) when the method returns.
    */
   METHOD,
   /**
    * The event (request) context. Spans a server request,
    * from restore view to render response.
    */
   EVENT,
   /**
    * The page context. Begins during the invoke application
    * phase prior to rendering a page, and lasts until the end 
    * of any invoke application phase of a faces request 
    * originating from that page. Non-faces requests do not
    * propagate the page scope.
    */
   PAGE,
   /**
    * The conversation context. Spans multiple requests from
    * the same browser window, demarcated by @Begin and @End
    * methods. A conversation context is propagated by
    * any faces request, or by any request that specifies
    * a conversation id as a request parameter. The conversation
    * context is not available during the restore view phase.
    */
   CONVERSATION,
   /**
    * The session context. (A servlet login session.)
    */
   SESSION,
   /**
    * The application context (Servlet context.)
    */
   APPLICATION,
   /**
    * The business process context. Spans multiple conversations
    * with multiple users, demarcated by the start and end states
    * of the business process definition.
    */
   BUSINESS_PROCESS,
   /**
    * Indicates that the scope is implied.
    */
   UNSPECIFIED;
   
   private final String prefix;

   private ScopeType()
   {
      prefix = "org.jboss.seam." + toString();
   }
   
   public boolean isContextActive()
   {
      switch (this)
      {
         case STATELESS:
            return true;
         case METHOD:
            return Contexts.isMethodContextActive();
         case EVENT:
            return Contexts.isEventContextActive();
         case PAGE:
            return Contexts.isPageContextActive();
         case CONVERSATION:
            return Contexts.isConversationContextActive();
         case SESSION:
            return Contexts.isSessionContextActive();
         case APPLICATION:
            return Contexts.isApplicationContextActive();
         case BUSINESS_PROCESS:
            return Contexts.isBusinessProcessContextActive();
         default: 
            throw new IllegalArgumentException();
      }
   }
   
   /**
    * @return the Context object for this scope
    */
   public Context getContext() {
      switch (this)
      {
         case STATELESS: 
            throw new UnsupportedOperationException("Stateless pseudo-scope does not have a Context object");
         case METHOD: 
            if ( !Contexts.isMethodContextActive() )
            {
               throw new IllegalStateException("No method context active");
            }
            return Contexts.getMethodContext();
         case EVENT: 
            if ( !Contexts.isEventContextActive() )
            {
               throw new IllegalStateException("No event context active");
            }
            return Contexts.getEventContext();
         case PAGE:
            if ( !Contexts.isPageContextActive() )
            {
               throw new IllegalStateException("No page context active");
            }
            return Contexts.getPageContext();
         case CONVERSATION: 
            if ( !Contexts.isConversationContextActive() )
            {
               throw new IllegalStateException("No conversation context active");
            }
            return Contexts.getConversationContext();
         case SESSION: 
            if ( !Contexts.isSessionContextActive() )
            {
               throw new IllegalStateException("No session context active");
            }
             return Contexts.getSessionContext();
         case APPLICATION: 
            if ( !Contexts.isApplicationContextActive() )
            {
               throw new IllegalStateException("No application context active");
            }
             return Contexts.getApplicationContext();
         case BUSINESS_PROCESS: 
            if ( !Contexts.isBusinessProcessContextActive() )
            {
               throw new IllegalStateException("No process context active");
            }
             return Contexts.getBusinessProcessContext();
         default: 
            throw new IllegalArgumentException();
      }
   }
   
   public String getPrefix()
   {
      return prefix;
   }

}


