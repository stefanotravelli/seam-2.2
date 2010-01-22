package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.util.Strings;

/**
 * A Seam component that propagates FacesMessages across redirects
 * and interpolates EL expressions in the message string.
 * 
 * @author Gavin King
 * @author Pete Muir
 */
@Scope(ScopeType.CONVERSATION)
@Name(StatusMessages.COMPONENT_NAME)
@Install(precedence=BUILT_IN, classDependencies="javax.faces.context.FacesContext")
@BypassInterceptors
public class FacesMessages extends StatusMessages
{
   
   /**
    * Called by Seam to transfer messages from FacesMessages to JSF
    */
   public void beforeRenderResponse() 
   {
      for (StatusMessage statusMessage: getMessages())
      {
         FacesContext.getCurrentInstance().addMessage( null, toFacesMessage(statusMessage) );
      }
      for ( Map.Entry<String, List<StatusMessage>> entry: getKeyedMessages().entrySet() )
      {
         for ( StatusMessage statusMessage: entry.getValue() )
         {
            String clientId = getClientId(entry.getKey());
            FacesContext.getCurrentInstance().addMessage( clientId, toFacesMessage(statusMessage) );
         }
      }
      clear();
   }
   
   /**
    * Called by Seam to transfer any messages added in the phase just processed
    * to the FacesMessages component.
    * 
    * A task runner is used to allow the messages access to outjected values.
    */
   public static void afterPhase()
   {
      runTasks();
   }
   
   /**
    * Convert a StatusMessage to a FacesMessage
    */
   private static FacesMessage toFacesMessage(StatusMessage statusMessage)
   {
      if (!Strings.isEmpty(statusMessage.getSummary()))
      {
         return new FacesMessage(toSeverity(statusMessage.getSeverity()), statusMessage.getSummary(), statusMessage.getDetail() );
      }
      else
      {
         return null;
      }
   }
   
   /**
    * Convert a StatusMessage.Severity to a FacesMessage.Severity
    */
   private static javax.faces.application.FacesMessage.Severity toSeverity(org.jboss.seam.international.StatusMessage.Severity severity)
   {
      switch (severity)
      {
      case ERROR:
         return FacesMessage.SEVERITY_ERROR;
      case FATAL:
         return FacesMessage.SEVERITY_FATAL;
      case INFO:
         return FacesMessage.SEVERITY_INFO;
      case WARN:
         return FacesMessage.SEVERITY_WARN;
      default:
         return null;
      }
   }
   
   /**
    * Convert a FacesMessage.Severity to a StatusMessage.Severity
    */
   private static org.jboss.seam.international.StatusMessage.Severity toSeverity(javax.faces.application.FacesMessage.Severity severity)
   {
      if (FacesMessage.SEVERITY_ERROR.equals(severity))
      {
         return org.jboss.seam.international.StatusMessage.Severity.ERROR;
      }
      else if (FacesMessage.SEVERITY_FATAL.equals(severity))
      {
         return org.jboss.seam.international.StatusMessage.Severity.FATAL;
      }
      else if (FacesMessage.SEVERITY_INFO.equals(severity))
      {
         return org.jboss.seam.international.StatusMessage.Severity.INFO;
      }
      else if (FacesMessage.SEVERITY_WARN.equals(severity))
      {
         return org.jboss.seam.international.StatusMessage.Severity.WARN;
      }
      else
      {
         return null;
      }
   }
   
