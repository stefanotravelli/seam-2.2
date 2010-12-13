package org.jboss.seam.wicket;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebRequestCycleProcessor;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.IPageRequestTarget;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.core.ConversationPropagation;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Manager;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.servlet.ServletRequestSessionMap;
import org.jboss.seam.web.ServletContexts;
import org.jboss.seam.wicket.international.SeamStatusMessagesListener;

/**
 * The base class for Seam Web Applications
 * 
 * @author Pete Muir, Clint Popetz
 *
 */
public abstract class SeamWebApplication extends WebApplication
{
   
   private static final LogProvider log = Logging.getLogProvider(SeamWebApplication.class);

   /**
    * When operating in tests, it is sometimes useful to leave the contexts extant
    * after a request, and destroy them upon the next request, so that models that use injections
    * can be queried post-request to determine their values. 
    */
   protected boolean destroyContextsLazily = false;

   public boolean isDestroyContextsLazily()
   {
      return destroyContextsLazily;
   }

   public void setDestroyContextsLazily(boolean destroyContextsLazily)
   {
      this.destroyContextsLazily = destroyContextsLazily;
   }

   /**
    * Custom session with invalidation override. We can't just let Wicket
    * invalidate the session as Seam might have to do some cleaning up to do.
    * We provide SeamWebSession as a separate class to allow for user subclasssing.
    */
   @Override
   public Session newSession(Request request, Response response)
   {
      return new SeamWebSession(request);
   }
   
   /**
    * This is the key we will use to to store the conversation metadata in the wicket page.
    */
   private static MetaDataKey CID = new MetaDataKey<String>() { };

   
   /**
    * Seam's hooks into Wicket. Required for proper functioning
    */
   @Override
   protected IRequestCycleProcessor newRequestCycleProcessor()
   {
      return new WebRequestCycleProcessor()
      {
         /**
          * If a long running conversation has been started, store its id into page metadata
          */
         @Override
         public void respond(RequestCycle requestCycle)
         {
            super.respond(requestCycle);
            if (Manager.instance().isLongRunningConversation())
            {
               Page page = RequestCycle.get().getResponsePage();
               if (page != null)
               {
                  page.setMetaData(CID, Manager.instance().getCurrentConversationId());
               }
            }
         }
      };
   }

   /**
    * Override to set up seam security, seam status messages, and add the SeamEnforceConversationListener
    */
   @Override
   protected void init()
   {
      super.init();
      inititializeSeamSecurity();
      initializeSeamStatusMessages();
      addPreComponentOnBeforeRenderListener(new SeamEnforceConversationListener());
   }

   /**
    * Add Seam Security to the wicket app.
    * 
    * This allows you to @Restrict your Wicket components. Override this method
    * to apply a different scheme
    * 
    */
   protected void inititializeSeamSecurity()
   {
      getSecuritySettings().setAuthorizationStrategy(new SeamAuthorizationStrategy(getLoginPage()));
   }

   /**
    * Add Seam status message transport support to your app.
    */
   protected void initializeSeamStatusMessages()
   {
      addPreComponentOnBeforeRenderListener(new SeamStatusMessagesListener());
   }

   protected abstract Class getLoginPage();

   /*
    * Override to provide a seam-specific RequestCycle, which sets up seam contexts.  
    */
   @Override
   public RequestCycle newRequestCycle(final Request request, final Response response)
   {
      return new SeamWebRequestCycle(this, (WebRequest)request, (WebResponse)response);
   }
   
   
   /**
    * A WebRequestCycle that sets up seam requests.  Essentially this
    * is similiar to the work of ContextualHttpServletRequest, but using the wicket API
    *
    */
   protected static class SeamWebRequestCycle extends WebRequestCycle { 
      
      public SeamWebRequestCycle(WebApplication application, WebRequest request, Response response)
      {
         super(application, request, response);
      }

      /**
       * Override this so that we can pull out the conversation id from page metadata 
       * when a new page is chosen as the target 
       */
      @Override
      protected void onRequestTargetSet(IRequestTarget target)
      {
         super.onRequestTargetSet(target);
         Page page = null;
         if (target instanceof  BookmarkablePageRequestTarget) 
         {
            page = ((BookmarkablePageRequestTarget)target).getPage();
         }
         else if (target instanceof IPageRequestTarget)
         {
            page = ((IPageRequestTarget)target).getPage();
         }
         if (page != null) 
         {
            String cid = (String) page.getMetaData(CID);
            if (cid != null)
            {
               Manager manager = Manager.instance();
               if (manager.isLongRunningConversation()) 
               {
                  if (!cid.equals(manager.getCurrentConversationId()))
                  {
                     manager.switchConversation(cid);
                  }
               }
               else 
               {
                  ConversationPropagation cp = ConversationPropagation.instance();
                  cp.setConversationId(cid);
                  manager.restoreConversation();
               }
            }
            else 
            {
               Manager manager = Manager.instance();
               if (manager.isLongRunningConversation()) 
               {
                  page.setMetaData(CID, Manager.instance().getCurrentConversationId());
               }
            }
         }
      }
      
      /**
       * Override to destroy the old seam contexts if we are destroying lazily and they still exist, and
       * to set up the new seam contexts.
       */
      @Override
      protected void onBeginRequest() 
      {
         HttpServletRequest httpRequest = ((WebRequest)request).getHttpServletRequest();

         if (Contexts.getEventContext() != null && ((SeamWebApplication)getApplication()).isDestroyContextsLazily() && ServletContexts.instance().getRequest() != httpRequest)
         { 
            destroyContexts();
         }

         if (Contexts.getEventContext() == null)
         {
            ServletLifecycle.beginRequest(httpRequest);
            ServletContexts.instance().setRequest(httpRequest);
            ConversationPropagation.instance().restoreConversationId( request.getParameterMap() );
            Manager.instance().restoreConversation();
            ServletLifecycle.resumeConversation(httpRequest);
            Manager.instance().handleConversationPropagation( request.getParameterMap() );

            // Force creation of the session
            if (httpRequest.getSession(false) == null)
            {
               httpRequest.getSession(true);
            }
         }
         super.onBeginRequest();
         Events.instance().raiseEvent("org.jboss.seam.wicket.beforeRequest");
      }  

      /**
       * Override to tear down seam contexts unless we are destroying lazily
       */
      @Override
      protected void onEndRequest() 
      {
         try 
         { 
            super.onEndRequest();
            Events.instance().raiseEvent("org.jboss.seam.wicket.afterRequest");
         }
         finally 
         {
            if (Contexts.getEventContext() != null && !((SeamWebApplication)getApplication()).isDestroyContextsLazily())
            {
               destroyContexts();
            }
         }
      }

      /**
       * The actual work of destroying the seam contexts.
       */
      private void destroyContexts() 
      {
         try { 
            HttpServletRequest httpRequest = ((WebRequest)request).getHttpServletRequest();
            Manager.instance().endRequest( new ServletRequestSessionMap(httpRequest)  );
            ServletLifecycle.endRequest(httpRequest);
         }
	      catch (Exception e)
	      {
	         /* Make sure we always clear out the thread locals */
	         Lifecycle.endRequest();
	         log.warn("ended request due to exception", e);
	         throw new RuntimeException(e);
	      }
      }
   }

}
