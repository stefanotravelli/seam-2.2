package org.jboss.seam.example.excel.test.selenium;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertEquals;

public class SeleniumExcelTest extends SeamSeleniumTest
{
   public static final String HOME_PAGE = "/home.seam";
   public static final String HOME_PAGE_TITLE = "Microsoft® Excel® Export examples";
   
   @BeforeMethod
   @Override
   public void setUp() {
      super.setUp();
      browser.open(CONTEXT_PATH + HOME_PAGE);
   }
   
   /**
    * Place holder - just verifies that example deploys
    */
   @Test
   public void homePageLoadTest() {
      assertEquals("Unexpected page title.", HOME_PAGE_TITLE, browser.getTitle());
   }
}