   /**
    * Calculate the JSF client ID from the provided widget ID
    */
   private String getClientId(String id)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      return getClientId( facesContext.getViewRoot(), id, facesContext);
   }

   private static String getClientId(UIComponent component, String id, FacesContext facesContext)
   {
      String componentId = component.getId();
      if (componentId!=null && componentId.equals(id))
      {
         return component.getClientId(facesContext);
      }
      else
      {
         Iterator iter = component.getFacetsAndChildren();
         while ( iter.hasNext() )
         {
            UIComponent child = (UIComponent) iter.next();
            String clientId = getClientId(child, id, facesContext);
            if (clientId!=null) return clientId;
         }
         return null;
      }
   }
   
   /**
    * Get all faces messages that have already been added
    * to the context.
    * 
    */
   public List<FacesMessage> getCurrentMessages()
   {
      List<FacesMessage> result = new ArrayList<FacesMessage>();
      Iterator<FacesMessage> iter = FacesContext.getCurrentInstance().getMessages();
      while ( iter.hasNext() )
      {
         result.add( iter.next() );
      }
      return result;
   }
   
   /**
    * Get all faces global messages that have already been added
    * to the context.
    * 
    */
   public List<FacesMessage> getCurrentGlobalMessages()
   {
      List<FacesMessage> result = new ArrayList<FacesMessage>();
      Iterator<FacesMessage> iter = FacesContext.getCurrentInstance().getMessages(null);
      while ( iter.hasNext() )
      {
         result.add( iter.next() );
      }
      return result;
   }
   
   /**
    * Get all faces messages that have already been added
    * to the control.
    * 
    */
   public List<FacesMessage> getCurrentMessagesForControl(String id)
   {
      String clientId = getClientId(id);
      List<FacesMessage> result = new ArrayList<FacesMessage>();
      Iterator<FacesMessage> iter = FacesContext.getCurrentInstance().getMessages(clientId);
      while ( iter.hasNext() )
      {
         result.add( iter.next() );
      }
      return result;
   }
   
   /**
    * Utility method to create a FacesMessage from a Severity, messageTemplate 
    * and params.
    * 
    * This method interpolates the parameters provided
    */
   public static FacesMessage createFacesMessage(javax.faces.application.FacesMessage.Severity severity, String messageTemplate, Object... params)
   {
      return createFacesMessage(severity, null, messageTemplate, params);
   }
   
   /**
    * Utility method to create a FacesMessage from a Severity, key, 
    * defaultMessageTemplate and params.
    * 
    * This method interpolates the parameters provided
    */
   public static FacesMessage createFacesMessage(javax.faces.application.FacesMessage.Severity severity, String key, String defaultMessageTemplate, Object... params)
   {
      StatusMessage message = new StatusMessage(toSeverity(severity), key, null, defaultMessageTemplate, null);
      message.interpolate(params);
      return toFacesMessage(message);
   }
   
   /**
    * Add a FacesMessage that will be used
    * the next time a page is rendered.
    * 
    * Deprecated, use a method in {@link StatusMessages} instead
    */
   @Deprecated
   public void add(FacesMessage facesMessage) 
   {
      if (facesMessage!=null)
      {
         add(toSeverity(facesMessage.getSeverity()), null, null, facesMessage.getSummary(), facesMessage.getDetail());
      }
   }
   
   /**
    * Create a new status message, with the messageTemplate is as the message.
    * 
    * You can also specify the severity, and parameters to be interpolated
    * 
    * Deprecated, use {@link #add(org.jboss.seam.international.StatusMessage.Severity, String, Object...)} 
    * instead
    */
   @Deprecated
   public void add(javax.faces.application.FacesMessage.Severity severity, String messageTemplate, Object... params)
   {
      add(toSeverity(severity), messageTemplate, params);
   }
   
   
   /**
    * Create a new status message, with the messageTemplate is as the message.
    *
    * A severity of INFO will be used, and you can specify paramters to be
    * interpolated
    * 
    * Deprecated, use {@link #addToControl(String, org.jboss.seam.international.StatusMessage.Severity, String, Object...)}
    * instead
    */
   @Deprecated
   public void addToControl(String id, javax.faces.application.FacesMessage.Severity severity, String messageTemplate, Object... params)
   {
      addToControl(id, toSeverity(severity), messageTemplate, params);
   }
   
   /**
    * Add a status message, looking up the message in the resource bundle
    * using the provided key.
    * 
    * You can also specify the severity, and parameters to be interpolated
    * 
    * Deprecated, use {@link #addFromResourceBundle(org.jboss.seam.international.StatusMessage.Severity, String, Object...)}
    * instead
    */
   @Deprecated
   public void addFromResourceBundle(javax.faces.application.FacesMessage.Severity severity, String key, Object... params)
   {
      addFromResourceBundle(toSeverity(severity), key, params);
   }
   
   /**
    * Add a status message, looking up the message in the resource bundle
    * using the provided key.
    * 
    * You can also specify the severity, and parameters to be interpolated
    * 
    * Deprecated, use {@link #addFromResourceBundleOrDefault(javax.faces.application.FacesMessage.Severity, String, String, Object...)}
    * instead
    */
   @Deprecated
   public void addFromResourceBundleOrDefault(javax.faces.application.FacesMessage.Severity severity, String key, String defaultMessageTemplate, Object... params)
   {
      addFromResourceBundleOrDefault(toSeverity(severity), key, defaultMessageTemplate, params);
   }
   
   /**
    * Create a new status message, looking up the message in the resource bundle
    * using the provided key.
    * 
    * The message will be added to the widget specified by the ID. The algorithm
    * used determine which widget the id refers to is determined by the view 
    * layer implementation in use.
    * 
    * You can also specify the severity, and parameters to be interpolated
    * 
    * Deprecated, use {@link #addToControlFromResourceBundle(String, org.jboss.seam.international.StatusMessage.Severity, String, Object...)}
    * instead
    */
   @Deprecated
   public void addToControlFromResourceBundle(String id, javax.faces.application.FacesMessage.Severity severity, String key, Object... params)
   {
      addToControlFromResourceBundle(id, toSeverity(severity), key, params);
   }
   
   /**
    * Add a status message, looking up the message in the resource bundle
    * using the provided key. If the message is found, it is used, otherwise, 
    * the defaultMessageTemplate will be used.
    * 
    * The message will be added to the widget specified by the ID. The algorithm
    * used determine which widget the id refers to is determined by the view 
    * layer implementation in use.
    * 
    * You can also specify the severity, and parameters to be interpolated
    * 
    * Deprecated, use {@link #addToControlFromResourceBundleOrDefault(String, org.jboss.seam.international.StatusMessage.Severity, String, String, Object...)}
    * instead
    */
   @Deprecated
   public void addToControlFromResourceBundleOrDefault(String id, javax.faces.application.FacesMessage.Severity severity, String key, String defaultMessageTemplate, Object... params)
   {
      addToControlFromResourceBundleOrDefault(id, toSeverity(severity), key, defaultMessageTemplate, params);
   }
   
   public static FacesMessages instance()
   {
      Component component = Component.forName(StatusMessages.COMPONENT_NAME);
      if(component != null && !component.getScope().isContextActive())
      {
         throw new IllegalStateException("No active "+component.getScope().name()+" context");
      }
      //Attempting to get the instance anyway for backwards compatibility with some potential hack situations.
      return (FacesMessages) Component.getInstance(StatusMessages.COMPONENT_NAME);
   }
}
