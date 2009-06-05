/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.contexts;

import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Init;
import org.jboss.seam.core.Manager;
import org.jboss.seam.core.Mutable;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.transaction.Transaction;
import org.jboss.seam.web.Session;

/**
 * Provides access to the current contexts associated with the thread.
 * 
 * @author Gavin King
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 */
public class Contexts 
{

   private static final LogProvider log = Logging.getLogProvider(Contexts.class);

   static final ThreadLocal<Context> applicationContext = new ThreadLocal<Context>();
   static final ThreadLocal<Context> methodContext = new ThreadLocal<Context>();
   static final ThreadLocal<Context> eventContext = new ThreadLocal<Context>();
   static final ThreadLocal<Context> pageContext = new ThreadLocal<Context>();
   static final ThreadLocal<Context> sessionContext = new ThreadLocal<Context>();
   static final ThreadLocal<Context> conversationContext = new ThreadLocal<Context>();
   static final ThreadLocal<Context> businessProcessContext = new ThreadLocal<Context>();

   public static Context getEventContext() 
   {
      return eventContext.get();
   }

   public static Context getMethodContext() 
   {
      return methodContext.get();
   }

   public static Context getPageContext() 
   {
      return pageContext.get();
   }

   public static Context getSessionContext() 
   {
      return sessionContext.get();
   }

   public static Context getApplicationContext() 
   {
      return applicationContext.get();
   }

   public static Context getConversationContext() 
   {
      return conversationContext.get();
   }

   public static Context getBusinessProcessContext() 
   {
      return businessProcessContext.get();
   }

   public static boolean isConversationContextActive() 
   {
      return getConversationContext() != null;
   }

   public static boolean isEventContextActive() 
   {
      return eventContext.get() != null;
   }

   public static boolean isMethodContextActive() 
   {
      return methodContext.get() != null;
   }

   public static boolean isPageContextActive() 
   {
      return pageContext.get() != null;
   }

   public static boolean isSessionContextActive() 
   {
      return sessionContext.get() != null;
   }

   public static boolean isApplicationContextActive() 
   {
      return applicationContext.get() != null;
   }

    public static boolean isBusinessProcessContextActive() 
    {
        return businessProcessContext.get() != null;
    }
   
    /**
     * Remove the named component from all contexts.
     */
   public static void removeFromAllContexts(String name)
   {
      log.debug("removing from all contexts: " + name);
      if (isMethodContextActive())
      {
         getMethodContext().remove(name);
      }
      if (isEventContextActive())
      {
         getEventContext().remove(name);
      }
      if (isPageContextActive())
      {
         getPageContext().remove(name);
      }
      if (isConversationContextActive())
      {
         getConversationContext().remove(name);
      }
      if (isSessionContextActive())
      {
         getSessionContext().remove(name);
      }
      if (isBusinessProcessContextActive())
      {
         getBusinessProcessContext().remove(name);
      }
      if (isApplicationContextActive())
      {
         getApplicationContext().remove(name);
      }
   }

   /**
    * Search for a named attribute in all contexts, in the
    * following order: method, event, page, conversation,
    * session, business process, application.
    * 
    * @return the first component found, or null
    */
   public static Object lookupInStatefulContexts(String name)
   {
      if (isMethodContextActive())
      {
         Object result = getMethodContext().get(name);
         if (result!=null)
         {
             if ( log.isTraceEnabled() ) log.trace("found in method context: " + name);
            return result;
         }
      }
      
      if (isEventContextActive())
      {
         Object result = getEventContext().get(name);
         if (result!=null)
         {
             if ( log.isTraceEnabled() ) log.trace("found in event context: " + name);
            return result;
         }
      }
      
      if ( isPageContextActive() )
      {
         Object result = getPageContext().get(name);
         if (result!=null)
         {
             if ( log.isTraceEnabled() ) log.trace("found in page context: " + name);
            return result;
         }
      }
      
      if (isConversationContextActive())
      {
         Object result = getConversationContext().get(name);
         if (result!=null)
         {
             if ( log.isTraceEnabled() ) log.trace("found in conversation context: " + name);
            return result;
         }
      }
      
      if (isSessionContextActive())
      {
         Object result = getSessionContext().get(name);
         if (result!=null)
         {
             if ( log.isTraceEnabled() ) log.trace("found in session context: " + name);
            return result;
         }
      }
      
      if (isBusinessProcessContextActive())
      {
         Object result = getBusinessProcessContext().get(name);
         if (result!=null)
         {
             if ( log.isTraceEnabled() ) log.trace("found in business process context: " + name);
            return result;
         }
      }
      
      if (isApplicationContextActive())
      {
         Object result = getApplicationContext().get(name);
         if (result!=null)
         {
             if ( log.isTraceEnabled() ) log.trace("found in application context: " + name);
            return result;
         }
      }
      
      return null;
      
   }
   
