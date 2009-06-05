package org.jboss.seam.mock;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.el.ELResolver;
import javax.el.ValueExpression;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import org.hibernate.validator.InvalidValue;
import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.FacesLifecycle;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.contexts.TestLifecycle;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Init;
import org.jboss.seam.core.Manager;
import org.jboss.seam.core.Validators;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.jsf.SeamPhaseListener;
import org.jboss.seam.mail.MailSession;
import org.jboss.seam.pageflow.Pageflow;
import org.jboss.seam.servlet.SeamFilter;
import org.jboss.seam.servlet.ServletSessionMap;
import org.jboss.seam.transaction.Transaction;
import org.jboss.seam.util.Naming;
import org.jboss.seam.util.Reflections;

/**
 * Base class for integration tests for JSF/Seam applications. This class can be
 * extended or referenced directly for integration with various testing
 * frameworks.
 * 
 * @author Gavin King
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 */
public class AbstractSeamTest
{

   private Application application;
   private ApplicationFactory applicationFactory;
   protected ServletContext servletContext;
   private static SeamPhaseListener phases;
   protected MockHttpSession session;
   private Map<String, Map> conversationViewRootAttributes;
   protected Filter seamFilter;
   
   static 
   {
      phases = new SeamPhaseListener();
   }

   protected boolean isSessionInvalid()
   {
      return session.isInvalid();
   }

   protected HttpSession getSession()
   {
      return session;
   }

   /**
    * Helper method for resolving components in the test script.
    */
   protected Object getInstance(Class clazz)
   {
      return Component.getInstance(clazz);
   }

   /**
    * Helper method for resolving components in the test script.
    */
   protected Object getInstance(String name)
   {
      return Component.getInstance(name);
   }

   /**
    * Is there a long running conversation associated with the current request?
    */
   protected boolean isLongRunningConversation()
   {
      return Manager.instance().isLongRunningConversation();
   }

   /**
    * Search in all contexts
    */
   public Object lookup(String name)
   {
      return Contexts.lookupInStatefulContexts(name);
   }

   public abstract class ComponentTest
   {
      /**
       * Call a method binding
       */
      protected Object invokeMethod(String methodExpression)
      {
         return Expressions.instance().createMethodExpression(methodExpression).invoke();
      }

      /**
       * Evaluate (get) a value binding
       */
      protected Object getValue(String valueExpression)
      {
         return Expressions.instance().createValueExpression(valueExpression).getValue();
      }

      /**
       * Set a value binding
       */
      protected void setValue(String valueExpression, Object value)
      {
         Expressions.instance().createValueExpression(valueExpression).setValue(value);
      }

      protected abstract void testComponents() throws Exception;

      public void run() throws Exception
      {
         TestLifecycle.beginTest(servletContext, new ServletSessionMap(session));
         try
         {
            testComponents();
         }
         finally
         {
            TestLifecycle.endTest();
         }
      }
   }

   /**
    * Request is an abstract superclass for usually anonymous inner classes that
    * test JSF interactions.
    * 
    * @author Gavin King
    */
   abstract class Request
   {
      private String conversationId;
      private String outcome;
      private String action;
      private boolean validationFailed;
      private String viewId;

      private boolean renderResponseBegun;
      private boolean renderResponseComplete;
      private boolean invokeApplicationBegun;
      private boolean invokeApplicationComplete;

      private HttpServletRequest request;
      private HttpServletResponse response;
      private MockFacesContext facesContext;
      private MockExternalContext externalContext;
      private Map<String, Object> pageParameters = new HashMap<String, Object>();

      protected void setPageParameter(String name, Object value)
      {
         pageParameters.put(name, value);
      }

      protected void setParameter(String name, String value)
      {
         getParameters().put(name, new String[] { value });
      }

      protected Map<String, String[]> getParameters()
      {
         return ((MockHttpServletRequest) externalContext.getRequest()).getParameters();
      }

