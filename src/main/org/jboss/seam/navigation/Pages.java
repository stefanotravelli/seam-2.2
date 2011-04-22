package org.jboss.seam.navigation;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.model.DataModel;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.MethodExpression;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.core.Init;
import org.jboss.seam.core.Interpolator;
import org.jboss.seam.core.Manager;
import org.jboss.seam.core.ResourceLoader;
import org.jboss.seam.deployment.DotPageDotXmlDeploymentHandler;
import org.jboss.seam.deployment.FileDescriptor;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Validation;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.pageflow.Pageflow;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;
import org.jboss.seam.util.Resources;
import org.jboss.seam.util.Strings;
import org.jboss.seam.util.XML;
import org.jboss.seam.web.Parameters;

/**
 * Holds metadata for pages defined in pages.xml, including
 * page actions and page descriptions.
 * 
 * @author Gavin King
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Name("org.jboss.seam.navigation.pages")
@Install(precedence=BUILT_IN, classDependencies="javax.faces.context.FacesContext")
@Startup
public class Pages
{   
   private static final LogProvider log = Logging.getLogProvider(Pages.class);

   private ValueExpression<String> noConversationViewId;
   private String loginViewId;
     
   private Integer httpPort;
   private Integer httpsPort;
   
   private Map<String, Page> pagesByViewId;  
   private Map<String, List<Page>> pageStacksByViewId;   
   private Map<String, ConversationIdParameter> conversations;    
   
   private String[] resources = { "/WEB-INF/pages.xml" };
 
   private SortedSet<String> wildcardViewIds = new TreeSet<String>( 
         new Comparator<String>() 
         {
            public int compare(String x, String y)
            {
               if ( x.length()<y.length() ) return -1;
               if ( x.length()> y.length() ) return 1;
               return x.compareTo(y);
            }
         } 
      );

   @Create
   public void create()
   {
      if (DotPageDotXmlDeploymentHandler.instance() != null)
      {
          initialize(DotPageDotXmlDeploymentHandler.instance().getResources());
      }
      else
      {
          initialize();
      }
   }
   
   
   
   public void initialize()
   {
       initialize(null);
   }
   
   public void initialize(Set<FileDescriptor> fileNames)
   {
      pagesByViewId = Collections.synchronizedMap(new HashMap<String, Page>());   
      pageStacksByViewId = Collections.synchronizedMap(new HashMap<String, List<Page>>());   
      conversations = Collections.synchronizedMap(new HashMap<String, ConversationIdParameter>());

      for (String resource: resources) 
      {
         InputStream stream = ResourceLoader.instance().getResourceAsStream(resource);      
         if (stream==null) 
         {
            log.debug("no pages.xml file found: " + resource);
         } else {
            log.debug("reading pages.xml file: " + resource);
            try {
                parse(stream);
            } finally {
                Resources.closeStream(stream);
            }
         }
      }
      
      if (fileNames != null)
      {
          parsePages(fileNames);
      }
   }
   
   private void parsePages(Set<FileDescriptor> files)
   {
      for (FileDescriptor file : files)  
      {
         String fileName = file.getName();
         String viewId = "/" + fileName.substring(0,fileName.length()-".page.xml".length()) + ".xhtml"; // needs more here
         
         InputStream stream = null;
         try
         {
            stream = file.getUrl().openStream();
         }
         catch (IOException exception)
         {
            // No-op
         }
         if (stream != null) 
         {
            log.debug("reading pages.xml file: " + fileName);
            try {
                parse(stream,viewId);
            } finally {
                Resources.closeStream(stream);
            }
         } 
     }
   }
   
   /**
    * Run any navigation rule defined in pages.xml
    * 
    * @param actionExpression the action method binding expression
    * @param actionOutcomeValue the outcome of the action method
    * @return true if a navigation rule was found
    */
   public boolean navigate(FacesContext context, String actionExpression, String actionOutcomeValue)
   {
      String viewId = getViewId(context);
      if (viewId!=null)
      {
         List<Page> stack = getPageStack(viewId);
         for (int i=stack.size()-1; i>=0; i--)
         {
            Page page = stack.get(i);
            Navigation navigation = page.getNavigations().get(actionExpression);
            if (navigation==null)
            {
               navigation = page.getDefaultNavigation();
            }
            
            if ( navigation!=null && navigation.navigate(context, actionOutcomeValue) ) return true;  
            
         }
      }
      return false;
   }
   /**
    * Get the Page object for the given view id.
    * 
    * @param viewId a JSF view id
    */
   public Page getPage(String viewId)
   {
      if (viewId==null)
      {
         //for tests
         return new Page(viewId);
      }
      else
      {
         Page result = getCachedPage(viewId);
         if (result==null)
         {
            return createPage(viewId);
         }
         else
         {
            return result;
         }
      }
   }
   
   /**
    * Create a new default Page object for a JSF view id
    */
   private Page createPage(String viewId)
   {
      Page result = new Page(viewId);
      pagesByViewId.put(viewId, result);
      return result;
   }
   
   private Page getCachedPage(String viewId)
   {
      return pagesByViewId.get(viewId);
   }
   
   /**
    * Get the stack of Page objects, from least specific to 
    * most specific, that match the given view id.
    * 
    * @param viewId a JSF view id
    */
   protected List<Page> getPageStack(String viewId)
   {
      List<Page> stack = pageStacksByViewId.get(viewId);
      if (stack==null)
      {
         stack = createPageStack(viewId);
         pageStacksByViewId.put(viewId, stack);
      }
      return stack;
   }
   /**
    * Create the stack of pages that match a JSF view id
    */
   private List<Page> createPageStack(String viewId)
   {
      List<Page> stack = new ArrayList<Page>(1);
      if ( viewId!=null && !isDebugPage(viewId) )
      {
         for (String wildcard: wildcardViewIds)
         {
            if ( viewId.startsWith( wildcard.substring(0, wildcard.length()-1) ) )
            {
               stack.add( getPage(wildcard) );
            }
         }
      }
      Page page = getPage(viewId);
      if (page!=null) stack.add(page);
      return stack;
   }
   
   /**
    * Call page actions, check permissions and validate the existence 
    * of a conversation for pages which require a long-running 
    * conversation, starting with the most general view id, ending at 
    * the most specific. Also perform redirection to the required
    * scheme if necessary.
    */
   public boolean preRender(FacesContext facesContext)
   {
      String viewId = getViewId(facesContext);
      
      //redirect to HTTPS if necessary
      String requestScheme = getRequestScheme(facesContext);
      if ( requestScheme!=null )
      {
         String scheme = getScheme(viewId);
         if ( scheme!=null && !requestScheme.equals(scheme) )
         {
            Manager.instance().redirect(viewId);
            return false;
         }
      }
      
      //apply the datamodelselection passed by s:link or s:button
      //before running any actions
      selectDataModelRow(facesContext);

      //redirect if necessary
      List<Page> pageStack = getPageStack(viewId);
      for ( Page page: pageStack )
      {         
         if ( isNoConversationRedirectRequired(page) )
         {
            redirectToNoConversationView();
            return false;
         }
         else if ( isLoginRedirectRequired(viewId, page) )
         {
            redirectToLoginView();
            return false;
         }
      }

      boolean result = callAction(facesContext); 

      //If responseComplete then we're probably doing a redirect so don't call the page actions now. 
      if (!facesContext.getResponseComplete()) { 
          String newViewId = getViewId(facesContext); 

          for ( Page page: getPageStack(newViewId) ) { 
              if ( isNoConversationRedirectRequired(page) ) { 
                  redirectToNoConversationView(); 
                  return false; 
              } else if ( isLoginRedirectRequired(newViewId, page) ) { 
                  redirectToLoginView(); 
                  return false; 
              } 
          } 

          //run the page actions, check permissions, 
          //handle conversation begin/end 

          for ( Page page: getPageStack(newViewId) ) { 
              result = page.preRender(facesContext) || result; 
          } 
      } 
      
      return result;
   }
   
   /**
    * Look for a DataModel row selection in the request parameters,
    * and apply it to the DataModel.
    */
   protected void selectDataModelRow(FacesContext facesContext)
   {
      String dataModelSelection = facesContext.getExternalContext()
               .getRequestParameterMap().get("dataModelSelection");
      if (dataModelSelection!=null)
      {
         int colonLoc = dataModelSelection.indexOf(':');
         int bracketLoc = dataModelSelection.indexOf('[');
         if (colonLoc>0 && bracketLoc>colonLoc)
         {
            String var = dataModelSelection.substring(0, colonLoc);
            String name = dataModelSelection.substring(colonLoc+1, bracketLoc);
            int index = Integer.parseInt( dataModelSelection.substring( bracketLoc+1, dataModelSelection.length()-1 ) );
            Object value = Component.getInstance(name, true);
            if (value!=null)
            {
               DataModel dataModel = (DataModel) value;
               if ( index<dataModel.getRowCount() )
               {
                  dataModel.setRowIndex(index);
                  Contexts.getEventContext().set( var, dataModel.getRowData() );
               }
               else
               {
                  log.debug("DataModel row was unavailable");
                  Contexts.getEventContext().remove(var);
               }
            }
         }
      }
   }
   
   /**
    * Check permissions and validate the existence of a conversation
    * for pages which require a long-running conversation, starting
    * with the most general view id, ending at the most specific.
    * Finally apply page parameters to the model.
    */
   public void postRestore(FacesContext facesContext)
   {
      //first store the page parameters into the viewroot, so 
      //that if a login redirect occurs, or if a failure
      //occurs while validating of applying to the model, we can 
      //still make Redirect.captureCurrentView() work.
      storeRequestStringValuesInPageContext(facesContext);
      
      //check if we need to redirect
      String viewId = getViewId(facesContext);      
      for ( Page page: getPageStack(viewId) )
      {         
         if ( isLoginRedirectRequired(viewId, page) )
         {
            redirectToLoginView();
            return;
         }
         else if ( isNoConversationRedirectRequired(page) )
         {
            redirectToNoConversationView();
            return;
         }
         else
         {
            //if we are about to proceed to the action
            //phase, check the permission.
            if ( !facesContext.getRenderResponse() )
            {
               page.postRestore(facesContext);
            }
         }
      }

      //now validate the values we just stored in
      //the view root, after the redirect checking
      if ( convertAndValidateStringValuesInPageContext(facesContext) ) 
      {
         Validation.instance().fail();
         //and don't apply them to the model
      }
      else
      {   
         //finally apply page parameters to the model
         //(after checking permissions)
         applyConvertedValidatedValuesToModel(facesContext);
      }
   }
   
   /**
    * Check if a login redirect is required for the current FacesContext
    * 
    * @param facesContext The faces context containing the view ID
    * @return boolean Returns true if a login redirect is required
    */
   public boolean isLoginRedirectRequired(FacesContext facesContext)
   {
      String viewId = getViewId(facesContext);      
      for ( Page page: getPageStack(viewId) )
      {         
         if ( isLoginRedirectRequired(viewId, page) ) return true;
      }
      return false;
   }
   
   private boolean isNoConversationRedirectRequired(Page page)
   {
      return page.isConversationRequired() && 
            !Manager.instance().isLongRunningOrNestedConversation();
   }
   
   private boolean isLoginRedirectRequired(String viewId, Page page)
   {
      return page.isLoginRequired() && 
            !viewId.equals( getLoginViewId() ) && 
            !Identity.instance().isLoggedIn();
   }
   
   public String getRequestScheme(FacesContext facesContext)
   {
      String requestUrl = getRequestUrl(facesContext);
      if (requestUrl==null)
      {
         return null;
      }
      else
      {
         int idx = requestUrl.indexOf(':');
         return idx<0 ? null : requestUrl.substring(0, idx);
      }
   }
   
   public String encodeScheme(String viewId, FacesContext context, String url)
   {
      String scheme = getScheme(viewId);
      if (scheme != null)
      {
         String requestUrl = getRequestUrl(context);
         if (requestUrl!=null)
         {
            try
            {
               URL serverUrl = new URL(requestUrl);
               
               StringBuilder sb = new StringBuilder();
               sb.append(scheme);
               sb.append("://");
               sb.append(serverUrl.getHost());
               
               if ("http".equals(scheme) && httpPort != null)
               {
                  sb.append(":");
                  sb.append(httpPort);
               }
               else if ("https".equals(scheme) && httpsPort != null)
               {
                  sb.append(":");
                  sb.append(httpsPort);
               }
               else if (serverUrl.getPort() != -1)
               {
                  sb.append(":");
                  sb.append(serverUrl.getPort());
               }
               
               if (!url.startsWith("/")) sb.append("/");
               
               sb.append(url);
               
               url = sb.toString();
            }
            catch (MalformedURLException ex) 
            {
               throw new RuntimeException(ex);
            }
         }
      }
      return url;   
   }
   
   private static String getRequestUrl(FacesContext facesContext)
   {
      Object request = facesContext.getExternalContext().getRequest(); 
      if (request instanceof HttpServletRequest) 
      {
         return ( (HttpServletRequest) request).getRequestURL().toString();
      }
      else
      {
         return null;
      }
   }
   
   public void redirectToLoginView()
   {
      notLoggedIn();
      
      String loginViewId = getLoginViewId();
      if (loginViewId==null)
      {
         throw new NotLoggedInException();
      }
      else
      {
         Manager.instance().redirect(loginViewId);
      }
   }
   
   public void redirectToNoConversationView()
   {
      noConversation();
      
      //stuff from jPDL takes precedence
      org.jboss.seam.faces.FacesPage facesPage = org.jboss.seam.faces.FacesPage.instance();
      String pageflowName = facesPage.getPageflowName();
      String pageflowNodeName = facesPage.getPageflowNodeName();
      
      String noConversationViewId = null;
      if (pageflowName==null || pageflowNodeName==null)
      {
         String viewId = Pages.getCurrentViewId();
         noConversationViewId = getNoConversationViewId(viewId);
      }
      else
      {
         noConversationViewId = Pageflow.instance().getNoConversationViewId(pageflowName, pageflowNodeName);
      }
      
      if (noConversationViewId!=null)
      {
         Manager.instance().redirect(noConversationViewId);
      }
   }
   
   public String getScheme(String viewId)
   {
      List<Page> stack = getPageStack(viewId);
      for ( int i = stack.size() - 1; i >= 0; i-- )
      {
         Page page = stack.get(i);
         if (page.getScheme() != null) return page.getScheme();
      }
      return null;
   }

   public boolean hasDescription(String viewId)
   {
      return getDescription(viewId)!=null;
   }

   public String getDescription(String viewId)
   {
      List<Page> stack = getPageStack(viewId);
      for ( int i = stack.size() - 1; i >= 0; i-- )
      {
         Page page = stack.get(i);
         if (page.hasDescription()) return page.getDescription();
      }
      return null;
   }

   public String renderDescription(String viewId)
   {
      return Interpolator.instance().interpolate( getDescription(viewId) );
   }
   
   protected void noConversation()
   {
      Events.instance().raiseEvent("org.jboss.seam.noConversation");
      
      FacesMessages.instance().addFromResourceBundleOrDefault( 
            StatusMessage.Severity.WARN, 
            "org.jboss.seam.NoConversation", 
            "The conversation ended, timed out or was processing another request" 
         );
   }

   protected void notLoggedIn()
   {
      //    TODO - Deprecated, remove for next major release
      Events.instance().raiseEvent("org.jboss.seam.notLoggedIn");
      Events.instance().raiseEvent(Identity.EVENT_NOT_LOGGED_IN);
   }

   public static String toString(Object returnValue)
   {
      return returnValue == null ? null : returnValue.toString();
   }
   
   /**
    * Call the JSF navigation handler
    */
   public static void handleOutcome(FacesContext facesContext, String outcome, String fromAction)
   {
      facesContext.getApplication().getNavigationHandler()
            .handleNavigation(facesContext, fromAction, outcome);
      //after every time that the view may have changed,
      //we need to flush the page context, since the 
      //attribute map is being discarder
      Contexts.getPageContext().flush();
   }
   
   public static Pages instance()
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("No active application context");
      }
      return (Pages) Component.getInstance(Pages.class, ScopeType.APPLICATION);
   }
   
   /**
    * Call the action requested by s:link or s:button.
    */
   @SuppressWarnings("deprecation")
   private static boolean callAction(FacesContext facesContext)
   {
      //TODO: refactor with Pages.instance().callAction()!!
      
      boolean result = false;
      
      String outcome = facesContext.getExternalContext()
            .getRequestParameterMap().get("actionOutcome");
      String fromAction = outcome;

      String decodedOutcome = null;
      if (outcome != null)
      {
         decodedOutcome = URLDecoder.decode(outcome);
      }

      if (decodedOutcome != null && (decodedOutcome.indexOf('#') >= 0 || decodedOutcome.indexOf('{') >= 0) ){
         throw new IllegalArgumentException("EL expressions are not allowed in actionOutcome parameter");
      }
      
      if (outcome==null)
      {
         String actionId = facesContext.getExternalContext()
               .getRequestParameterMap().get("actionMethod");
         if (actionId!=null)
         {
            String decodedActionId = URLDecoder.decode(actionId);
            if (decodedActionId != null && (decodedActionId.indexOf('#') >= 0 || decodedActionId.indexOf('{') >= 0) ){
               throw new IllegalArgumentException("EL expressions are not allowed in actionMethod parameter");
            }
            if ( !SafeActions.instance().isActionSafe(actionId) ) return result;
            String expression = SafeActions.toAction(actionId);
            result = true;
            MethodExpression actionExpression = Expressions.instance().createMethodExpression(expression);
            outcome = toString( actionExpression.invoke() );
            fromAction = expression;
            handleOutcome(facesContext, outcome, fromAction);
         }
      }
      else
      {
         handleOutcome(facesContext, outcome, fromAction);
      }
      
      return result;
   }
   
   /**
    * Build a list of page-scoped resource bundles, from most
    * specific view id, to most general.
    */
   public List<ResourceBundle> getResourceBundles(String viewId)
   {
      List<ResourceBundle> result = new ArrayList<ResourceBundle>(1);
      List<Page> stack = getPageStack(viewId);
      for (int i=stack.size()-1; i>=0; i--)
      {
         Page page = stack.get(i);
         ResourceBundle bundle = page.getResourceBundle();
         if ( bundle!=null ) result.add(bundle);
      }
      return result;
   }
   
   /**
    * Get the values of any page parameters by evaluating the value bindings
    * against the model and converting to String.
    * 
    * @param viewId the JSF view id
    * @param overridden excluded parameters
    * @return a map of page parameter name to String value
    */
   public Map<String, Object> getStringValuesFromModel(FacesContext facesContext, String viewId, Set<String> overridden)
   {
      Map<String, Object> parameters = new HashMap<String, Object>();
      for ( Page page: getPageStack(viewId) )
      {
         for ( Param pageParameter: page.getParameters() )
         {
            if ( !overridden.contains( pageParameter.getName() ) )
            {
               String value = null;
               if ( pageParameter.getValueExpression()==null )
               {                  
                  if (Contexts.isPageContextActive()) {
                      value = (String) Contexts.getPageContext().get(pageParameter.getName());
                  }
               }
               else
               {
                  value = pageParameter.getStringValueFromModel(facesContext);
               }
               if (value!=null) 
               {
                  parameters.put( pageParameter.getName(), value );
               }
            }
         }
      }
      return parameters;
   }
   
   private void storeRequestStringValuesInPageContext(FacesContext facesContext)
   {
      Parameters parameters = Parameters.instance();
      if (parameters!=null) //for unit tests
      {
         Map<String, String[]> requestParameters = parameters.getRequestParameters();
         for ( Page page: getPageStack( getViewId(facesContext) ) )
         {
            for ( Param pageParameter: page.getParameters() )
            {
               String value = pageParameter.getStringValueFromRequest(facesContext, requestParameters);
               if (value==null)
               {
                  //this should not be necessary, were it not for a MyFaces bug
                  if ( facesContext.getRenderResponse() ) //ie. for a non-faces request
                  {
                     Contexts.getPageContext().remove( pageParameter.getName() );
                  }
               }
               else
               {
                  Contexts.getPageContext().set( pageParameter.getName(), value );
               }
            }
         }
      }
   }
   
   /**
    * Convert and validate page parameters passed as view root attributes or request parameters
    */
   private boolean convertAndValidateStringValuesInPageContext(FacesContext facesContext)
   {
      boolean validationFailed = false;
      for ( Page page: getPageStack( getViewId(facesContext) ) )
      {
         for ( Param pageParameter: page.getParameters() )
         {  
            try
            {
               String value = (String) Contexts.getPageContext().get( pageParameter.getName() );
               if (value!=null)
               {
                  Object convertedValue = pageParameter.convertValueFromString(facesContext, value);
                  pageParameter.validateConvertedValue(facesContext, convertedValue);
                  Contexts.getEventContext().set( pageParameter.getName(), convertedValue );
               }
            }
            catch (ValidatorException ve)
            {
               if (ve.getFacesMessage() != null)
               {
                  facesContext.addMessage(null, ve.getFacesMessage());
               }
               
               validationFailed = true;
            }
            catch (ConverterException ce)
            {
               if (ce.getFacesMessage() != null)
               {
                  facesContext.addMessage( null, ce.getFacesMessage() );
               }
               validationFailed = true;
            }
         }
      }
      return validationFailed;
   }
   
   /**
    * Apply page parameters passed as view root attributes or request parameters to the model
    */
   private void applyConvertedValidatedValuesToModel(FacesContext facesContext)
   {
      String viewId = getViewId(facesContext);
      for ( Page page: getPageStack(viewId) )
      {
         for ( Param pageParameter: page.getParameters() )
         {         
            ValueExpression valueExpression = pageParameter.getValueExpression();
            if (valueExpression!=null)
            {
               Object object = Contexts.getEventContext().get( pageParameter.getName() );
               if (object!=null)
               {
                  valueExpression.setValue(object);
               }
            }
         }
      }
   }

   /**
    * Get the page parameter values that were passed in the original request from
    * the PAGE context
    */
   public Map<String, Object> getStringValuesFromPageContext(FacesContext facesContext)
   {
      Map<String, Object> parameters = new HashMap<String, Object>();
      String viewId = getViewId(facesContext);
      for ( Page page: getPageStack(viewId) )
      {
         for ( Param pageParameter: page.getParameters() )
         {
            Object object = Contexts.getPageContext().get( pageParameter.getName() );
            if (object!=null)
            {
               parameters.put( pageParameter.getName(), object );
            }
         }
      }
      return parameters;
   }
   
   /**
    * Update the page parameter values stored in the PAGE context with the current
    * values of the mapped attributes of the model
    */
   public void updateStringValuesInPageContextUsingModel(FacesContext facesContext)
   {
      for ( Page page: getPageStack( getViewId(facesContext) ) )
      {
         for ( Param pageParameter: page.getParameters() )
         {
            if ( pageParameter.getValueExpression()!=null )
            {
               String value = pageParameter.getStringValueFromModel(facesContext);
               if (value==null)
               {
                  Contexts.getPageContext().remove( pageParameter.getName() );
               }
               else
               {
                  Contexts.getPageContext().set( pageParameter.getName(), value );
               }
            }
         }
      }
   }
   
   /**
    * Encode page parameters into a URL
    * 
    * @param url the base URL
    * @param viewId the JSF view id of the page
    * @return the URL with parameters appended
    */
   public String encodePageParameters(FacesContext facesContext, String url, String viewId)
   {
      return encodePageParameters(facesContext, url, viewId, Collections.EMPTY_SET);
   }
   
   /**
    * Encode page parameters into a URL
    * 
    * @param url the base URL
    * @param viewId the JSF view id of the page
    * @param overridden excluded parameters
    * @return the URL with parameters appended
    */
   public String encodePageParameters(FacesContext facesContext, String url, String viewId, Set<String> overridden)
   {
      Map<String, Object> parameters = getStringValuesFromModel(facesContext, viewId, overridden);
      return Manager.instance().encodeParameters(url, parameters);
   }
   
   /**
    * Search for a defined no-conversation-view-id, beginning with
    * the most specific view id, then wildcarded view ids, and 
    * finally the global setting
    */
   public String getNoConversationViewId(String viewId)
   {
      List<Page> stack = getPageStack(viewId);
      for (int i=stack.size()-1; i>=0; i--)
      {
         Page page = stack.get(i);
         if (page.getNoConversationViewId() != null)
         {
            String noConversationViewId = page.getNoConversationViewId().getValue();
            if (noConversationViewId!=null)
            {
               return noConversationViewId;
            }
         }
      }
      return this.noConversationViewId != null ? this.noConversationViewId.getValue() : null;
   }
   
   /**
    * Search for a defined conversation timeout, beginning with
    * the most specific view id, then wildcarded view ids, and 
    * finally the global setting from Manager
    */
   public Integer getTimeout(String viewId)
   {
      List<Page> stack = getPageStack(viewId);
      for (int i=stack.size()-1; i>=0; i--)
      {
         Page page = stack.get(i);
         Integer timeout = page.getTimeout();
         if (timeout!=null)
         {
            return timeout;
         }
      }
      return Manager.instance().getConversationTimeout();
   }
   
   /**
    * Search for a defined concurrent request timeout, beginning with
    * the most specific view id, then wildcarded view ids, and 
    * finally the global setting from Manager
    */
   public Integer getConcurrentRequestTimeout(String viewId)
   {
      List<Page> stack = getPageStack(viewId);
      for (int i=stack.size()-1; i>=0; i--)
      {
         Page page = stack.get(i);
         Integer concurrentRequestTimeout = page.getConcurrentRequestTimeout();
         if (concurrentRequestTimeout!=null)
         {
            return concurrentRequestTimeout;
         }
      }
      return Manager.instance().getConcurrentRequestTimeout();
   }
   
   public static String getSuffix()
   {
      String defaultSuffix = FacesContext.getCurrentInstance().getExternalContext()
            .getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
      return defaultSuffix == null ? ViewHandler.DEFAULT_SUFFIX : defaultSuffix;
   }
   
   /**
    * Parse a pages.xml file
    */
   private void parse(InputStream stream)
   {
      Element root = getDocumentRoot(stream);
      if (noConversationViewId==null) //let the setting in components.xml override the pages.xml
      {
         String noConversationViewIdString = root.attributeValue("no-conversation-view-id");
         if (noConversationViewIdString != null)
         {
            noConversationViewId = Expressions.instance().createValueExpression(noConversationViewIdString, String.class);
         }
      }
      if (loginViewId==null) //let the setting in components.xml override the pages.xml
      {
         loginViewId = root.attributeValue("login-view-id");
      }
      
      if (httpPort == null)
      {
         try
         {
            String value = root.attributeValue("http-port");
            if (!Strings.isEmpty(value))
            {
               httpPort = Integer.parseInt(value);
            }
         }
         catch (NumberFormatException ex)
         {
            throw new IllegalStateException("Invalid value specified for http-port attribute in pages.xml");
         }
      }
      
      if (httpsPort == null)
      {
         try
         {
            String value = root.attributeValue("https-port");
            if (!Strings.isEmpty(value))
            {
               httpsPort = Integer.parseInt(value);
            }
         }
         catch (NumberFormatException ex)
         {
            throw new IllegalStateException("Invalid valid specified for https-port attribute in pages.xml");
         }
      }
      
      List<Element> elements = root.elements("conversation");
      for (Element conversation : elements)
      {
         parseConversation(conversation, conversation.attributeValue("name"));
      }
      
      elements = root.elements("page");
      for (Element page: elements)
      {
         parse( page, page.attributeValue("view-id") );
      } 
   }
   
   /**
    * Parse a viewId.page.xml file
    */
   private void parse(InputStream stream, String viewId)
   {
      parse( getDocumentRoot(stream), viewId );
   }
   
   /**
    * Get the root element of the document
    */
   private static Element getDocumentRoot(InputStream stream)
   {
      try
      {
         return XML.getRootElement(stream);
      }
      catch (DocumentException de)
      {
         throw new RuntimeException(de);
      }
   }
   
   private void parseConversation(Element element, String name)
   {
      if (name == null)
      {
         throw new IllegalStateException("Must specify name for <conversation/> declaration");
      }
      
      if (conversations.containsKey(name))
      {
         throw new IllegalStateException("<conversation/> declaration already exists for [" + name + "]");
      }
      
      NaturalConversationIdParameter param = new NaturalConversationIdParameter(name, 
               element.attributeValue("parameter-name"), 
               element.attributeValue("parameter-value"));
      
      conversations.put(name, param);
   }
   
   /**
    * Parse a page element and add a Page to the map
    */
   private void parse(Element element, String viewId)
   {
      if (viewId==null)
      {
         throw new IllegalStateException("Must specify view-id for <page/> declaration");
      }
      
      if ( viewId.endsWith("*") )
      {
         wildcardViewIds.add(viewId);
      }
      Page page = new Page(viewId);
      pagesByViewId.put(viewId, page);
      
      parsePage(page, element, viewId);
      parseConversationControl( element, page.getConversationControl() );
      parseTaskControl(element, page.getTaskControl());
      parseProcessControl(element, page.getProcessControl());
      List<Element> children = element.elements("param");
      for (Element param: children)
      {
         page.getParameters().add( parseParam(param, page.isValidateModel()) );
      }
      
      List<Element> moreChildren = element.elements("navigation");
      for (Element fromAction: moreChildren)
      {
         parseActionNavigation(page, fromAction);
      }
      
      Element restrict = element.element("restrict");
      if (restrict != null)
      {
         page.setRestricted(true);
         String expr = restrict.getTextTrim();
         if ( !Strings.isEmpty(expr) ) page.setRestriction(expr);
      }
      
      List<Element> headers = element.elements("header");
      for (Element header: headers) {
         page.getHeaders().add(parseHeader(header));
      }
   }
   
   public ConversationIdParameter getConversationIdParameter(String conversationName)
   {
      return conversations.get(conversationName);
   }
   
   /**
    * Parse the attributes of page
    */
   private Page parsePage(Page page, Element element, String viewId)
   {
      
      page.setSwitchEnabled( !"disabled".equals( element.attributeValue("switch") ) );
      
      Element optionalElement = element.element("description");
      String description = optionalElement==null ? 
               element.getTextTrim() : optionalElement.getTextTrim();
      if (description!=null && description.length()>0)
      {
         page.setDescription(description);
      }
      
      String timeoutString = element.attributeValue("timeout");
      if (timeoutString!=null)
      {
         page.setTimeout(Integer.parseInt(timeoutString));
      }
      
      String concurrentRequestTimeoutString = element.attributeValue("concurrent-request-timeout");
      if (concurrentRequestTimeoutString!=null)
      {
         page.setConcurrentRequestTimeout(Integer.parseInt(concurrentRequestTimeoutString));
      }
      
      String noConversationViewIdString = element.attributeValue("no-conversation-view-id");
      if (noConversationViewIdString != null)
      {
         page.setNoConversationViewId(Expressions.instance().createValueExpression(noConversationViewIdString, String.class));
      }
      page.setConversationRequired(Boolean.parseBoolean(element.attributeValue("conversation-required")));
      page.setLoginRequired(Boolean.parseBoolean(element.attributeValue("login-required")));
      page.setScheme(element.attributeValue("scheme"));
      
      String expiresValue = element.attributeValue("expires");
      if (expiresValue != null) {
           page.setExpires(Integer.parseInt(expiresValue));
      }
      
      ConversationIdParameter param = conversations.get( element.attributeValue("conversation") );
      if (param != null) page.setConversationIdParameter(param);
      

      List<Element> patterns = element.elements("rewrite");
      for (Element pattern: patterns) {
           page.addRewritePattern(pattern.attributeValue("pattern"));
      }
      
      List<Element> events = element.elements("raise-event");
      for (Element eventElement : events)
      {
         page.addEventType( eventElement.attributeValue("type") );
      }
      
      Action action = parseAction(element, "action", false);
      if (action!=null) page.getActions().add(action);
      List<Element> childElements = element.elements("action");
      for (Element childElement: childElements)
      {
         page.getActions().add( parseAction(childElement, "execute", true) );
      }
            
      String bundle = element.attributeValue("bundle");
      if (bundle!=null)
      {
         page.setResourceBundleName(bundle);
      }
      List<Element> moreChildElements = element.elements("in");
      for (Element child: moreChildElements)
      {
         Input input = new Input();
         input.setName( child.attributeValue("name") );
         input.setValue( Expressions.instance().createValueExpression( child.attributeValue("value") ) );
         String scopeName = child.attributeValue("scope");
         if (scopeName!=null)
         {
            input.setScope( ScopeType.valueOf( scopeName.toUpperCase() ) );
         }
         page.getInputs().add(input);
      }
      // by default the model is validated by Hibernate validator; this attribute is used to disable that feature
      // this setting can be overridden at the param level
      String validateModelStr = element.attributeValue("validate-model");
      if (validateModelStr != null)
      {
         page.setValidateModel(Boolean.parseBoolean(validateModelStr));
      }
      
      return page;
   }
   
   private static Action parseAction(Element element, String actionAtt, boolean conditionalsAllowed)
   {
      Action action = new Action();
      String methodExpression = element.attributeValue(actionAtt);
      if (methodExpression==null) return null;
      if ( methodExpression.startsWith("#{") )
      {
         action.setMethodExpression( Expressions.instance().createMethodExpression(methodExpression) );
      }
      else
      {
         action.setOutcome(methodExpression);
      }
      
      if (conditionalsAllowed)
      {
         String expression = element.attributeValue("if");
         if (expression!=null)
         {
            action.setValueExpression( Expressions.instance().createValueExpression(expression) );
         }
         action.setOnPostback(!"false".equals(element.attributeValue("on-postback")));
      }
      return action;
   }
   
   /**
    * Parse end-conversation (and end-task) and begin-conversation (start-task and begin-task) 
    *
    */
   private static void parseConversationControl(Element element, ConversationControl control)
   {
      Element endConversation = element.element("end-conversation");
      endConversation = endConversation == null ? element.element("end-task") : endConversation;
      if ( endConversation!=null )
      {
         control.setEndConversation(true);
         control.setEndConversationBeforeRedirect( Boolean.parseBoolean( endConversation.attributeValue("before-redirect") ) );
         control.setEndRootConversation( Boolean.parseBoolean( endConversation.attributeValue("root") ) );
         String expression = endConversation.attributeValue("if");
         if (expression!=null)
         {
            control.setEndConversationCondition( Expressions.instance().createValueExpression(expression, Boolean.class) );
         }
      }
      
      Element beginConversation = element.element("begin-conversation");
      beginConversation = beginConversation == null ? element.element("begin-task") : beginConversation;
      beginConversation = beginConversation == null ? element.element("start-task") : beginConversation;
      if ( beginConversation!=null )
      {
         control.setBeginConversation(true);
         control.setJoin( Boolean.parseBoolean( beginConversation.attributeValue("join") ) );
         control.setNested( Boolean.parseBoolean( beginConversation.attributeValue("nested") ) );
         control.setPageflow( beginConversation.attributeValue("pageflow") );
         control.setConversationName( beginConversation.attributeValue("conversation") );
         String flushMode = beginConversation.attributeValue("flush-mode");
         if (flushMode!=null)
         {
            control.setFlushMode( FlushModeType.valueOf( flushMode.toUpperCase() ) );
         }
         String expression = beginConversation.attributeValue("if");
         if (expression!=null)
         {
            control.setBeginConversationCondition( Expressions.instance().createValueExpression(expression, Boolean.class) );
         }
      }
      
      if ( control.isBeginConversation() && control.isEndConversation() )
      {
         throw new IllegalStateException("cannot use both <begin-conversation/> and <end-conversation/>");
      }
   }
   
   /**
    * Parse begin-task, start-task and end-task
    */
   private static void parseTaskControl(Element element, TaskControl control)
   {
      Element endTask = element.element("end-task");
      if ( endTask!=null )
      {
         control.setEndTask(true);
         String transition = endTask.attributeValue("transition");
         if (transition != null)
         {
            control.setTransition( Expressions.instance().createValueExpression(transition, String.class) );
         }
      }
      
      Element beginTask = element.element("begin-task");
      if ( beginTask!=null )
      {
         control.setBeginTask(true);
         String taskId = beginTask.attributeValue("task-id");
         if (taskId==null)
         {
           taskId = "#{param.taskId}";
         }
         control.setTaskId( Expressions.instance().createValueExpression(taskId, Long.class) );
      }
      
      Element startTask = element.element("start-task");
      if ( startTask!=null )
      {
         control.setStartTask(true);
         String taskId = startTask.attributeValue("task-id");
         if (taskId==null)
         {
           taskId = "#{param.taskId}";
         }
         control.setTaskId( Expressions.instance().createValueExpression(taskId, Long.class) );
      }
      
      if ( control.isBeginTask() && control.isEndTask() )
      {
         throw new IllegalStateException("cannot use both <begin-task/> and <end-task/>");
      }
      else if ( control.isBeginTask() && control.isStartTask() )
      {
          throw new IllegalStateException("cannot use both <start-task/> and <begin-task/>");
       }
      else if ( control.isStartTask() && control.isEndTask() )
      {
           throw new IllegalStateException("cannot use both <start-task/> and <end-task/>");
       }
   }
   
   /**
    * Parse create-process and end-process
    */
   private static void parseProcessControl(Element element, ProcessControl control)
   {
      Element createProcess = element.element("create-process");
      if ( createProcess!=null )
      {
         control.setCreateProcess(true);
         control.setDefinition( createProcess.attributeValue("definition") );
      }
      
      Element resumeProcess = element.element("resume-process");
      if ( resumeProcess!=null )
      {
         control.setResumeProcess(true);
         String processId = resumeProcess.attributeValue("process-id");
         if (processId==null)
         {
           processId = "#{param.processId}";
         }
         control.setProcessId( Expressions.instance().createValueExpression(processId, Long.class) );
      }
      
      if ( control.isCreateProcess() && control.isResumeProcess() )
      {
         throw new IllegalStateException("cannot use both <create-process/> and <resume-process/>");
      }
   }
   
   private static void parseEvent(Element element, Rule rule)
   {
      List<Element> events = element.elements("raise-event");
      for (Element eventElement : events)
      {
         rule.addEventType( eventElement.attributeValue("type") );
      }
   }
   
   /**
    * Parse navigation
    */
   private static void parseActionNavigation(Page entry, Element element)
   {
      Navigation navigation = new Navigation(); 
      String outcomeExpression = element.attributeValue("evaluate");
      if (outcomeExpression!=null)
      {
         navigation.setOutcome( Expressions.instance().createValueExpression(outcomeExpression) );
      }
      
      List<Element> cases = element.elements("rule");
      for (Element childElement: cases)
      {
         navigation.getRules().add( parseRule(childElement) );
      }
      
      Rule rule = new Rule();
      parseEvent(element, rule);
      parseNavigationHandler(element, rule);
      parseConversationControl( element, rule.getConversationControl() );
      parseTaskControl(element, rule.getTaskControl());
      parseProcessControl(element, rule.getProcessControl());
      navigation.setRule(rule);
      
      String expression = element.attributeValue("from-action");
      if (expression==null)
      {
         if (entry.getDefaultNavigation()==null)
         {
            entry.setDefaultNavigation(navigation);
         }
         else
         {
            throw new IllegalStateException("multiple catchall <navigation> elements");
         }
      }
      else
      {
         Object old = entry.getNavigations().put(expression, navigation);
         if (old!=null)
         {
            throw new IllegalStateException("multiple <navigation> elements for action: " + expression);
         }
      }
   }
   
   /**
    * Parse param
    */
   private static Param parseParam(Element element, boolean validateModel)
   {
      String valueExpression = element.attributeValue("value");
      String name = element.attributeValue("name");
      if (name==null)
      {
         if (valueExpression==null)
         {
            throw new IllegalArgumentException("must specify name or value for page <param/> declaration");
         }
         name = valueExpression.substring(2, valueExpression.length()-1);
      }
      Param param = new Param(name, validateModel);
      if (valueExpression!=null)
      {
         param.setValueExpression(Expressions.instance().createValueExpression(valueExpression));
      }
      param.setConverterId(element.attributeValue("converterId"));
      String converterExpression = element.attributeValue("converter");
      if (converterExpression!=null)
      {
         param.setConverterValueExpression(Expressions.instance().createValueExpression(converterExpression));
      }
      param.setValidatorId(element.attributeValue("validatorId"));
      String validatorExpression = element.attributeValue("validator");
      if (validatorExpression!=null)
      {
         param.setValidatorValueExpression(Expressions.instance().createValueExpression(validatorExpression));
      }
      param.setRequired( Boolean.parseBoolean( element.attributeValue("required") ) );
      String validateModelStr = element.attributeValue("validateModel");
      if (validateModelStr != null)
      {
         param.setValidateModel(Boolean.parseBoolean(validateModelStr));
      }
      
      return param;
   }
   
   private static Header parseHeader(Element element)
   {
       Header header = new Header();

       String name = element.attributeValue("name");
       header.setName(name);

       String valueExpression = element.attributeValue("value");
       if (valueExpression==null) {
           valueExpression = element.getTextTrim();
       }
       header.setValue(Expressions.instance().createValueExpression(valueExpression));

       return header;
   }
   
   /**
    * Parse rule
    */
   private static Rule parseRule(Element element)
   {
      Rule rule = new Rule();
      
      rule.setOutcomeValue( element.attributeValue("if-outcome") );
      String expression = element.attributeValue("if");
      if (expression!=null)
      {
         rule.setCondition( Expressions.instance().createValueExpression(expression)  );
      }
      
      parseConversationControl( element, rule.getConversationControl() );
      parseTaskControl(element, rule.getTaskControl());
      parseProcessControl(element, rule.getProcessControl());
      parseEvent(element, rule);
      parseNavigationHandler(element, rule);
      
      return rule;
   }
   
   private static void parseNavigationHandler(Element element, Rule rule)
   {
      
      Element render = element.element("render");
      if (render!=null)
      {
         final String viewId = render.attributeValue("view-id");
         Element messageElement = render.element("message");
         String message = messageElement==null ? null : messageElement.getTextTrim();
         String control = messageElement==null ? null : messageElement.attributeValue("for");
         String severityName = messageElement==null ? null : messageElement.attributeValue("severity");
         Severity severity = severityName==null ? 
                  FacesMessage.SEVERITY_INFO : 
                  getFacesMessageValuesMap().get( severityName.toUpperCase() );
         rule.addNavigationHandler( new RenderNavigationHandler(stringValueExpressionFor(viewId), message, severity, control) );
      }
      
      Element redirect = element.element("redirect");
      if (redirect!=null)
      {
         List<Element> children = redirect.elements("param");
         final List<Param> params = new ArrayList<Param>();
         for (Element child: children)
         {
            params.add( parseParam(child, true) );
         }
         final String viewId = redirect.attributeValue("view-id");
         final String url    = redirect.attributeValue("url");
         final String includePageParamsAttr = redirect.attributeValue("include-page-params");
         final boolean includePageParams = includePageParamsAttr == null ? true : Boolean.getBoolean(includePageParamsAttr);

         Element messageElement = redirect.element("message");
         String control = messageElement==null ? null : messageElement.attributeValue("for");
         String message = messageElement==null ? null : messageElement.getTextTrim();
         String severityName = messageElement==null ? null : messageElement.attributeValue("severity");
         Severity severity = severityName==null ? 
                  FacesMessage.SEVERITY_INFO : 
                  getFacesMessageValuesMap().get( severityName.toUpperCase() );
         rule.addNavigationHandler(new RedirectNavigationHandler(stringValueExpressionFor(viewId), 
               stringValueExpressionFor(url), params, message, severity, control, includePageParams) );
      }
      
      List<Element> childElements = element.elements("out");
      for (Element child: childElements)
      {
         Output output = new Output();
         output.setName( child.attributeValue("name") );
         output.setValue( Expressions.instance().createValueExpression( child.attributeValue("value") ) );
         String scopeName = child.attributeValue("scope");
         if (scopeName==null)
         {
            output.setScope(ScopeType.CONVERSATION);
         }
         else
         {
            output.setScope( ScopeType.valueOf( scopeName.toUpperCase() ) );
         }
         rule.getOutputs().add(output);
      }
      
   }
   
   private static ValueExpression<String> stringValueExpressionFor(String expr) {
       return (ValueExpression<String>) ((expr == null) ? expr : Expressions.instance().createValueExpression(expr, String.class));
   }
   
   public static Map<String, Severity> getFacesMessageValuesMap()
   {
      Map<String, Severity> result = new HashMap<String, Severity>();
      for (Map.Entry<String, Severity> me: (Set<Map.Entry<String, Severity>>) FacesMessage.VALUES_MAP.entrySet())
      {
         result.put( me.getKey().toUpperCase(), me.getValue() );
      }
      return result;
   }
   
   /**
    * The global setting for no-conversation-viewid.
    * 
    * @return a JSF view id
    */
   public ValueExpression<String> getNoConversationViewId()
   {
      return noConversationViewId;
   }
   
   public void setNoConversationViewId(ValueExpression<String> noConversationViewId)
   {
      this.noConversationViewId = noConversationViewId;
   }
   
   /**
    * The global setting for login-viewid.
    * 
    * @return a JSF view id
    */
   public String getLoginViewId()
   {
      return loginViewId;
   }
   
   public void setLoginViewId(String loginViewId)
   {
      this.loginViewId = loginViewId;
   }
   
   public static String getCurrentViewId()
   {
      return getViewId( FacesContext.getCurrentInstance() );
   }
   
   public static String getCurrentBaseName()
   {
      String viewId = getViewId(FacesContext.getCurrentInstance());

      int pos = viewId.lastIndexOf("/");
      if (pos != -1)
      {
         viewId = viewId.substring(pos + 1);
      }

      pos = viewId.lastIndexOf(".");
      if (pos != -1)
      {
         viewId = viewId.substring(0, pos);
      }

      return viewId;      
   }   
   
   public static String getViewId(FacesContext facesContext)
   {
      if (facesContext!=null)
      {
         UIViewRoot viewRoot = facesContext.getViewRoot();
         if (viewRoot!=null) return viewRoot.getViewId();
      }
      return null;
   }
   
   public Integer getHttpPort()
   {
      return httpPort;
   }
   
   public void setHttpPort(Integer httpPort)
   {
      this.httpPort = httpPort;
   }
   
   public Integer getHttpsPort()
   {
      return httpsPort;
   }
   
   public void setHttpsPort(Integer httpsPort)
   {
      this.httpsPort = httpsPort;
   }
   
   public String[] getResources()
   {
      return resources;
   }
   
   public void setResources(String[] resources)
   {
      this.resources = resources;
   }
   
   private static boolean isDebugPage(String viewId)
   {
      return Init.instance().isDebugPageAvailable() && viewId.startsWith("/debug.");
   }
   
   public static boolean isDebugPage()
   {
      return Init.instance().isDebugPageAvailable() &&
            getCurrentViewId() != null &&
            getCurrentViewId().startsWith("/debug.");
   }
   
   public Collection<String> getKnownViewIds() {
       return pagesByViewId.keySet();
   }
   
}
