package org.jboss.seam.mock;

import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;

import org.jboss.seam.jsf.SeamApplication;

/**
 * An mock implementation of the JSF ApplicationFactory which returns a mock
 * Application wrapped in a SeamApplication. This class can be registered with
 * JSF to allow JSF to be used formally in a test environment as follows:
 * 
 * <code>
 * FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY,
 *    "org.jboss.seam.mock.MockApplicationFactory");
 * Application application = ((ApplicationFactory) FactoryFinder
 *    .getFactory(FactoryFinder.APPLICATION_FACTORY)).getApplication();    
 * </code>
 * 
 * @author Dan Allen
 */
public class MockApplicationFactory extends ApplicationFactory
{
   private Application application;
   
   @Override
   public Application getApplication()
   {
      if (application == null) {
         application = new SeamApplication(new MockApplication());
      }
      return application;
   }

   @Override
   public void setApplication(Application application)
   {
      this.application = application;
   }

}
