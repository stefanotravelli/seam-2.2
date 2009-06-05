package org.jboss.seam.test.unit;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.FacesLifecycle;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Init;
import org.jboss.seam.core.Interpolator;
import org.jboss.seam.core.Locale;
import org.jboss.seam.core.ResourceLoader;
import org.jboss.seam.mock.MockApplication;
import org.jboss.seam.mock.MockExternalContext;
import org.jboss.seam.mock.MockFacesContext;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.util.Conversions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Abstract class that provides the setup and tear-down necessary to initialize the context for performing tests
 * against the Pages component.
 */
public abstract class AbstractPageTest
{
   private static final String TEST_PAGES_DOT_XML = "/META-INF/pagesForPageActionsTest.xml";
   
   @BeforeMethod
   public void setup()
   {
      // create main application map
      Lifecycle.beginApplication(new HashMap<String, Object>());

      // start all the contexts
      Lifecycle.beginCall();

      // establish the FacesContext
      new MockFacesContext(new MockExternalContext(), new MockApplication()).setCurrent().createViewRoot();
      FacesLifecycle.resumePage();

      // install key components
      installComponents(Contexts.getApplicationContext());

      // initialize pages
      // the descriptor file locations are set using the property: org.jboss.seam.navigation.pages.resources
      // this setup of this test sets this property value to: /META-INF/pagesForPageActionsTest.xml
      Pages.instance();

      // mark the application as started
      Lifecycle.setupApplication();
   }

   @AfterMethod
   public void tearDown()
   {
      Lifecycle.endApplication();
      Lifecycle.cleanupApplication();
   }
   
   protected void installComponents(Context appContext)
   {
      Init init = new Init();
      init.setTransactionManagementEnabled(false);
      appContext.set(Seam.getComponentName(Init.class), init);
      Map<String, Conversions.PropertyValue> properties = new HashMap<String, Conversions.PropertyValue>();
      appContext.set(Component.PROPERTIES, properties);
      properties.put(Seam.getComponentName(Pages.class) + ".resources", new Conversions.FlatPropertyValue(TEST_PAGES_DOT_XML));

      installComponent(appContext, ResourceLoader.class);
      installComponent(appContext, Expressions.class);
      installComponent(appContext, Pages.class);
      installComponent(appContext, Interpolator.class);
      installComponent(appContext, Locale.class);
   }

   /**
    * Installs a component for use in the current test context.
    * 
    * @param appContext
    * @param clazz
    */
   protected void installComponent(Context appContext, Class clazz)
   {
      appContext.set(Seam.getComponentName(clazz) + ".component", new Component(clazz));
   }
}
