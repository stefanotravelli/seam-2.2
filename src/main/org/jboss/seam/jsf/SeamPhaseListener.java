/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.jsf;

import static javax.faces.event.PhaseId.ANY_PHASE;
import static javax.faces.event.PhaseId.INVOKE_APPLICATION;
import static javax.faces.event.PhaseId.PROCESS_VALIDATIONS;
import static javax.faces.event.PhaseId.RENDER_RESPONSE;
import static javax.faces.event.PhaseId.RESTORE_VIEW;
import static org.jboss.seam.transaction.Transaction.TRANSACTION_FAILED;

import java.lang.reflect.Method;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Seam;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.FacesLifecycle;
import org.jboss.seam.core.ConversationPropagation;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Init;
import org.jboss.seam.core.Manager;
import org.jboss.seam.exception.Exceptions;
import org.jboss.seam.faces.FacesManager;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.FacesPage;
import org.jboss.seam.faces.Switcher;
import org.jboss.seam.faces.Validation;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.pageflow.Pageflow;
import org.jboss.seam.persistence.PersistenceContexts;
import org.jboss.seam.transaction.Transaction;
import org.jboss.seam.transaction.UserTransaction;
import org.jboss.seam.util.Reflections;
import org.jboss.seam.web.ServletContexts;

/**
 * Manages the Seam contexts associated with a JSF request
 * throughout the lifecycle of the request. Performs
 * transaction demarcation when Seam transaction management
 * is enabled. Hacks the JSF lifecyle to provide page
 * actions and page parameters.
 *
 * @author Gavin King
 */
public class SeamPhaseListener implements PhaseListener
{
   private static final long serialVersionUID = -9127555729455066493L;
   
   private static final LogProvider log = Logging.getLogProvider(SeamPhaseListener.class);
   
   private static boolean exists = false;
   
   private static final Method SET_RENDER_PARAMETER;
   private static final Class ACTION_RESPONSE;
   private static final Class PORTLET_REQUEST;
   
   static
   {
      Method method = null;
      Class actionResponseClass = null;
      Class portletRequestClass = null;
      try
      {
         Class[] parameterTypes = { String.class, String.class };
         actionResponseClass = Class.forName("javax.portlet.ActionResponse");
         portletRequestClass = Class.forName("javax.portlet.PortletRequest");
         method = actionResponseClass.getMethod("setRenderParameter", parameterTypes);
      }
      catch (Exception e) {}
      SET_RENDER_PARAMETER = method;
      ACTION_RESPONSE = actionResponseClass;
      PORTLET_REQUEST = portletRequestClass;
   }

   public SeamPhaseListener()
   {
      if (exists) 
      {
         log.warn("There should only be one Seam phase listener per application");
      }
      exists=true;
   }
   
   public PhaseId getPhaseId()
   {
      return ANY_PHASE;
   }
   
   public void beforePhase(PhaseEvent event)
   {
      log.trace( "before phase: " + event.getPhaseId() );
      
      FacesLifecycle.setPhaseId( event.getPhaseId() );

      try
      {
         if ( isPortletRequest(event) )
         {
            beforePortletPhase(event);
         }
         else
         {
            beforeServletPhase(event);
         }
         raiseEventsBeforePhase(event);
      }
      catch (Exception e)
      {
         try
         {
            Exceptions.instance().handle(e);
         }
         catch (Exception ehe) 
         {
            log.error("swallowing exception", e);
         }
      }

   }

   private void beforeServletPhase(PhaseEvent event)
   {
      if ( event.getPhaseId() == RESTORE_VIEW )
      {
         beforeRestoreView( event.getFacesContext() );
         ServletContexts.instance().setRequest((HttpServletRequest) event.getFacesContext().getExternalContext().getRequest());
      }
      
      handleTransactionsBeforePhase(event);         
      
      if ( event.getPhaseId() == RENDER_RESPONSE )
      {
         beforeRenderResponse( event.getFacesContext() );
      }
      
   }
   
   private void beforePortletPhase(PhaseEvent event)
   {

      FacesContext facesContext = event.getFacesContext();
      
      boolean notInitialised=false;

      if ( event.getPhaseId() == RESTORE_VIEW )
      {
         beforeRestoreView(facesContext);
      }
      if ( event.getPhaseId() == RENDER_RESPONSE && !Contexts.isApplicationContextActive() ) 
      {
          beforeRestoreView(facesContext);
          notInitialised = true;
      }
      
      //delegate to subclass:
      handleTransactionsBeforePhase(event);
      
      if (event.getPhaseId() == RENDER_RESPONSE) 
      {
         if (notInitialised) 
         {
            afterRestoreView(facesContext);
         }
         beforeRenderResponse(event.getFacesContext());
      }
   }

