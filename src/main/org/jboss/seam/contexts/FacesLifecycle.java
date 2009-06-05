/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.contexts;

import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.jboss.seam.ScopeType;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.web.Session;

/**
 * Methods for setup and teardown of Seam contexts at the
 * beginning and end of JSF requests.
 * 
 * @see org.jboss.seam.jsf.SeamPhaseListener
 * @author Gavin King
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 */
public class FacesLifecycle
{
   private static ThreadLocal<PhaseId> phaseId = new ThreadLocal<PhaseId>();

   private static final LogProvider log = Logging.getLogProvider(FacesLifecycle.class);
   
   public static void setPhaseId(PhaseId phase)
   {
      phaseId.set(phase);
   }
   
   public static PhaseId getPhaseId()
   {
      return phaseId.get();
   }

   public static void clearPhaseId()
   {
      setPhaseId(null);
   }

   public static void beginRequest(ExternalContext externalContext) 
   {
      log.debug( ">>> Begin JSF request for " + getRequestPath(externalContext) );
      Contexts.eventContext.set( new EventContext( externalContext.getRequestMap() ) );
      Contexts.applicationContext.set( new ApplicationContext( externalContext.getApplicationMap() ) );
      Contexts.sessionContext.set( new SessionContext( externalContext.getSessionMap() ) );
      Session session = Session.getInstance();
      if ( session!=null && session.isInvalidDueToNewScheme( Pages.instance().getRequestScheme( FacesContext.getCurrentInstance() ) ) )
      {
         invalidateSession(externalContext);
      }
      Contexts.conversationContext.set(null); //in case endRequest() was never called
      //Events.instance(); //TODO: only for now, until we have a way to do EL outside of JSF!
      
      saveRequestPath(externalContext);
   }
   
   
   /**
    * with rewriting, the filter chain might not have access to the post-rewrite request information.
    * we'll save some of the information that we may need.
    */
   private static void saveRequestPath(ExternalContext externalContext) {
       Map<String, Object> map = externalContext.getRequestMap();
           
       map.put("org.jboss.seam.web.requestServletPath", externalContext.getRequestServletPath());
       map.put("org.jboss.seam.web.requestContextPath", externalContext.getRequestContextPath());
       map.put("org.jboss.seam.web.requestPathInfo",    externalContext.getRequestPathInfo());
   }

   public static void beginExceptionRecovery(ExternalContext externalContext)
   {
      log.debug(">>> Begin exception recovery");
      
      //application and session contexts are easy :-)
      Contexts.applicationContext.set( new ApplicationContext( externalContext.getApplicationMap() ) );      
      Contexts.sessionContext.set( new SessionContext( externalContext.getSessionMap() ) );
      
      //don't really have anything good to do with these ones:
      Contexts.pageContext.set(null);
      Contexts.businessProcessContext.set(null); //TODO: is this really correct?

      //depending upon when the exception occurs, endRequest() may or may not get called
      //by SeamPhaseListener - this is because the stupid JSF spec gives me know way to
      //determine if an exception occurred during the phase :-/
      if ( Contexts.isEventContextActive() )
      {
         //this means that endRequest() never ran, so the components in the EVENT context
         //have not yet been destroyed, and we can re-use them
         Contexts.eventContext.set( new EventContext( externalContext.getRequestMap() ) );
      }
      else
      {
         //the endRequest() method already ran, so the EVENT context is in principle empty, 
         //but we can't use the actual ServletRequest, since it contains garbage components
         //that have already been destroyed (we could change that in EventContext!) 
         Contexts.eventContext.set( new BasicContext(ScopeType.EVENT) );
      }
      
      //for the conversation context, we need to account for flushing
      //note that since we can't guarantee that it's components get flushed 
      //to the session, there is always a possibility of components that were
      //newly created in the current request "leaking" (we could fix that by
      //copying unflushed changes in the context object into the new context
      //object, or we could fix it by flushing at the end of every phase)
      boolean conversationContextFlushed = !Contexts.isConversationContextActive();
      ServerConversationContext conversationContext = new ServerConversationContext( externalContext.getSessionMap() );
      Contexts.conversationContext.set(conversationContext);
      if (conversationContextFlushed) conversationContext.unflush();
   }

   public static void endRequest(ExternalContext externalContext) 
   {
      log.debug("After render response, destroying contexts");
      try
      {
         Session session = Session.getInstance();
         boolean sessionInvalid = session!=null && session.isInvalid();
         
         Contexts.flushAndDestroyContexts();

         if (sessionInvalid)
         {
            Lifecycle.clearThreadlocals();
            clearPhaseId();
            invalidateSession(externalContext);
            //actual session context will be destroyed from the listener
         }
      }
      finally
      {
         Lifecycle.clearThreadlocals();
         log.debug( "<<< End JSF request for " + getRequestPath(externalContext) );
      }
   }
   
   /**
    * Invalidate the session, no matter what kind of session it is
    * (portlet or servlet). Why is this method not on ExternalContext?!
    * Oh boy, those crazy rascals in the JSF EG...
    */
   private static void invalidateSession(ExternalContext externalContext)
   {
      Object session = externalContext.getSession(false);
      if (session!=null)
      {
         try
         {
            session.getClass().getMethod("invalidate").invoke(session);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
   }

   public static void resumeConversation(ExternalContext externalContext)
   {
      ServerConversationContext conversationContext = new ServerConversationContext( externalContext.getSessionMap() );
      /*Context conversationContext = Init.instance().isClientSideConversations() ?
            (Context) new ClientConversationContext() :
            (Context) new ServerConversationContext( externalContext.getSessionMap() );*/
      Contexts.conversationContext.set(conversationContext);
      Contexts.businessProcessContext.set( new BusinessProcessContext() );
      conversationContext.unflush();
   }

   public static void resumePage()
   {
      Contexts.pageContext.set( new PageContext() );
   }
  
   private static String getRequestPath(ExternalContext externalContext)
   {
      return externalContext.getRequestContextPath() + externalContext.getRequestServletPath();
   }

}
