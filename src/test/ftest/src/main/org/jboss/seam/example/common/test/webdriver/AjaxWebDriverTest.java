package org.jboss.seam.example.common.test.webdriver;

import static org.testng.Assert.fail;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

/**
 * Abstract class for all WebDriver tests. Provides running a WebDriverInstance
 * before each executed test and shuts it down after its execution.
 * 
 * @author kpiwko
 * 
 */
public abstract class AjaxWebDriverTest
{
   protected AjaxWebDriver driver;
   protected String serverURL;
   protected String contextPath;

   @BeforeMethod
   @Parameters(value = {
         "browser", "server.url", "context.path"
   })
   public void createDriver(String browser, String serverURL, String contextPath)
   {
      try
      {
         this.driver = AjaxWebDriverFactory.getDriver(browser);
      }
      catch (IllegalArgumentException e)
      {
         fail("Unable to instantiate browser of type: " + browser + ", available browsers are: " + AjaxWebDriverFactory.availableBrowsers());
      }
      catch (NullPointerException e)
      {
         fail("Unable to instantiate browser of type: " + browser + ", available browsers are: " + AjaxWebDriverFactory.availableBrowsers());
      }

      this.serverURL = serverURL;
      this.contextPath = contextPath;
   }

   @AfterMethod
   public void closeDriver()
   {
      driver.close();
   }

}
