package org.jboss.seam.flex;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.*;

import org.jboss.seam.*;

import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.intercept.*;
import org.jboss.seam.deployment.AnnotationDeploymentHandler;
import org.jboss.seam.deployment.DeploymentStrategy;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.web.AbstractFilter;

@Scope(ScopeType.APPLICATION)
@Name("org.jboss.seam.flex.flexFilter")
@Startup
@Install(precedence=Install.BUILT_IN, value=false)
@BypassInterceptors
@org.jboss.seam.annotations.web.Filter //within={"org.jboss.seam.????"}
public class FlexFilter 
    extends AbstractFilter
{
   private static final LogProvider log = Logging.getLogProvider(FlexFilter.class);

   MessageBrokerManager messageBrokerManager;
   List<Class<?>> scanned = new ArrayList<Class<?>>();

   private AnnotationDeploymentHandler annotationDeploymentHandler() {
      DeploymentStrategy deployment = (DeploymentStrategy) Component.getInstance("deploymentStrategy");                   
      
      if (deployment != null) {
          return (AnnotationDeploymentHandler) 
             deployment.getDeploymentHandlers().get(AnnotationDeploymentHandler.NAME);
      }
      
      return null;
   }
   
   private Collection<Class<?>> scannedClasses() {
      Collection<Class<?>> result = null;
      
      AnnotationDeploymentHandler handler = annotationDeploymentHandler();
      if (handler !=null) {
         result = handler.getClassMap().get(FlexRemote.class.getName());
      }
      
      return result != null ? result : new ArrayList<Class<?>>(0);
   }
   
   @Create
   public void seamInit() {
      // deployment handler only knows about scanned classes during startup
      // so we need to get them now and save them
      scanned.addAll(scannedClasses());
   }
   
   @Override
   public void init(FilterConfig filterConfig) 
      throws ServletException
   {
      super.init(filterConfig);
      
      messageBrokerManager = new MessageBrokerManager();
      messageBrokerManager.init(new FlexServletConfig(filterConfig.getServletContext()));      
      messageBrokerManager.addDestinations(scanned);
   }
   

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
       throws IOException, ServletException
   {
      if (isMappedToCurrentRequestPath(request)) {         
         messageBrokerManager.service((HttpServletRequest)request, (HttpServletResponse)response);
      } else { 
         chain.doFilter(request, response);
      }
   }
   
   
   private static class FlexServletConfig 
       implements ServletConfig 
   {
      Map<String,String> params;
      ServletContext context;
      
      public FlexServletConfig(ServletContext context) {
         this(context, null);
      }
      
      public FlexServletConfig(ServletContext context, Map<String,String> params) {
         this.context = context;
         this.params = (params!=null) ? params : new HashMap<String,String>();
      }
      
      public ServletContext getServletContext() {
         return context;
      }
      
      public String getServletName() {
         return "FlexServlet";
      }
           
      public String getInitParameter(String param) {         
         String result = params.get(param);
         
         log.info("init param " + param + " is " + result);
         return result;
      }
      
      @SuppressWarnings("unchecked")        
      public Enumeration getInitParameterNames() {
         return Collections.enumeration(params.keySet());
      }
      
   }
}