   /**
    * Destroy all components in the given context
    */
   static void destroy(Context context)
   {
      if ( Events.exists() ) Events.instance().raiseEvent("org.jboss.seam.preDestroyContext." + context.getType().toString());
      
      Lifecycle.startDestroying();
      try
      {
         for ( String name: context.getNames() ) 
         {
            Component component = Component.forName(name);
            log.debug("destroying: " + name);
            if ( component!=null )
            {
               Object object = context.get(name);
               if (object!=null) //in a portal environment, this is possible
               {
                  if ( Events.exists() ) Events.instance().raiseEvent("org.jboss.seam.preDestroy." + name);
                  component.destroy(object);
               }
            }
         }
      }
      finally
      {
         Lifecycle.stopDestroying();
      }
      
      if ( Events.exists() ) Events.instance().raiseEvent("org.jboss.seam.postDestroyContext." + context.getType().toString());
   }
   
   /**
    * Startup all @Startup components in the given scope
    */
   static void startup(ScopeType scopeType)
   {
      Context context = Contexts.getApplicationContext();
      for ( String name: context.getNames() )
      {
         Object object = context.get(name);
         if ( object!=null && (object instanceof Component) )
         {
            Component component = (Component) object;
            if ( component.isStartup() && component.getScope()==scopeType )
            {
               startup(component);
            }
         }
      }
   }

   /**
    * Startup a component and all its dependencies
    */
   static void startup(Component component)
   {
      if ( component.isStartup() )
      {
         for ( String dependency: component.getDependencies() )
         {
            Component dependentComponent = Component.forName(dependency);
            if (dependentComponent!=null)
            {
               startup(dependentComponent);
            }
         }
      }

      if ( !component.getScope().getContext().isSet( component.getName() ) ) 
      {
         log.debug( "starting up: " + component.getName() );
         component.newInstance();
      }
   }

   /**
    * Does this context attribute need to be force-replicated?
    */
   static boolean isAttributeDirty(Object attribute)
   {
      return attribute instanceof Mutable && ( (Mutable) attribute ).clearDirty();
   }

   /**
    * At the end of a request, flush all contexts to their underlying
    * persistent stores, or destroy their attributes (one or the other!).
    */
   static void flushAndDestroyContexts()
   {
   
      if ( isConversationContextActive() )
      {
   
         if ( isBusinessProcessContextActive() )
         {
            boolean transactionActive = false;
            try
            {
               transactionActive = Transaction.instance().isActive();
            }
            catch (Exception e)
            {
               log.error("could not discover transaction status");
            }
            if (transactionActive)
            {
               //in calls to MDBs and remote calls to SBs, the 
               //transaction doesn't commit until after contexts
               //are destroyed, so pre-emptively flush here:
               getBusinessProcessContext().flush();
            }
            
            //TODO: it would be nice if BP context spanned redirects along with the conversation
            //      this would also require changes to BusinessProcessContext
            boolean destroyBusinessProcessContext = !Init.instance().isJbpmInstalled() ||
                  !BusinessProcess.instance().hasActiveProcess();
            if (destroyBusinessProcessContext)
            {
               //TODO: note that this occurs from Lifecycle.endRequest(), after
               //      the Seam-managed txn was committed, but Contexts.destroy()
               //      calls BusinessProcessContext.getNames(), which hits the
               //      database!
               log.debug("destroying business process context");
               destroy( getBusinessProcessContext() );
            }
         }
   
         if ( !Manager.instance().isLongRunningConversation() )
         {
            log.debug("destroying conversation context");
            destroy( getConversationContext() );
         }
         /*if ( !Init.instance().isClientSideConversations() )
         {*/
            //note that we need to flush even if the session is
            //about to be invalidated, since we still need
            //to destroy the conversation context in endSession()
            log.debug("flushing server-side conversation context");
            getConversationContext().flush();
        //}
   
         //uses the event and session contexts
         if ( Session.getInstance()!=null )
         {
            Manager.instance().unlockConversation();
         }
   
      }
      
      if ( isSessionContextActive() )
      {
         log.debug("flushing session context");
         getSessionContext().flush();
      }
      
      //destroy the event context after the
      //conversation context, since we need
      //the manager to flush() conversation
      if ( isEventContextActive() )
      {
         log.debug("destroying event context");
         destroy( getEventContext() );
      }
   
   }

   /**
    * Destroy a conversation context that is not currently bound to the request, called 
    * due to a timeout.
    * 
    * @param session the current session, to which both current and destroyed conversation belong
    * @param conversationId the conversation id of the conversation to be destroyed
    */
   static void destroyConversationContext(Map<String, Object> session, String conversationId)
   {
      Context current = getConversationContext();
      ServerConversationContext temp = new ServerConversationContext(session, conversationId);
      conversationContext.set(temp);
      try
      {
         destroy(temp);
         if ( !Session.instance().isInvalid() ) //its also unnecessary during a session timeout
         {
            temp.clear();
            temp.flush();
         }
      }
      finally
      {
         conversationContext.set(current);
      }
   }

}
