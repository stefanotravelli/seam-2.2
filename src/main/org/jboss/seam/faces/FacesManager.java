/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.FacesLifecycle;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.Init;
import org.jboss.seam.core.Interpolator;
import org.jboss.seam.core.Manager;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.navigation.ConversationIdParameter;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.pageflow.Pageflow;

/**
 * An extended conversation manager for the JSF environment.
 *
 * @author Gavin King
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 */
@Scope(ScopeType.EVENT)
@Name("org.jboss.seam.core.manager")
@Install(precedence=FRAMEWORK, classDependencies="javax.faces.context.FacesContext")
@BypassInterceptors
public class FacesManager extends Manager
{
   private static final LogProvider log = Logging.getLogProvider(FacesManager.class);
   
   

   private boolean controllingRedirect; 
   
   /**
    * Temporarily promote a temporary conversation to
    * a long running conversation for the duration of
    * a browser redirect. After the redirect, the 
    * conversation will be demoted back to a temporary
    * conversation. Handle any changes to the conversation
    * id, due to propagation via natural id.
    */
   public void beforeRedirect(String viewId)
   {
      beforeRedirect();
      
      FacesContext facesContext = FacesContext.getCurrentInstance();
      String currentViewId = Pages.getViewId(facesContext);
      if ( viewId!=null && currentViewId!=null )
      {
         ConversationIdParameter currentPage = Pages.instance().getPage(currentViewId).getConversationIdParameter();
         ConversationIdParameter targetPage = Pages.instance().getPage(viewId).getConversationIdParameter();
         if ( isDifferentConversationId(currentPage, targetPage) )
         {
            updateCurrentConversationId( targetPage.getConversationId() );
         }      
      }
   }

   public void interpolateAndRedirect(String url)
   {
      Map<String, Object> parameters = new HashMap<String, Object>();
      int loc = url.indexOf('?');
      if (loc>0)
      {
         StringTokenizer tokens = new StringTokenizer( url.substring(loc), "?=&" );
         while ( tokens.hasMoreTokens() )
         {
            String name = tokens.nextToken();
            String value = Interpolator.instance().interpolate( tokens.nextToken() );
            parameters.put(name, value);
         }
         url = url.substring(0, loc);
      }
      redirect(url, parameters, true, true);
   }
   
