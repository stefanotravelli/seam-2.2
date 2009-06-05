//$Id$
package org.jboss.seam.test.unit;

import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.core.Manager;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.mock.MockServletContext;
import org.jboss.seam.test.unit.component.ConfigurableComponent;
import org.jboss.seam.test.unit.component.MyEntityHome;
import org.jboss.seam.test.unit.component.PrimaryColor;
import org.jboss.seam.transaction.NoTransaction;
import org.jboss.seam.transaction.Transaction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class InitializationTest
{
   @Test
   public void testInitialization()
   {
      MockServletContext servletContext = new MockServletContext();
      ServletLifecycle.beginApplication(servletContext);
      new Initialization(servletContext).create().init();

      assert !servletContext.getAttributes().isEmpty();
      assert servletContext.getAttributes().containsKey( Seam.getComponentName(Manager.class) + ".component" );
      assert servletContext.getAttributes().containsKey( Seam.getComponentName(Foo.class) + ".component" );
      assert !Contexts.isApplicationContextActive();
      ServletLifecycle.endApplication();
   }

   /**
    * Configuration for ConfigurableComponent is defined in ConfigurableComponent.component.xml
    */
   @Test
   public void testEnumPropertyAssignment()
   {
       MockServletContext servletContext = new MockServletContext();
       ServletLifecycle.beginApplication(servletContext);
       new Initialization( servletContext ).create().init();

       Lifecycle.beginCall();

       ConfigurableComponent component = (ConfigurableComponent) Component.getInstance(ConfigurableComponent.class);
       assert component != null;
       assert component.getPrimaryColor().equals(PrimaryColor.RED);

       ServletLifecycle.endApplication();
   }
   
   @Test
   public void testEntityHomeConfiguration()
   {
      MockServletContext servletContext = new MockServletContext();
      ServletLifecycle.beginApplication(servletContext);
      new Initialization( servletContext ).create().init();
      Lifecycle.beginCall();
      Contexts.getEventContext().set(Seam.getComponentName(Transaction.class), new NoTransaction());
      MyEntityHome myEntityHome = (MyEntityHome) Component.getInstance("myEntityHome");
      assert myEntityHome != null;
      // verify that the reference to new-instance remains unparsed
      Assert.assertEquals(myEntityHome.getNewInstance().getExpressionString(), "#{simpleEntity}");
      // verify that the message string for the created/updated/deleted message remains unparsed
      Assert.assertEquals(myEntityHome.getCreatedMessage().getExpressionString(), "You #{'created'} it! Yeah!");
      // verify that the id is parsed prior to assignment
      Assert.assertEquals(String.valueOf(myEntityHome.getId()), "11");
      
      ServletLifecycle.endApplication();
   }
   //TODO: write a test for components.xml
}