      protected Map<String, String[]> getHeaders()
      {
         return ((MockHttpServletRequest) externalContext.getRequest()).getHeaders();
      }

      /**
       * Override to define the name of the current principal
       * 
       * @return "gavin" by default
       */
      public String getPrincipalName()
      {
         return "gavin";
      }

      /**
       * Override to define the roles assigned to the current principal
       * 
       * @return a Set of all roles by default
       */
      public Set<String> getPrincipalRoles()
      {
         return new AbstractSet<String>()
         {
            @Override
            public boolean contains(Object o)
            {
               return true;
            }

            @Override
            public Iterator<String> iterator()
            {
               throw new UnsupportedOperationException();
            }

            @Override
            public int size()
            {
               throw new UnsupportedOperationException();
            }
         };
      }

      public List<Cookie> getCookies()
      {
         return Collections.EMPTY_LIST;
      }

      /**
       * A script for a JSF interaction with no existing long-running
       * conversation.
       */
      protected Request()
      {
      }

      /**
       * A script for a JSF interaction in the scope of an existing long-running
       * conversation.
       */
      protected Request(String conversationId)
      {
         this.conversationId = conversationId;
      }

      /**
       * Is this a non-faces request? Override if it is.
       * 
       * @return false by default
       */
      protected boolean isGetRequest()
      {
         return false;
      }

      /**
       * The JSF view id of the form that is being submitted or of the page that
       * is being rendered in a non-faces request. (override if you need page
       * actions to be called, and page parameters applied)
       */
      protected String getViewId()
      {
         return viewId;
      }

      protected void setViewId(String viewId)
      {
         this.viewId = viewId;
      }

      /**
       * Override to implement the interactions between the JSF page and your
       * components that occurs during the apply request values phase.
       */
      protected void applyRequestValues() throws Exception
      {
      }

      /**
       * Override to implement the interactions between the JSF page and your
       * components that occurs during the process validations phase.
       */
      protected void processValidations() throws Exception
      {
      }

      /**
       * Override to implement the interactions between the JSF page and your
       * components that occurs during the update model values phase.
       */
      protected void updateModelValues() throws Exception
      {
      }

      /**
       * Override to implement the interactions between the JSF page and your
       * components that occurs during the invoke application phase.
       */
      protected void invokeApplication() throws Exception
      {
      }

      /**
       * Set the outcome of the INVOKE_APPLICATION phase
       */
      protected void setOutcome(String outcome)
      {
         this.outcome = outcome;
      }

      /**
       * The outcome of the INVOKE_APPLICATION phase
       */
      protected String getOutcome()
      {
         return outcome;
      }

      /**
       * Get the outcome of the INVOKE_APPLICATION phase
       */
      protected String getInvokeApplicationOutcome()
      {
         return outcome;
      }

      /**
       * Override to implement the interactions between the JSF page and your
       * components that occurs during the render response phase.
       */
      protected void renderResponse() throws Exception
      {
      }

      /**
       * Make some assertions, after the end of the request.
       */
      protected void afterRequest()
      {
      }

      /**
       * Do anything you like, after the start of the request. Especially, set
       * up any request parameters for the request.
       */
      protected void beforeRequest()
      {
      }

      /**
       * Get the view id to be rendered
       * 
       * @return the JSF view id
       */
      protected String getRenderedViewId()
      {
         if (Init.instance().isJbpmInstalled() && Pageflow.instance().isInProcess())
         {
            return Pageflow.instance().getPageViewId();
         }
         else
         {
            // TODO: not working right now, 'cos no mock navigation handler!
            return getFacesContext().getViewRoot().getViewId();
         }
      }

      /**
       * Did a validation failure occur during a call to validate()?
       */
      protected boolean isValidationFailure()
      {
         return validationFailed;
      }

      protected FacesContext getFacesContext()
      {
         return facesContext;
      }

      protected String getConversationId()
      {
         return conversationId;
      }

