package org.jboss.seam.flex;

import java.io.*;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;

import javax.servlet.*;
import javax.servlet.http.*;

import flex.messaging.*;
import flex.messaging.config.*;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.log.ServletLogTarget;
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.*;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Reflections;
import org.jboss.seam.util.Resources;


public class MessageBrokerManager
{
   private static final String SEAM_ENDPOINT = "seam-amf";

   private static final LogProvider log = Logging.getLogProvider(MessageBrokerManager.class);

   private static String WAR_CONFIG_PREFIX = "/WEB-INF/flex/";
   private static String EAR_CONFIG_PREFIX = "/META-INF/flex/seam-default-";

   private flex.messaging.MessageBroker broker;
   
   private ServletConfig servletConfig;
   
   
   public void init(ServletConfig servletConfig)
      throws ServletException
   {
      this.servletConfig = servletConfig;
      
      // allocate thread local variables
      createThreadLocals();
      
      try
      {
         FlexContext.setThreadLocalObjects(null, null, null, null, null, servletConfig);         
         ServletLogTarget.setServletContext(servletConfig.getServletContext());
                 
         FlexConfigurationManager configManager = new SeamFlexConfigurationManager();
         MessagingConfiguration config = configManager.getMessagingConfiguration(servletConfig);
         
         config.createLogAndTargets();         
         broker = config.createBroker(servletConfig.getInitParameter("messageBrokerId"), 
                                      Thread.currentThread().getContextClassLoader());
         
         // Set the servlet config as thread local
         FlexContext.setThreadLocalObjects(null, null, broker, null, null, servletConfig);
         
         setInitServletContext(broker, servletConfig.getServletContext());
         
         // Create endpoints, services, security, and logger on the broker based on configuration
         config.configureBroker(broker);                 
         
         
         if (broker.getChannelIds()== null || !broker.getChannelIds().contains(SEAM_ENDPOINT)) {
            log.info("seam-amf endpoint not found. creating...");

            broker.createEndpoint(SEAM_ENDPOINT, 
                  "http://{server.name}:{server.port}/{context.root}/messagebroker/seam-amf", 
                  "flex.messaging.endpoints.AMFEndpoint");
         }
         
         
         //initialize the httpSessionToFlexSessionMap
         synchronized(HttpFlexSession.mapLock)
         {
            if (servletConfig.getServletContext().getAttribute(HttpFlexSession.SESSION_MAP) == null) {
               servletConfig.getServletContext().setAttribute(HttpFlexSession.SESSION_MAP, new ConcurrentHashMap());
            }
         }
         
         broker.start();
         
         configManager.reportTokens();
         config.reportUnusedProperties();
         
         
         
         // clear the broker and servlet config as this thread is done
         FlexContext.clearThreadLocalObjects();
      
      } catch (Throwable t){
         log.error("MessageBrokerServlet failed to initialize due to runtime exception");
         destroy();
         throw new ServletException(t);
      }
   }
   
   
   private void setInitServletContext(flex.messaging.MessageBroker broker, ServletContext ctx)    
      throws Exception
   {
      Method setMethod = flex.messaging.MessageBroker.class.
      getDeclaredMethod("setInitServletContext", ServletContext.class);
      setMethod.setAccessible(true);
      Reflections.invoke(setMethod, broker, ctx);
   }
   
   
   
   public void destroy()
   {
      if (broker != null) {
         broker.stop();
         // release static thread locals
         destroyThreadLocals();
      }
   }
   
