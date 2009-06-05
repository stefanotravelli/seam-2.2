package org.jboss.seam.test.unit;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.navigation.Pages;
import org.testng.annotations.Test;

/**
 * This test verifies that the page descriptor is parsed correctly and that the
 * Page instance returned by the Pages component are based on the matching Page
 * definition as well as any matching wild-card pages in the page stack.
 */
public class PageDescriptorTest extends AbstractPageTest
{
   /**
    * Verify that the switch enabled flag is true by default, but false
    * for the debug page.
    */
   @Test(enabled = true)
   public void testSwitchEnabledFlag()
   {
      Pages pages = Pages.instance();
      
      assert pages.getPage("/action-test01a.xhtml").isSwitchEnabled();
      assert !pages.getPage("/debug.xhtml").isSwitchEnabled();
   }
   
   /**
    * Verify that the Pages component determines when a description exists in the Page stack and when
    * it does not.
    */
   @Test(enabled = true)
   public void testHasDescription()
   {
      Pages pages = Pages.instance();
      
      assert pages.hasDescription("/action-test01b.xhtml");
      assert pages.hasDescription("/action-test01a.xhtml");
      assert !pages.hasDescription("/action-test02.xhtml");
   }
   
   /**
    * Verify that the description can be retrieved from the page stack when a wild-card view-id 
    * description has been specified.  Also verifies that a description specific to a page overrides
    * the wild-card description.
    */
   @Test(enabled = true)
   public void testGetAndResolveDescription()
   {
      Pages pages = Pages.instance();
      String fineDescription = "fine-description";
      Contexts.getEventContext().set("fineDescription", fineDescription);
      
      assert "coarse-description".equals(pages.getDescription("/action-test01a.xhtml")) :
         "Expecting the wild-card description to be returned as no description was provided for the view-id";
      assert fineDescription.equals(pages.renderDescription("/action-test01b.xhtml")) :
         "Expecting the description for the view-id to be returned";
   }
}