      /**
       * Evaluate (get) a value binding
       */
      protected Object getValue(String valueExpression)
      {
         return application.evaluateExpressionGet(facesContext, valueExpression, Object.class);
      }

      /**
       * Set a value binding
       */
      protected void setValue(String valueExpression, Object value)
      {
         application.getExpressionFactory().createValueExpression(facesContext.getELContext(), valueExpression, Object.class).setValue(facesContext.getELContext(), value);
      }

      /**
       * Validate the value against model-based constraints return true if the
       * value is valid
       */
      protected boolean validateValue(String valueExpression, Object value)
      {
         ValueExpression ve = application.getExpressionFactory().createValueExpression(facesContext.getELContext(), valueExpression, Object.class);
         InvalidValue[] ivs = Validators.instance().validate(ve, facesContext.getELContext(), value);
         if (ivs.length > 0)
         {
            validationFailed = true;
            facesContext.addMessage(null, FacesMessages.createFacesMessage(FacesMessage.SEVERITY_ERROR, ivs[0].getMessage()));
            return false;
         }
         else
         {
            return true;
         }
      }

      protected void onException(Exception e)
      {
         throw new AssertionError(e);
      }

      /**
       * Call a method binding
       */
      protected Object invokeMethod(String methodExpression)
      {
         return application.getExpressionFactory().createMethodExpression(facesContext.getELContext(), methodExpression, Object.class, new Class[0]).invoke(facesContext.getELContext(), null);
      }

      /**
       * Simulate an action method
       */
      protected Object invokeAction(String actionMethodExpression)
      {
         action = actionMethodExpression;
         Object result = invokeMethod(actionMethodExpression);
         if (result != null)
         {
            setOutcome(result.toString());
         }
         return result;
      }

      /**
       * @return the conversation id
       * @throws Exception to fail the test
       */
      public String run() throws Exception
      {
         try
         {
            init();
            beforeRequest();
            setStandardJspVariables();
            seamFilter.doFilter(request, response, new FilterChain()
            {
               public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException
               {
                  try
                  {
                     if (emulateJsfLifecycle())
                     {
                        saveConversationViewRoot();
                     }
                  }
                  catch (Exception e)
                  {
                     onException(e);
                     throw new ServletException(e);
                  }
               }
            });
            seamFilter.destroy();
            facesContext.release();
            afterRequest();
            return conversationId;
         }
         finally
         {
            if (Contexts.isEventContextActive())
            {
               FacesLifecycle.endRequest(externalContext);
            }
         }

      }

      private void saveConversationViewRoot()
      {
         Map renderedViewRootAttributes = facesContext.getViewRoot().getAttributes();
         if (renderedViewRootAttributes != null && conversationId != null)
         {
            Map conversationState = new HashMap();
            conversationState.putAll(renderedViewRootAttributes);
            conversationViewRootAttributes.put(conversationId, conversationState);
         }
      }

      protected void init()
      {
         request = createRequest();
         response = createResponse();
         externalContext = new MockExternalContext(servletContext, request, response);
         facesContext = new MockFacesContext(externalContext, application);
         facesContext.setCurrent();
      }

      /**
       * Override if you wish to customize the HttpServletRequest used in this request.
       * You may find {@link HttpServletRequestWrapper} useful.
       */
      protected HttpServletRequest createRequest()
      {
         Cookie[] cookieArray = getCookies().toArray(new Cookie[] {});
         return new MockHttpServletRequest(session, getPrincipalName(), getPrincipalRoles(), cookieArray, isGetRequest() ? "GET" : "POST");
      }
      
      /**
       * Override if you wish to customize the HttpServletResponse used in this request.
       * You may find {@link HttpServletResponseWrapper} useful. 
       */
      protected HttpServletResponse createResponse()
      {
         return new MockHttpServletResponse();
      }

