package org.jboss.seam.wicket.mock;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.protocol.http.HttpSessionStore;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.tester.BaseWicketTester;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;
import org.jboss.seam.Seam;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.core.Init;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.mock.EmbeddedBootstrap;
import org.jboss.seam.wicket.SeamWebApplication;


/**
 * A subclass of WicketTester that enforces the use of a SeamWebApplication.  This ensures that
 * ensures that seam contexts are are set up and torn down correctly in testing.   Each SeamWebApplication
 * gets a complete application context, so seam application teardown/restart happens if you create
 * a new SeamWicketTester without propogating any existing SeamWebApplication through the constructor.
 */
public class SeamWicketTester extends WicketTester
{

   public SeamWicketTester()
   {
      this(DummyHomePage.class);
   }
   
   public SeamWicketTester(Class homePage) 
   {
      this(homePage,null);
   }
   
   public SeamWicketTester(Class homePage, Class loginPage) 
   {
      this(createApplication(homePage, loginPage));
   }   
   public SeamWicketTester(final SeamWebApplication application)
   {
      this(application,null);
   }
   
   public SeamWicketTester(final SeamWebApplication application, final String path)
   {
      super(application,path);
      
      /*
       * When in tests, we want the contexts destroyed lazily, i.e. 
       * don't destroy the contexts after a request, but rather before the 
       * next request, so that we can inspect the values of wicket components and
       * their models 
       */
      Lifecycle.beginCall();
      ((SeamWebApplication)getApplication()).setDestroyContextsLazily(true);
      Lifecycle.endCall();
   }

   /**
    * Create a SeamWebApplication suitable for testing, a la WicketTester's DummyApplication
    * @param homePage The WebPage class to start on
    * @param loginPage The WebPage class to use for any Seam Authentication redirects
    */
   private static SeamWebApplication createApplication(final Class homePage, final Class loginPage)
   {
      return new SeamWebApplication()
      {
         @Override
         public Class getHomePage()
         {
            return homePage;
         }
         
         @Override
         protected Class getLoginPage()
         {
            return loginPage;
         }

         @Override
         protected ISessionStore newSessionStore()
         {
            // Don't use a filestore, or we spawn lots of threads, which
            // makes things slow.
            return new HttpSessionStore(this);
         }

         @Override
         protected WebResponse newWebResponse(final HttpServletResponse servletResponse)
         {
            // Don't use a buffered response in testing 
            return new WebResponse(servletResponse);
         }

         @Override
         protected void outputDevelopmentModeWarning()
         {
            // Do nothing.
         }
      };
   }
   
   /**
    * For each new servletContext, i.e. each new SeamWebApplication instance, we create a new seam
    * initialization.
    */
   @Override
   public ServletContext newServletContext(String path) 
   { 
      if (ServletLifecycle.getServletContext() != null)
      {
         ServletLifecycle.endApplication();
      }
      if (path == null) 
      {
         URL webxml = getClass().getResource("/WEB-INF/web.xml");
         if (webxml != null)
         {
            try
            {
               path = new File(webxml.toURI()).getParentFile().getParentFile().getAbsolutePath();
            }
            catch (URISyntaxException e)
            {
               throw new RuntimeException(e);
            }
         }
      }
      ServletContext context = super.newServletContext(path);
      startJbossEmbeddedIfNecessary();
      ServletLifecycle.beginApplication(context);
      new Initialization(context).create().init();
      ((Init) context.getAttribute(Seam.getComponentName(Init.class))).setDebug(false);
      return context;
   }

   @Override
   public WebRequestCycle setupRequestAndResponse(boolean isAjax) { 
      WebRequestCycle cycle = super.setupRequestAndResponse(isAjax);
      /**
       * FormTester wants to walk the form tree and call getValue() on components
       * without having started the request cycle.  This fails when wicket components have
       * seam injections.  So force a call here 
       */
      BaseWicketTester.callOnBeginRequest(cycle);
      return cycle;
   }
   
   private static boolean started;

   protected void startJbossEmbeddedIfNecessary() 
   {
      try 
      {
         if (!started && embeddedJBossAvailable())
         {
            new EmbeddedBootstrap().startAndDeployResources();
         }
         started = true;
      }
      catch (Exception exception)
      {
         throw new RuntimeException("Failure starting up Embedded Jboss",exception);
      }
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

}