   public void afterPhase(PhaseEvent event)
   {
      log.trace( "after phase: " + event.getPhaseId() );
      
      try
      {
         raiseEventsAfterPhase(event);
         if ( isPortletRequest(event) )
         {
            afterPortletPhase(event);
         }
         else
         {
            afterServletPhase(event);
         }
      }
      catch (Exception e)
      {
         log.warn("uncaught exception, passing to exception handler", e);
         try
         {
            Exceptions.instance().handle(e);
            // A redirect occurred inside the error handler, and we are in after
            // phase, so we need to clean up now as there are no more after
            // phases to be run
            if ( event.getFacesContext().getResponseComplete() )
            {
               afterResponseComplete(event.getFacesContext());
            }
         }
         catch (Exception ehe) 
         {
            log.error("swallowing exception", e);
         }
      }

      FacesLifecycle.clearPhaseId();
      
   }

   private void afterServletPhase(PhaseEvent event)
   {
  
      FacesContext facesContext = event.getFacesContext();
      
      if ( event.getPhaseId() == RESTORE_VIEW )
      {
         afterRestoreView(facesContext);
      }      
      else if ( event.getPhaseId() == INVOKE_APPLICATION )
      {
         afterInvokeApplication();
      }
      else if ( event.getPhaseId() == PROCESS_VALIDATIONS )
      {
         afterProcessValidations(facesContext);
      }
            
      //has to happen after, since restoreAnyConversationContext() 
      //can add messages
      FacesMessages.afterPhase();
      
      handleTransactionsAfterPhase(event);
            
      if ( event.getPhaseId() == RENDER_RESPONSE )
      {
         afterRenderResponse(facesContext);
      }
      else if ( facesContext.getResponseComplete() )
      {
         afterResponseComplete(facesContext);
      }
   }
   
   private void afterPortletPhase(PhaseEvent event)
   {
      Object portletPhase = event.getFacesContext().getExternalContext().getRequestMap().get("javax.portlet.faces.phase");

      if (event.getPhaseId() == RESTORE_VIEW) 
      {
         afterRestoreView(event.getFacesContext());
      }
      else if (event.getPhaseId() == INVOKE_APPLICATION) 
      {
         afterInvokeApplication();
      }
      else if (event.getPhaseId() == PROCESS_VALIDATIONS) 
      {
         afterProcessValidations(event.getFacesContext());
      }

      FacesMessages.afterPhase();

      // delegate to subclass:
      handleTransactionsAfterPhase(event);

      if (event.getPhaseId() == RENDER_RESPONSE) 
      {
         // writeConversationIdToResponse(
         // facesContext.getExternalContext().getResponse() );
         afterRenderResponse(event.getFacesContext());
      }
      else if ( (null != portletPhase && "ActionPhase".equals(portletPhase.toString()) )
             && (event.getPhaseId() == INVOKE_APPLICATION
                     || event.getFacesContext().getRenderResponse() 
                     || event.getFacesContext().getResponseComplete()) )
      {
         Manager.instance().beforeRedirect();
         if ( Manager.instance().isLongRunningConversation() ) 
         {
             setPortletRenderParameter(
                   event.getFacesContext().getExternalContext().getResponse(),
                   Manager.instance().getConversationIdParameter(),
                     Manager.instance().getCurrentConversationId() );
         }
         afterResponseComplete( event.getFacesContext() );
      }
   }
   
   private static void setPortletRenderParameter(Object response, String conversationIdParameter, String conversationId)
   {
      if ( ACTION_RESPONSE.isInstance(response) )
      {
         Reflections.invokeAndWrap(SET_RENDER_PARAMETER, response, conversationIdParameter, conversationId);
      }
   }
   
   private static boolean isPortletRequest(PhaseEvent event)
   {
      return PORTLET_REQUEST!=null && 
            PORTLET_REQUEST.isInstance( event.getFacesContext().getExternalContext().getRequest() );
   }
   
   public void handleTransactionsBeforePhase(PhaseEvent event)
   {
      if ( Init.instance().isTransactionManagementEnabled() ) 
      {
         PhaseId phaseId = event.getPhaseId();
         boolean beginTran = phaseId == PhaseId.RENDER_RESPONSE || 
               phaseId == ( Transaction.instance().isConversationContextRequired() ? PhaseId.APPLY_REQUEST_VALUES : PhaseId.RESTORE_VIEW );
               //( phaseId == PhaseId.RENDER_RESPONSE && !Init.instance().isClientSideConversations() );
         
         if (beginTran) 
         {
            begin(phaseId);
         }
      }
   }
   