      private void setStandardJspVariables()
      {
         // TODO: looks like we should also set request, session, application,
         // page...
         Map<String, String> params = new HashMap<String, String>();
         for (Map.Entry<String, String[]> e : ((Map<String, String[]>) request.getParameterMap()).entrySet())
         {
            if (e.getValue().length == 1)
            {
               params.put(e.getKey(), e.getValue()[0]);
            }
         }
         request.setAttribute("param", params);
      }

      /**
       * @return true if a response was rendered
       */
      private boolean emulateJsfLifecycle() throws Exception
      {
         restoreViewPhase();
         if (!isGetRequest() && !skipToRender())
         {
            applyRequestValuesPhase();
            if (!skipToRender())
            {
               processValidationsPhase();
               if (!skipToRender())
               {
                  updateModelValuesPhase();
                  if (!skipToRender())
                  {
                     invokeApplicationPhase();
                  }
               }
            }
         }

         if (skipRender())
         {
            // we really should look at redirect parameters here!
            return false;
         }
         else
         {
            renderResponsePhase();
            return true;
         }
      }

      private void renderResponsePhase() throws Exception
      {
         phases.beforePhase(new PhaseEvent(facesContext, PhaseId.RENDER_RESPONSE, MockLifecycle.INSTANCE));

         try
         {
            updateConversationId();

            renderResponseBegun = true;

            renderResponse();

            renderResponseComplete = true;

            facesContext.getApplication().getStateManager().saveView(facesContext);

            updateConversationId();
         }
         finally
         {
            phases.afterPhase(new PhaseEvent(facesContext, PhaseId.RENDER_RESPONSE, MockLifecycle.INSTANCE));
         }
      }

      private void invokeApplicationPhase() throws Exception
      {
         phases.beforePhase(new PhaseEvent(facesContext, PhaseId.INVOKE_APPLICATION, MockLifecycle.INSTANCE));
         try
         {
            updateConversationId();

            invokeApplicationBegun = true;

            invokeApplication();

            invokeApplicationComplete = true;

            String outcome = getInvokeApplicationOutcome();
            facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, action, outcome);

            viewId = getRenderedViewId();

            updateConversationId();
         }
         finally
         {
            phases.afterPhase(new PhaseEvent(facesContext, PhaseId.INVOKE_APPLICATION, MockLifecycle.INSTANCE));
         }
      }

      private void updateModelValuesPhase() throws Exception
      {
         phases.beforePhase(new PhaseEvent(facesContext, PhaseId.UPDATE_MODEL_VALUES, MockLifecycle.INSTANCE));
         try
         {
            updateConversationId();

            updateModelValues();

            updateConversationId();
         }
         finally
         {
            phases.afterPhase(new PhaseEvent(facesContext, PhaseId.UPDATE_MODEL_VALUES, MockLifecycle.INSTANCE));
         }
      }

      private void processValidationsPhase() throws Exception
      {
         phases.beforePhase(new PhaseEvent(facesContext, PhaseId.PROCESS_VALIDATIONS, MockLifecycle.INSTANCE));
         try
         {
            updateConversationId();

            processValidations();

            updateConversationId();

            if (isValidationFailure())
            {
               facesContext.renderResponse();
            }
         }
         finally
         {
            phases.afterPhase(new PhaseEvent(facesContext, PhaseId.PROCESS_VALIDATIONS, MockLifecycle.INSTANCE));
         }
      }

      private void applyRequestValuesPhase() throws Exception
      {
         phases.beforePhase(new PhaseEvent(facesContext, PhaseId.APPLY_REQUEST_VALUES, MockLifecycle.INSTANCE));
         try
         {
            updateConversationId();

            applyRequestValues();

            updateConversationId();
         }
         finally
         {
            phases.afterPhase(new PhaseEvent(facesContext, PhaseId.APPLY_REQUEST_VALUES, MockLifecycle.INSTANCE));
         }
      }