   public void service(HttpServletRequest req, HttpServletResponse res)
   {
      log.info("=========== START FLEX REQUEST");
      try {
         broker.initThreadLocals();
         
         FlexContext.setThreadLocalObjects(null, null, broker, req, res, servletConfig);
         
         // necessary to create for later
         HttpFlexSession fs = HttpFlexSession.getFlexSession(req);
         log.info("flex session is " + fs);
         
         Endpoint endpoint = findEndpoint(req, res);            
         log.info("Endpoint: " + endpoint.describeEndpoint());
         
         endpoint.service(req, res);         
      } catch (UnsupportedOperationException ue) {     
         ue.printStackTrace();
         sendError(res);
      } catch (RuntimeException e) {
         e.printStackTrace();
      } finally {
         FlexContext.clearThreadLocalObjects();         
      }
      
      log.info("=========== END FLEX REQUEST");
   }
   
   
   private void sendError(HttpServletResponse res)
   {
      if (!res.isCommitted()) {
         try {                        
            res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
         } catch (IOException ignored) {
            
         }
      }
   }
   
   
   private Endpoint findEndpoint(HttpServletRequest req, HttpServletResponse res)
   {
      String contextPath = req.getContextPath();
      String pathInfo = req.getPathInfo();
      String endpointPath = req.getServletPath();
      if (pathInfo != null) {
         endpointPath = endpointPath + pathInfo;
      }
      
      log.info("flex request for cp=" + contextPath + " ep=" + endpointPath);
      try {
         return broker.getEndpoint(endpointPath, contextPath);
      } catch (MessageException me) {
         if (!res.isCommitted()) {
            try {                    
               res.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException ignore) {
               // ignore
            }
         }                
         return null;
      }
   }
   
   
   // Call ONLY on servlet startup
   public void createThreadLocals()
   {
      // allocate static thread local objects
      flex.messaging.MessageBroker.createThreadLocalObjects();
      FlexContext.createThreadLocalObjects();
      flex.messaging.io.SerializationContext.createThreadLocalObjects();
      flex.messaging.io.TypeMarshallingContext.createThreadLocalObjects();
   }
   
   protected void destroyThreadLocals()
   {
      // Destroy static thread local objects
      flex.messaging.MessageBroker.releaseThreadLocalObjects();
      FlexContext.releaseThreadLocalObjects();
      flex.messaging.io.SerializationContext.releaseThreadLocalObjects();
      flex.messaging.io.TypeMarshallingContext.releaseThreadLocalObjects();
   }
   
   
   private RemotingService createRemotingService() {
            RemotingService remotingService = null;
           
      remotingService = new RemotingService();
      remotingService.setId("remoting-service");

      broker.addService(remotingService);
      log.info("Flex remotingservice not found- creating " + remotingService);
      return remotingService;
   }
   
   
   private RemotingService findRemotingService() {
      return (RemotingService) broker.getServiceByType(RemotingService.class.getName());
   }
   
   private void registerSeamAdapter(RemotingService remotingService) {
      if (remotingService.getRegisteredAdapters().get(SeamAdapter.SEAM_ADAPTER_ID) == null) {
         remotingService.registerAdapter(SeamAdapter.SEAM_ADAPTER_ID,SeamAdapter.class.getName());
      }
   }
   
   private Destination createDestination(String destinationName, String componentName) {           
      RemotingService remotingService = findRemotingService();
      if (remotingService==null) {
         remotingService = createRemotingService();
      }
      
      RemotingDestination destination = 
         (RemotingDestination) remotingService.createDestination(destinationName);
      
      destination.setFactory(new FlexSeamFactory(destinationName, componentName));
            
      // configure adapter       
      registerSeamAdapter(remotingService);
      destination.createAdapter(SeamAdapter.SEAM_ADAPTER_ID);

      destination.addChannel(SEAM_ENDPOINT); 
      
      return destination;
   }
   
   public void addDestinations(Collection<Class<?>> destinations) {                            
      for (Class<?> annotatedClass: destinations) {
         log.info("Adding scanned flex desitionation for class " + annotatedClass);
         FlexRemote fr = annotatedClass.getAnnotation(FlexRemote.class);
         
         Name name = annotatedClass.getAnnotation(Name.class);
         
         String destinationName = fr.name();
         String componentName = name.value();
         Destination destination = createDestination(destinationName, componentName);
         
         destination.start();   
      }
   }
   
}