   public void handleTransactionsAfterPhase(PhaseEvent event)
   {
      if ( Init.instance().isTransactionManagementEnabled() ) 
      {
         PhaseId phaseId = event.getPhaseId();
         boolean commitTran = phaseId == PhaseId.INVOKE_APPLICATION || 
               event.getFacesContext().getRenderResponse() || //TODO: no need to commit the tx if we failed to restore the view
               event.getFacesContext().getResponseComplete() ||
               phaseId == PhaseId.RENDER_RESPONSE;
               //( phaseId == PhaseId.RENDER_RESPONSE && !Init.instance().isClientSideConversations() );
         
         if (commitTran)
         { 
            commitOrRollback(phaseId); //we commit before destroying contexts, cos the contexts have the PC in them
         }
      }
   }
   
   protected void handleTransactionsAfterPageActions(FacesContext facesContext)
   {
      if ( Init.instance().isTransactionManagementEnabled() ) 
      {
         commitOrRollback("after invoking page actions");
         if ( !facesContext.getResponseComplete() )
         {
            begin("before continuing render");
         }
      }
   }
   
   protected void afterInvokeApplication() 
   {
      if ( Init.instance().isTransactionManagementEnabled() ) 
      {
         raiseTransactionFailedEvent();
      }
   }

   protected void afterProcessValidations(FacesContext facesContext)
   {
      Validation.instance().afterProcessValidations(facesContext);
   }
   
   /**
    * Set up the Seam contexts, except for the conversation
    * context
    */
   protected void beforeRestoreView(FacesContext facesContext)
   {
      FacesLifecycle.beginRequest( facesContext.getExternalContext() );
   }
   
   /**
    * Restore the page and conversation contexts during a JSF request
    */
   protected void afterRestoreView(FacesContext facesContext)
   {
      FacesLifecycle.resumePage();
      Map parameters = facesContext.getExternalContext().getRequestParameterMap();
      ConversationPropagation.instance().restoreConversationId(parameters);
      boolean conversationFound = Manager.instance().restoreConversation();
      FacesLifecycle.resumeConversation( facesContext.getExternalContext() );
      postRestorePage(facesContext, parameters, conversationFound);
   }

   public void raiseEventsBeforePhase(PhaseEvent event)
   {
      if ( Contexts.isApplicationContextActive() )
      {
         Events.instance().raiseEvent("org.jboss.seam.beforePhase", event);
      }
      
      /*if ( Contexts.isConversationContextActive() && Init.instance().isJbpmInstalled() && Pageflow.instance().isInProcess() )
      {
         String name;
         PhaseId phaseId = event.getPhaseId();
         if ( phaseId == PhaseId.PROCESS_VALIDATIONS )
         {
            name = "process-validations";
         }
         else if ( phaseId == PhaseId.UPDATE_MODEL_VALUES )
         {
            name = "update-model-values";
         }
         else if ( phaseId == PhaseId.INVOKE_APPLICATION )
         {
            name = "invoke-application";
         }
         else if ( phaseId == PhaseId.RENDER_RESPONSE )
         {
            name = "render-response";
         }
         else
         {
            return;
         }
         Pageflow.instance().processEvents(name);
      }*/
   }
   
   public void raiseEventsAfterPhase(PhaseEvent event)
   {
      if ( Contexts.isApplicationContextActive() )
      {
         Events.instance().raiseEvent("org.jboss.seam.afterPhase", event);
      }
   }
   
   /**
    * Raise an event so that an observer may add a faces message when Seam-managed transactions fail.
    */
   protected void raiseTransactionFailedEvent()
   {
      try
      {
         UserTransaction tx = Transaction.instance();
         if ( tx.isRolledBackOrMarkedRollback() )
         {
            if (Events.exists()) Events.instance().raiseEvent(TRANSACTION_FAILED, tx.getStatus());
         }
      }
      catch (Exception e) {} //swallow silently, not important
   }
   