      private void restoreViewPhase()
      {
         phases.beforePhase(new PhaseEvent(facesContext, PhaseId.RESTORE_VIEW, MockLifecycle.INSTANCE));
         try
         {
            UIViewRoot viewRoot = facesContext.getApplication().getViewHandler().createView(facesContext, getViewId());
            facesContext.setViewRoot(viewRoot);
            Map restoredViewRootAttributes = facesContext.getViewRoot().getAttributes();
            if (conversationId != null)
            {
               if (isGetRequest())
               {
                  setParameter(Manager.instance().getConversationIdParameter(), conversationId);
                  // TODO: what about conversationIsLongRunning????
               }
               else
               {
                  if (conversationViewRootAttributes.containsKey(conversationId))
                  {
                     // should really only do this if the view id matches (not
                     // really possible to implement)
                     Map state = conversationViewRootAttributes.get(conversationId);
                     restoredViewRootAttributes.putAll(state);
                  }
               }
            }
            if (isGetRequest())
            {
               facesContext.renderResponse();
            }
            else
            {
               restoredViewRootAttributes.putAll(pageParameters);
            }
         }
         finally
         {
            phases.afterPhase(new PhaseEvent(facesContext, PhaseId.RESTORE_VIEW, MockLifecycle.INSTANCE));
         }
      }

      private void updateConversationId()
      {
         Manager manager = Manager.instance();
         conversationId = manager.isLongRunningConversation() ? manager.getCurrentConversationId() : manager.getParentConversationId();
      }

      private boolean skipRender()
      {
         return FacesContext.getCurrentInstance().getResponseComplete();
      }

      private boolean skipToRender()
      {
         return FacesContext.getCurrentInstance().getRenderResponse() || FacesContext.getCurrentInstance().getResponseComplete();
      }

      protected boolean isInvokeApplicationBegun()
      {
         return invokeApplicationBegun;
      }

      protected boolean isInvokeApplicationComplete()
      {
         return invokeApplicationComplete;
      }

      protected boolean isRenderResponseBegun()
      {
         return renderResponseBegun;
      }

      protected boolean isRenderResponseComplete()
      {
         return renderResponseComplete;
      }

