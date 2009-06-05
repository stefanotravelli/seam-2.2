package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.PerNestedConversation;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.AbstractMutable;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.navigation.Pages;

/**
 * Convenient API for performing browser redirects with
 * parameters.
 * 
 * @author Gavin King
 */
@Name("org.jboss.seam.faces.redirect")
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
@Install(precedence=BUILT_IN, classDependencies="javax.faces.context.FacesContext")
@PerNestedConversation
public class Redirect extends AbstractMutable implements Serializable
{
   private static final long serialVersionUID = 6947384474861235210L;
   private String viewId;
   private Map<String, Object> parameters = new HashMap<String, Object>();
   private boolean conversationPropagationEnabled = true;
   private boolean conversationBegun;
   
   /**
    * Get the JSF view id to redirect to
    */
   public String getViewId()
   {
      return viewId;
   }
   
   /**
    * Set the JSF view id to redirect to
    * 
    * @param viewId any JSF view id
    */
   public void setViewId(String viewId)
   {
      setDirty(this.viewId, viewId);
      this.viewId = viewId;
   }
   
   /**
    * Get all the request parameters that have been set
    */
   public Map<String, Object> getParameters()
   {
      return parameters;
   }
   
   /**
    * Set a request parameter value (to set a multi-valued
    * request parameter, pass an array or collection as
    * the value)
    */
   public void setParameter(String name, Object value)
   {
      Object old = parameters.put(name, value);
      setDirty(old, value);
   }
   
   /**
    * Capture the view id and request parameters from the
    * current request and squirrel them away so we can
    * return here later in the conversation.
    * 
    * @deprecated use captureCurrentView()
    */
   public void captureCurrentRequest()
   {
      parameters.clear();
      FacesContext context = FacesContext.getCurrentInstance();
      parameters.putAll( context.getExternalContext().getRequestParameterMap() );
      viewId = Pages.getViewId(context);
      setDirty();
   }
   
   /**
    * Capture the view id, request parameters and page parameters (which take
    * precedence) from the current request and squirrel them away so we can
    * return here later in the conversation. If no conversation is active,
    * begin a conversation. The conversation is terminated by {@link
    * Redirect#returnToCapturedView()} if begun by this method.
    * 
    * @see Redirect#returnToCapturedView()
    */
   public void captureCurrentView()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      
      // If this isn't a faces request then just return
      if (context == null) return;
      
      // first capture all request parameters
      parameters.putAll( context.getExternalContext().getRequestParameterMap() );
      // then preserve page parameters, overwriting request parameters with same names
      parameters.putAll( Pages.instance().getStringValuesFromPageContext(context) );
      
      // special case only needed for actionMethod if decide not to capture all request parameters
      //if (context.getExternalContext().getRequestParameterMap().containsKey("actionMethod"))
      //{
      //   parameters.put("actionMethod", context.getExternalContext().getRequestParameterMap().get("actionMethod"));
      //}
      
      viewId = Pages.getViewId(context);
      conversationBegun = Conversation.instance().begin(true, false);
      setDirty();
      //if the request ends with an exception,
      //the conversation context never gets
      //flushed....
      Contexts.getConversationContext().flush();
   }
   
   /**
    * Should the conversation be propagated across the redirect?
    * @return true by default
    */
   public boolean isConversationPropagationEnabled()
   {
      return conversationPropagationEnabled;
   }
   
   /**
    * Note that conversations are propagated by default
    */
   public void setConversationPropagationEnabled(boolean conversationPropagationEnabled)
   {
      this.conversationPropagationEnabled = conversationPropagationEnabled;
   }
   
   /**
    * Perform the redirect
    */
   public void execute()
   {
      FacesManager.instance().redirect(viewId, parameters, conversationPropagationEnabled, true);
   }
   
   /**
    * Redirect to the captured view, and end any conversation
    * that began in captureCurrentView(). 
    *
    *@see Redirect#captureCurrentView()
    */
   public boolean returnToCapturedView()
   {
      if (viewId!=null)
      {
         if (conversationBegun)
         {
            Conversation.instance().end();
         }
         execute();
         return true;
      }
      else
      {
         return false;
      }
   }
   
   //TODO: replacement for Conversation.endAndRedirect()
   /*public boolean returnToParentView()
   {
      Manager manager = Manager.instance();
      String viewId = manager.getParentConversationViewId();
      if (viewId==null)
      {
         return false;
      }
      else
      {
         manager.redirect(viewId);
         return true;
      }         
   }*/
   
   public static Redirect instance()
   {
      if ( !Contexts.isConversationContextActive() )
      {
         throw new IllegalStateException("No active conversation context");
      }
      return (Redirect) Component.getInstance(Redirect.class, ScopeType.CONVERSATION);
   }
}
