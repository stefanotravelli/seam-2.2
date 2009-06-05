package org.jboss.seam.test.unit;

import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import org.jboss.seam.contexts.ApplicationContext;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.core.Init;
import org.jboss.seam.core.Manager;
import org.jboss.seam.mock.MockExternalContext;
import org.jboss.seam.mock.MockServletContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * A simple base test class that registers components according to class so that
 * they can be looked up successfully using ComponentClass.instance(), as
 * required by much of Seam's internal API.
 */
public abstract class MockContainerTest
{
   @BeforeMethod
   protected void setUp()
   {
      MockServletContext servletContext = new MockServletContext();
      ServletLifecycle.beginApplication(servletContext);
      MockExternalContext externalContext = new MockExternalContext(servletContext);
      Context appContext = new ApplicationContext(externalContext.getApplicationMap());
      installComponent(appContext, Manager.class);
      for (Class c : getComponentsToInstall())
      {
         installComponent(appContext, c);
      }
      appContext.set(Seam.getComponentName(Init.class), new Init());
      Lifecycle.beginCall();
   }
   
   protected Class[] getComponentsToInstall()
   {
      return new Class[] {};
   }
   
   @AfterMethod
   protected void tearDown()
   {
      Lifecycle.endCall();
   }
   
   protected void installComponent(Context appContext, Class clazz)
   {
      appContext.set(Seam.getComponentName(clazz) + ".component", new Component(clazz));
   }
}