      protected MimeMessage getRenderedMailMessage(String viewId)
      {
         installMockTransport();
         MockTransport.clearMailMessage();
         Renderer.instance().render(viewId);
         return MockTransport.getMailMessage();
      }

   }

   public class NonFacesRequest extends Request
   {
      public NonFacesRequest()
      {
      }

      /**
       * @param viewId the view id to be rendered
       */
      public NonFacesRequest(String viewId)
      {
         setViewId(viewId);
      }

      /**
       * @param viewId the view id to be rendered
       * @param conversationId the conversation id
       */
      public NonFacesRequest(String viewId, String conversationId)
      {
         super(conversationId);
         setViewId(viewId);
      }

      @Override
      protected final boolean isGetRequest()
      {
         return true;
      }

      @Override
      protected final void applyRequestValues() throws Exception
      {
         throw new UnsupportedOperationException();
      }

      @Override
      protected final void processValidations() throws Exception
      {
         throw new UnsupportedOperationException();
      }

      @Override
      protected final void updateModelValues() throws Exception
      {
         throw new UnsupportedOperationException();
      }

      @Override
      protected final void invokeApplication() throws Exception
      {
         throw new UnsupportedOperationException();
      }

   }

   public class FacesRequest extends Request
   {

      public FacesRequest()
      {
      }

      /**
       * @param viewId the view id of the form that was submitted
       */
      public FacesRequest(String viewId)
      {
         setViewId(viewId);
      }

      /**
       * @param viewId the view id of the form that was submitted
       * @param conversationId the conversation id
       */
      public FacesRequest(String viewId, String conversationId)
      {
         super(conversationId);
         setViewId(viewId);
      }

      @Override
      protected final boolean isGetRequest()
      {
         return false;
      }

   }

   public void begin()
   {
      session = new MockHttpSession(servletContext);
      ServletLifecycle.beginSession(session);
   }

   public void end()
   {
      ServletLifecycle.endSession(session);
      session = null;
   }
   
   /**
    * Boot Seam. Can be used at class, test group or suite level (e.g.
    * @BeforeClass, @BeforeTest, @BeforeSuite)
    * Use in conjunction with {@link #stopSeam()}.
    * @throws Exception
    */
   protected void startSeam() throws Exception
   {
      startJbossEmbeddedIfNecessary();
      this.servletContext = createServletContext();
      ServletLifecycle.beginApplication(servletContext);
      FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, MockApplicationFactory.class.getName());
      new Initialization(servletContext).create().init();
      ((Init) servletContext.getAttribute(Seam.getComponentName(Init.class))).setDebug(false);
   }
   
   protected ServletContext createServletContext()
   {
      MockServletContext mockServletContext = new MockServletContext();
      initServletContext(mockServletContext.getInitParameters());
      return mockServletContext;
   }
   
   /**
    * Shutdown Seam. Can be used at class, test group or suite level (e.g
    * @AfterClass, @AfterTest, @AfterSuite)
    * Use in conjunction with {@link #startSeam()}.
    * @throws Exception
    */
   protected void stopSeam() throws Exception
   {
      ServletLifecycle.endApplication();
   }
   
   /**
    * Setup this test class instance
    * Must be run for each test class instance (e.g. @BeforeClass)
    * @throws Exception
    */
   protected void setupClass() throws Exception
   {
      servletContext = ServletLifecycle.getServletContext();
      applicationFactory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
      application = applicationFactory.getApplication();
      conversationViewRootAttributes = new HashMap<String, Map>();
      seamFilter = createSeamFilter();
      FactoryFinder.setFactory(FactoryFinder.FACES_CONTEXT_FACTORY, MockFacesContextFactory.class.getName());
      
      for (ELResolver elResolver : getELResolvers())
      {
         application.addELResolver(elResolver);
      }
   }
   
   /**
    * Cleanup this test class instance
    * Must be run for each test class instance (e.g. @AfterClass)
    */
   protected void cleanupClass() throws Exception
   {
      seamFilter.destroy();
      conversationViewRootAttributes = null;
      applicationFactory.setApplication(null);
   }

   protected Filter createSeamFilter() throws ServletException
   {
      SeamFilter seamFilter = new SeamFilter();
      seamFilter.init(new MockFilterConfig(servletContext));
      return seamFilter;
   }

   /**
    * Override to set up any servlet context attributes.
    */
   public void initServletContext(Map initParams)
   {
   }

   protected InitialContext getInitialContext() throws NamingException
   {
      return Naming.getInitialContext();
   }

   protected UserTransaction getUserTransaction() throws NamingException
   {
      return Transaction.instance();
   }

   /**
    * Get the value of an object field, by reflection.
    */
   protected Object getField(Object object, String fieldName)
   {
      Field field = Reflections.getField(object.getClass(), fieldName);
      if (!field.isAccessible())
         field.setAccessible(true);
      return Reflections.getAndWrap(field, object);
   }

   /**
    * Set the value of an object field, by reflection.
    */
   protected void setField(Object object, String fieldName, Object value)
   {
      Field field = Reflections.getField(object.getClass(), fieldName);
      if (!field.isAccessible())
         field.setAccessible(true);
      Reflections.setAndWrap(field, object, value);
   }

   private static boolean started;

   protected void startJbossEmbeddedIfNecessary() throws Exception
   {
      if (!started && embeddedJBossAvailable())
      {
         new EmbeddedBootstrap().startAndDeployResources();
      }

      started = true;
   }

   private boolean embeddedJBossAvailable()
   {
      try
      {
         Class.forName("org.jboss.embedded.Bootstrap");
         return true;
      }
      catch (ClassNotFoundException e)
      {
         return false;
      }
   }

   protected ELResolver[] getELResolvers()
   {
      return new ELResolver[0];
   }

   protected void installMockTransport()
   {
      Contexts.getApplicationContext().set(Seam.getComponentName(MailSession.class), new MailSession("mock").create());

   }

}
