package org.jboss.seam.navigation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.core.Events;
import org.jboss.seam.core.Interpolator;
import org.jboss.seam.core.ResourceLoader;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Strings;
import org.jboss.seam.web.Pattern;

/**
 * Metadata about page actions, page parameters, action navigation,
 * resource bundle, etc, for a particular JSF view id.
 */
public final class Page
{
   public static final String RFC_1123_DATE = "EEE, dd MMM yyyy HH:mm:ss zzz";

   private final String viewId;
   private String description;
   private Integer timeout;
   private Integer concurrentRequestTimeout;
   private ValueExpression<String> noConversationViewId;
   private String resourceBundleName;
   private boolean switchEnabled = true;
   private boolean validateModel = true;
   private List<Param> parameters = new ArrayList<Param>();
   private List<Input> inputs = new ArrayList<Input>();
   private List<Action> actions = new ArrayList<Action>();
   private Map<String, Navigation> navigations = new HashMap<String, Navigation>();
   private Navigation defaultNavigation;
   private boolean conversationRequired;
   private boolean loginRequired;
   private ConversationControl conversationControl = new ConversationControl();
   private TaskControl taskControl = new TaskControl();
   private ProcessControl processControl = new ProcessControl();
   private ConversationIdParameter conversationIdParameter;
   private List<String> eventTypes = new ArrayList<String>();
   private List<Pattern> rewritePatterns = new ArrayList<Pattern>();
   private List<Header> httpHeaders = new ArrayList<Header>();
   
   private Integer expires;

   /**
    * The scheme (http/https) required by this page.
    */
   private String scheme;
   
   /**
    * Indicates whether this view id has a security restriction.  
    */
   private boolean restricted;
   
   /**
    * A security restriction expression to evaluate when requesting this view id.
    * If the view is restricted but no restriction expression is set, the implied
    * permission restriction will be name="[viewid]", action="[get or post]" 
    */
   private String restriction;
   
   public Page(String viewId)
   {
      this.viewId = viewId;
      if (viewId!=null)
      {
         if (viewId.equals("/debug.xhtml")) 
         {
             switchEnabled = false;
         }
         int loc = viewId.lastIndexOf('.');
         if ( loc>0 && viewId.startsWith("/") )
         {
            this.setResourceBundleName( viewId.substring(1, loc) );
         }
      }
      
      conversationIdParameter = new SyntheticConversationIdParameter();
   }
   
   public java.util.ResourceBundle getResourceBundle()
   {
      String resourceBundleName = getResourceBundleName();
      if (resourceBundleName==null)
      {
         return null;
      }
      else
      {
          return ResourceLoader.instance().loadBundle(resourceBundleName);
      }
   }
   
   @Override
   public String toString()
   {
      return "Page(" + getViewId() + ")";
   }
   
   public String getViewId()
   {
      return viewId;
   }
   
   public String renderDescription()
   {
      return Interpolator.instance().interpolate( getDescription() );
   }
   
   public void setDescription(String description)
   {
      this.description = description;
   }
   
   public String getDescription()
   {
      return description;
   }
   
   public void setTimeout(Integer timeout)
   {
      this.timeout = timeout;
   }
   
   public Integer getTimeout()
   {
      return timeout;
   }
   
   public void setConcurrentRequestTimeout(Integer concurrentRequestTimeout)
   {
      this.concurrentRequestTimeout = concurrentRequestTimeout;
   }
   
   public Integer getConcurrentRequestTimeout()
   {
      return concurrentRequestTimeout;
   }
   
   public void setNoConversationViewId(ValueExpression<String> noConversationViewId)
   {
      this.noConversationViewId = noConversationViewId;
   }
   
   public ValueExpression<String> getNoConversationViewId()
   {
      return noConversationViewId;
   }
   
   public void setResourceBundleName(String resourceBundleName)
   {
      this.resourceBundleName = resourceBundleName;
   }
   
   public String getResourceBundleName()
   {
      return resourceBundleName;
   }
   
   public void setSwitchEnabled(boolean switchEnabled)
   {
      this.switchEnabled = switchEnabled;
   }
   
   public boolean isSwitchEnabled()
   {
      return switchEnabled;
   }
   
   public void setValidateModel(boolean validateModel)
   {
      this.validateModel = validateModel;
   }
   
   /**
    * Indicates whether the model validator (Hibernate Validator) should be
    * registered on the page parameters by default. The default is to add the
    * model validator. This setting can be overridden per page parameter. If
    * parameters are registered on the page programatically, this setting should
    * be honored as the default.
    */
   public boolean isValidateModel()
   {
      return validateModel;
   }
   
   public List<Param> getParameters()
   {
      return parameters;
   }
   
   public Map<String, Navigation> getNavigations()
   {
      return navigations;
   }
   
   public boolean hasDescription()
   {
      return description!=null;
   }
   
   public boolean isConversationRequired()
   {
      return conversationRequired;
   }
   
   public void setConversationRequired(boolean conversationRequired)
   {
      this.conversationRequired = conversationRequired;
   }
   