   protected void beforeRenderResponse(FacesContext facesContext)
   {  
      
      if ( Contexts.isPageContextActive() )
      {
         Context pageContext = Contexts.getPageContext();
         //after every time that the view may have changed,
         //we need to flush the page context, since the 
         //attribute map is being discarder
         pageContext.flush();
         //force refresh of the conversation lists (they are kept in PAGE context)
         pageContext.remove(Seam.getComponentName(Switcher.class));
         pageContext.remove("org.jboss.seam.core.conversationList");
         pageContext.remove("org.jboss.seam.core.conversationStack");
      }
      
      preRenderPage(facesContext);
      
      if ( facesContext.getResponseComplete() )
      {
         //workaround for a bug in MyFaces prior to 1.1.3
         if ( Init.instance().isMyFacesLifecycleBug() ) 
         {
            FacesLifecycle.endRequest( facesContext.getExternalContext() );
         }
      }
      else //if the page actions did not call responseComplete()
      {
         FacesMessages.instance().beforeRenderResponse();
         //do this both before and after render, since conversations 
         //and pageflows can begin during render
         FacesManager.instance().prepareBackswitch(facesContext); 
      }
      
      FacesPage.instance().storeConversation();
      FacesPage.instance().storePageflow();
      
      PersistenceContexts persistenceContexts = PersistenceContexts.instance();
      if (persistenceContexts != null) 
      {
          persistenceContexts.beforeRender();
      }
   }
   
   protected void afterRenderResponse(FacesContext facesContext)
   {
      //do this both before and after render, since conversations 
      //and pageflows can begin during render
      FacesManager.instance().prepareBackswitch(facesContext);
      
      PersistenceContexts persistenceContexts = PersistenceContexts.instance();
      if (persistenceContexts != null) 
      {
          persistenceContexts.afterRender();
      }
      
      ExternalContext externalContext = facesContext.getExternalContext();
      Manager.instance().endRequest( externalContext.getSessionMap() );
      FacesLifecycle.endRequest(externalContext);
   }
   
   protected void afterResponseComplete(FacesContext facesContext)
   {
      //responseComplete() was called by one of the other phases, 
      //so we will never get to the RENDER_RESPONSE phase
      //Note: we can't call Manager.instance().beforeRedirect() here, 
      //since a redirect is not the only reason for a responseComplete
      ExternalContext externalContext = facesContext.getExternalContext();
      Manager.instance().endRequest( externalContext.getSessionMap() );
      FacesLifecycle.endRequest( facesContext.getExternalContext() );
   }
   
   private void postRestorePage(FacesContext facesContext, Map parameters, boolean conversationFound)
   {
      if ( !Pages.isDebugPage() )
      {
         // Only redirect to no-conversation-view if a login redirect isn't required
         if (!conversationFound && !Pages.instance().isLoginRedirectRequired(facesContext))
         {
            Pages.instance().redirectToNoConversationView();
         }
         
         Manager.instance().handleConversationPropagation(parameters);
         
         if ( Init.instance().isJbpmInstalled() && !isExceptionHandlerRedirect() )
         {
            Pageflow.instance().validatePageflow(facesContext);
         }
         
         Pages.instance().postRestore(facesContext);
      }
   }
  
   private boolean preRenderPage(FacesContext facesContext)
   {
      if ( Pages.isDebugPage() )
      {
         return false;
      }
      else
      {
         FacesLifecycle.setPhaseId(PhaseId.INVOKE_APPLICATION);
         boolean actionsWereCalled = false;
         try
         {
            actionsWereCalled = Pages.instance().preRender(facesContext);
            return actionsWereCalled;
         }
         finally
         {
            FacesLifecycle.setPhaseId(PhaseId.RENDER_RESPONSE);
            if (actionsWereCalled) 
            {
               FacesMessages.afterPhase();
               handleTransactionsAfterPageActions(facesContext); //TODO: does it really belong in the finally?
            }
         }
      }
   }
   
   private boolean isExceptionHandlerRedirect()
   {
      return Contexts.getConversationContext().isSet("org.jboss.seam.handledException");
   }
     
   void begin(PhaseId phaseId)
   {
      begin("prior to phase: " + phaseId);
   }
   
   void begin(String phaseString) 
   {
      try 
      {
         if ( !Transaction.instance().isActiveOrMarkedRollback() )
         {
            log.debug("beginning transaction " + phaseString);
            Transaction.instance().begin();
         }
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Could not start transaction", e);
      }
   }
   
   void commitOrRollback(PhaseId phaseId)
   {
      commitOrRollback("after phase: " + phaseId);
   }
   
   void commitOrRollback(String phaseString) 
   {  
      try {
         if (Transaction.instance().isActive()) {
             try {
                 log.debug("committing transaction " + phaseString);            
                 Transaction.instance().commit();

             } catch (IllegalStateException e) {
                 log.warn("TX commit failed with illegal state exception. This may be " + 
                          "because the tx timed out and was rolled back in the background.", e);
             }
         } else if ( Transaction.instance().isRolledBackOrMarkedRollback()) {
            log.debug("rolling back transaction " + phaseString);
            Transaction.instance().rollback();
         }
         
      } catch (Exception e) {
         throw new IllegalStateException("Could not commit transaction", e);
      }
   }
   
}