   @Override
   protected void storeConversationToViewRootIfNecessary()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      if ( facesContext!=null && FacesLifecycle.getPhaseId()==PhaseId.RENDER_RESPONSE )
      {
         FacesPage.instance().storeConversation();
      }
   }

   @Override
   protected String generateInitialConversationId()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      String viewId = Pages.getViewId(facesContext);
      if ( viewId!=null )
      {
         return Pages.instance().getPage(viewId)
                     .getConversationIdParameter()
                     .getInitialConversationId( facesContext.getExternalContext().getRequestParameterMap() );
      }
      else
      {
         return super.generateInitialConversationId();
      }
   }

   public void redirectToExternalURL(String url) {
       try {
           FacesContext.getCurrentInstance().getExternalContext().redirect(url);
       } catch (IOException e) {
          throw new RedirectException(e);
       }
   }
   
   /**
    * Redirect to the given view id, encoding the conversation id
    * into the request URL.
    * 
    * @param viewId the JSF view id
    */
   @Override
   public void redirect(String viewId)
   {
      redirect(viewId, null, true, true);
   }   
   
   public void redirect(String viewId, Map<String, Object> parameters, 
         boolean includeConversationId)
   {
      redirect(viewId, parameters, includeConversationId, true);
   }
   
   /**
    * Redirect to the given view id, after encoding parameters and conversation  
    * id into the request URL.
    * 
    * @param viewId the JSF view id
    * @param parameters request parameters to be encoded (possibly null)
    * @param includeConversationId determines if the conversation id is to be encoded
    */
   public void redirect(String viewId, Map<String, Object> parameters, 
            boolean includeConversationId, boolean includePageParams)
   {
      if (viewId == null)
      {
         throw new RedirectException("cannot redirect to a null viewId");
      }
      FacesContext context = FacesContext.getCurrentInstance();
      String url = context.getApplication().getViewHandler().getActionURL(context, viewId);
      if (parameters!=null) 
      {
         url = encodeParameters(url, parameters);
      }
      
      if (includePageParams)
      {
         url = Pages.instance().encodePageParameters(FacesContext.getCurrentInstance(), 
                  url, viewId, parameters==null ? Collections.EMPTY_SET : parameters.keySet());
      }
      
      if (includeConversationId)
      {
         beforeRedirect(viewId);
         url = encodeConversationId(url, viewId);
      }
      redirect(viewId, context, url);
   }
   
   /**
    * Redirect to the given view id, after encoding the given conversation  
    * id into the request URL.
    * 
    * @param viewId the JSF view id
    * @param conversationId an id of a long-running conversation
    */
   @Override
   public void redirect(String viewId, String conversationId)
   {
      if (viewId == null)
      {
         throw new RedirectException("cannot redirect to a null viewId");
      }
      FacesContext context = FacesContext.getCurrentInstance();
      String url = context.getApplication().getViewHandler().getActionURL(context, viewId);
      url = encodeConversationId(url, viewId, conversationId);
      redirect(viewId, context, url);
   }
   
   private void redirect(String viewId, FacesContext context, String url)
   {
      url = Pages.instance().encodeScheme(viewId, context, url);
      if ( log.isDebugEnabled() )
      {
         log.debug("redirecting to: " + url);
      }
      ExternalContext externalContext = context.getExternalContext();
      controllingRedirect = true;
      try
      {  
         Contexts.getEventContext().set(REDIRECT_FROM_MANAGER, "");
         externalContext.redirect( externalContext.encodeActionURL(url) );
      }
      catch (IOException ioe)
      {
         throw new RedirectException(ioe);
      }
      finally
      {
         Contexts.getEventContext().remove(REDIRECT_FROM_MANAGER);
         controllingRedirect = false;
      }
      context.responseComplete();
   }
   
   /**
    * Called by the Seam Redirect Filter when a redirect is called.
    * Appends the conversationId parameter if necessary.
    * 
    * @param url the requested URL
    * @return the resulting URL with the conversationId appended
    */
   public String appendConversationIdFromRedirectFilter(String url, String viewId)
   {
      boolean appendConversationId = !controllingRedirect;
      if (appendConversationId)
      {
         beforeRedirect(viewId);         
         url = encodeConversationId(url, viewId);
      }
      return url;
   }

   /**
    * If a page description is defined, remember the description and
    * view id for the current page, to support conversation switching.
    * Called just before the render phase.
    */
   public void prepareBackswitch(FacesContext facesContext) 
   {
      
      Conversation conversation = Conversation.instance();

      //stuff from jPDL takes precedence
      org.jboss.seam.pageflow.Page page = 
            Manager.instance().isLongRunningConversation() &&
            Init.instance().isJbpmInstalled() && 
            Pageflow.instance().isInProcess() && Pageflow.instance().isStarted() ?
                  Pageflow.instance().getPage() : null;
      
      if (page==null)
      {
         //handle stuff defined in pages.xml
         Pages pages = Pages.instance();
         if (pages!=null) //for tests
         {
            String viewId = Pages.getViewId(facesContext);
            org.jboss.seam.navigation.Page pageEntry = pages.getPage(viewId);
            if ( pageEntry.isSwitchEnabled() )
            {
               conversation.setViewId(viewId);
            }
            if ( pageEntry.hasDescription() )
            {
               conversation.setDescription( pageEntry.renderDescription() );
            }
            else if(pages.hasDescription(viewId))
            {
               conversation.setDescription( pages.renderDescription(viewId) );  
            }
            conversation.setTimeout( pages.getTimeout(viewId) );
            conversation.setConcurrentRequestTimeout( pages.getConcurrentRequestTimeout(viewId) );
         }
      }
      else
      {
         //use stuff from the pageflow definition
         if ( page.isSwitchEnabled() )
         {
            conversation.setViewId( Pageflow.instance().getPageViewId() );
         }
         if ( page.hasDescription() )
         {
            conversation.setDescription( page.getDescription() );
         }
         conversation.setTimeout( page.getTimeout() );
      }
      
      flushConversationMetadata();

   }

   public static FacesManager instance()
   {
      return (FacesManager) Manager.instance();
   }

}