   public Navigation getDefaultNavigation()
   {
      return defaultNavigation;
   }
   
   public void setDefaultNavigation(Navigation defaultActionOutcomeMapping)
   {
      this.defaultNavigation = defaultActionOutcomeMapping;
   }
   
   public ConversationControl getConversationControl()
   {
      return conversationControl;
   }
   
   public TaskControl getTaskControl()
   {
      return taskControl;
   }
   
   public ProcessControl getProcessControl()
   {
      return processControl;
   }
   
   public List<Action> getActions()
   {
      return actions;
   }
   
   private void checkPermission(FacesContext facesContext, String name)
   {
      if ( isRestricted() && Identity.isSecurityEnabled() )
      {
         // If no expression is configured, create a default one
         if (restriction == null)
         {
            Identity.instance().checkPermission( Pages.getViewId(facesContext), name );
         }
         else
         {
            Identity.instance().checkRestriction(restriction);
         }
      }
   }
   
   /**
    * Check the restore permission.
    */
   public void postRestore(FacesContext facesContext)
   {
      checkPermission(facesContext, "restore");
   }

   /**
    * Call page actions, in order they appear in XML, and
    * handle conversation begin/end. Also check the 
    * render permission.
    */
   public boolean preRender(FacesContext facesContext)
   {
      checkPermission(facesContext, "render");     
     
      sendHeaders(facesContext);
      
      boolean result = false;
      
      getConversationControl().beginOrEndConversation();
      getTaskControl().beginOrEndTask();
      getProcessControl().createOrResumeProcess();
      
      for ( Input in: getInputs() ) in.in();
      
      for (String eventType : eventTypes)
      {
         Events.instance().raiseEvent(eventType);
      }
   
      for ( Action action: getActions() )
      {
         if ( action.isExecutable(facesContext.getRenderKit().getResponseStateManager().isPostback(facesContext)) )
         {
            String outcome = action.getOutcome();
            String fromAction = outcome;
            
            if (outcome==null)
            {
               fromAction = action.getMethodExpression().getExpressionString();
               result = true;
               outcome = Pages.toString( action.getMethodExpression().invoke() );
               UIViewRoot oldViewRoot = facesContext.getViewRoot();
               Pages.handleOutcome(facesContext, outcome, fromAction);
               if (facesContext.getResponseComplete() || oldViewRoot != facesContext.getViewRoot()) {
                  break;
               }
            }
            else
            {
               UIViewRoot oldViewRoot = facesContext.getViewRoot();
               Pages.handleOutcome(facesContext, outcome, fromAction);
               if (facesContext.getResponseComplete() || oldViewRoot != facesContext.getViewRoot()) {
                  break;
               }
            }
         }
      }
      
      return result;
   }

    private void sendHeaders(FacesContext facesContext) {
        Object value = facesContext.getExternalContext().getResponse();
        
        if (value == null || !(value instanceof HttpServletResponse)) {
            return;
        }
        
        HttpServletResponse response = (HttpServletResponse) value;
        for (Header header: httpHeaders) {
            header.sendHeader(response);
        }
        
        if (expires != null) {
            Header.sendHeader(response, 
                              "Expires", 
                              rfc1123Date(new Date(System.currentTimeMillis()+expires*1000)));
        }
    }
   
    private String rfc1123Date(Date when) {
        SimpleDateFormat format = new SimpleDateFormat(RFC_1123_DATE);
       
        return format.format(when);
    }

   public List<Input> getInputs()
   {
      return inputs;
   }
   
   public boolean isRestricted()
   {
      return restricted;
   }
   
   public void setRestricted(boolean restricted)
   {
      this.restricted = restricted;
   }
   public String getRestriction()
   {
      return restriction;
   }
   
   public void setRestriction(String restriction)
   {
      this.restriction = restriction;
   }

   public boolean isLoginRequired()
   {
      return loginRequired;
   }

   public void setLoginRequired(boolean loginRequired)
   {
      this.loginRequired = loginRequired;
   }
   
   public String getScheme()
   {
      return scheme;
   }
   
   public void setScheme(String scheme)
   {
      this.scheme = scheme;
   }
   
   public ConversationIdParameter getConversationIdParameter()
   {
      return conversationIdParameter;
   }
   
   public void setConversationIdParameter(ConversationIdParameter param)
   {
      this.conversationIdParameter = param;
   }

   public List<String> getEventTypes()
   {
      return eventTypes;
   }

   public void addEventType(String eventType)
   {
      if (!Strings.isEmpty(eventType))
      {
         eventTypes.add(eventType);
      }
   }
   
   public List<Pattern> getRewritePatterns() 
   {
      return rewritePatterns;
   }

    //public void setRewritePatterns(List<String> rewritePatterns) {
    //    this.rewritePatterns = rewritePatterns;
    //} 

    public void addRewritePattern(String value) {
        Pattern pattern = new Pattern(viewId, value);
        rewritePatterns.add(pattern);
    }

    public List<Header> getHeaders() {
        return httpHeaders;
    }

    public void setExpires(Integer expires) {
        this.expires = expires;   
    }

}
