/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.contexts;

import java.lang.ref.WeakReference;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.servlet.ServletApplicationMap;
import org.jboss.seam.servlet.ServletRequestMap;
import org.jboss.seam.servlet.ServletRequestSessionMap;
import org.jboss.seam.servlet.ServletSessionMap;
import org.jboss.seam.web.Session;

/**
 * Methods for setup and teardown of Seam contexts at the
 * beginning and end of servlet requests.
 *
 * @author Gavin King
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 */
public class ServletLifecycle
{
   
   private static final LogProvider log = Logging.getLogProvider(ServletLifecycle.class);
   
   private static ServletContext servletContext;
   
   public static final String SERVLET_CONTEXT_KEY = "seam.contexts.servletContext";
   
   public static ServletContext getServletContext() 
   {
      //don't throw an exception if null, because of unit tests
      return servletContext;
   }
   
   public static void beginRequest(HttpServletRequest request)
   {
      beginRequest(request,null);
   }
   
   public static void beginRequest(HttpServletRequest request,ServletContext context)
   {
      
      ServletContext ctx = context;
      if(ctx == null)
      {
         //try and figure out which servlet context to use
         //from the request. 
         HttpSession session = request.getSession(false);
         if(session == null)
         {
            ctx = servletContext;
         }
         else
         {
            ctx = session.getServletContext();
         }
      }
      
      log.debug( ">>> Begin web request" );
      Contexts.eventContext.set( new EventContext( new ServletRequestMap(request) ) );
      Contexts.sessionContext.set( new SessionContext( new ServletRequestSessionMap(request) ) );
      Contexts.applicationContext.set(new ApplicationContext( new ServletApplicationMap(ctx) ) );
      Contexts.conversationContext.set(null); //in case endRequest() was never called
   }
   
   public static void endRequest(HttpServletRequest request) 
   {
      log.debug("After request, destroying contexts");
      try
      {
         Session session = Session.getInstance();
         boolean sessionInvalid = session!=null && session.isInvalid();
         
         Contexts.flushAndDestroyContexts();
         
         if (sessionInvalid)
         {
            Lifecycle.clearThreadlocals();
            request.getSession().invalidate();
            //actual session context will be destroyed from the listener
         }
      }
      finally
      {
         Lifecycle.clearThreadlocals();
         log.debug( "<<< End web request" );
      }
   }
   @Deprecated
   public static void beginReinitialization(HttpServletRequest request)
   {
      beginReinitialization(request, servletContext);
   }
   
   public static void beginReinitialization(HttpServletRequest request,ServletContext servletContext)
   {
      log.debug(">>> Begin re-initialization");
      Contexts.applicationContext.set( new ApplicationContext( new ServletApplicationMap(servletContext) ) );
      Contexts.eventContext.set( new BasicContext(ScopeType.EVENT) );
      Contexts.sessionContext.set( new SessionContext( new ServletRequestSessionMap(request) ) );
      Contexts.conversationContext.set( new BasicContext(ScopeType.CONVERSATION) );
   }
   
   public static void endReinitialization()
   {
      Contexts.startup(ScopeType.APPLICATION);
      
      Events.instance().raiseEvent("org.jboss.seam.postReInitialization");
      
      // Clean up contexts used during reinitialization
      Contexts.destroy( Contexts.getConversationContext() );
      Contexts.conversationContext.set(null);
      Contexts.destroy( Contexts.getEventContext() );
      Contexts.eventContext.set(null);
      Contexts.sessionContext.set(null);
      Contexts.applicationContext.set(null);
      
      log.debug("<<< End re-initialization");
   }
   
   public static void beginInitialization()
   {
      log.debug(">>> Begin initialization");
      Contexts.applicationContext.set( new ApplicationContext( Lifecycle.getApplication() ) );
      Contexts.eventContext.set( new BasicContext(ScopeType.EVENT) );
      Contexts.conversationContext.set( new BasicContext(ScopeType.CONVERSATION) );
   }
   
   public static void endInitialization()
   {
      Contexts.startup(ScopeType.APPLICATION);
      
      Events.instance().raiseEvent("org.jboss.seam.postInitialization");
      
      // Clean up contexts used during initialization
      Contexts.destroy( Contexts.getConversationContext() );
      Contexts.conversationContext.set(null);
      Contexts.destroy( Contexts.getEventContext() );
      Contexts.eventContext.set(null);
      Contexts.sessionContext.set(null);
      Contexts.applicationContext.set(null);
      
      log.debug("<<< End initialization");
   }
   
   public static void beginApplication(ServletContext context)
   {
      // caching the classloader to servletContext
      WeakReference<ClassLoader> ref = new WeakReference<ClassLoader>(Thread.currentThread().getContextClassLoader());
      context.setAttribute("seam.context.classLoader",ref);
      log.debug("Cached the context classloader in servletContext as 'seam.context.classLoader'");
      context.setAttribute(SERVLET_CONTEXT_KEY, context); 
      servletContext = context;
      Lifecycle.beginApplication( new ServletApplicationMap(context) );
   }
   
   public static void endApplication()
   {
      endApplication(servletContext);
   }
   
   public static void endApplication(ServletContext context)
   {
      Lifecycle.endApplication(new ServletApplicationMap( context));
      servletContext=null;
   }
   
   public static void beginSession(HttpSession session)
   {
      Lifecycle.beginSession( new ServletSessionMap(session), new ServletApplicationMap(session.getServletContext()) );
   }
   
   public static void endSession(HttpSession session)
   {
      Lifecycle.endSession( new ServletSessionMap(session) , new ServletApplicationMap(session.getServletContext()));
   }
   
   public static void resumeConversation(HttpServletRequest request)
   {
      ServerConversationContext conversationContext = new ServerConversationContext( new ServletRequestSessionMap(request) );
      Contexts.conversationContext.set(conversationContext);
      Contexts.businessProcessContext.set( new BusinessProcessContext() );
      conversationContext.unflush();
   }
   /**
    * Convenience method that retrieves the servlet context from application
    * scope.
    * 
    * @return the current servlet context
    */
   public static ServletContext getCurrentServletContext()
   {
      if (!Contexts.isApplicationContextActive())
      {
         return servletContext;
      }
      return (ServletContext) Contexts.getApplicationContext().get(SERVLET_CONTEXT_KEY);
   }
   
}
