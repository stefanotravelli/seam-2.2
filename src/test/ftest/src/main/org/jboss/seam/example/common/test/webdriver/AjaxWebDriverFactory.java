package org.jboss.seam.example.common.test.webdriver;

/**
 * Creates appropriate WebDriver driver according to passed string.
 * To hold a compatibility with Selenium, it is able to use Selenium
 * based browser names
 * @author kpiwko 
 */
public class AjaxWebDriverFactory
{
   public static enum Browser
   {
      firefox
      {
         //@Override
         public AjaxWebDriver getDriver()
         {
            return new FirefoxAjaxDriver();
         }
      };

      public abstract AjaxWebDriver getDriver();
   }

   public static final AjaxWebDriver getDriver(String browser) throws IllegalArgumentException, NullPointerException
   {
      if(browser.contains("firefox")) {
         return Browser.firefox.getDriver();
      }
      
      return Browser.valueOf(browser).getDriver();
   }

   public static final String availableBrowsers()
   {
      StringBuilder sb = new StringBuilder();
      for (Browser b : Browser.values())
         sb.append("'").append(b.toString()).append("' ");
      return sb.toString();
   }
}
