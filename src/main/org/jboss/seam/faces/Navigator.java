package org.jboss.seam.faces;

import java.util.Map;

import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.util.Strings;

public abstract class Navigator
{
   private static final LogProvider log = Logging.getLogProvider(Navigator.class);

   /**
    * Send an error.
    */
   protected void error(int code, String message)
   {
      if ( log.isDebugEnabled() ) log.debug("sending error: " + code);
      org.jboss.seam.faces.HttpError httpError = org.jboss.seam.faces.HttpError.instance();
      if (message==null)
      {
         httpError.send(code);
      }
      else
      {
         httpError.send(code, message);
      }
   }

   protected void redirectExternal(String url) {
       FacesManager.instance().redirectToExternalURL(url);
   }
   
   protected void redirect(String viewId, Map<String, Object> parameters)
   {
      redirect(viewId, parameters, true);
   }
   
   /**
    * Redirect to the view id.
    */
   protected void redirect(String viewId, Map<String, Object> parameters, boolean includePageParams)
   {
      if ( Strings.isEmpty(viewId) )
      {
         viewId = Pages.getCurrentViewId();
      }
      if ( log.isDebugEnabled() ) log.debug("redirecting to: " + viewId);
      FacesManager.instance().redirect(viewId, parameters, true, includePageParams);
   }
   
   /**
    * Render the view id.
    */
   protected void render(String viewId)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      if ( !Strings.isEmpty(viewId) )
      {
         UIViewRoot viewRoot = facesContext.getApplication().getViewHandler()
               .createView(facesContext, viewId);
         facesContext.setViewRoot(viewRoot);
      }
      else
      {
         viewId = Pages.getViewId(facesContext); //just for the log message
      }
      if ( log.isDebugEnabled() ) log.debug("rendering: " + viewId);
      facesContext.renderResponse();
   }

   protected static String getDisplayMessage(Exception e, String message)
   {
      if ( Strings.isEmpty(message) && e.getMessage()!=null ) 
      {
         return e.getMessage();
      }
      else
      {
         return message;
      }
   }
   
   @SuppressWarnings("deprecation")
   protected static void addFacesMessage(String message, Severity severity, String control, Object... params)
   {
      if ( Contexts.isConversationContextActive() )
      {
         if ( !Strings.isEmpty(message) )
         {
            if ( Strings.isEmpty(control) )
            {
               FacesMessages.instance().add(severity, message, params);
            }
            else
            {
               FacesMessages.instance().addToControl(control, severity, message, params);
            }
         }
      }
   }
   
}
