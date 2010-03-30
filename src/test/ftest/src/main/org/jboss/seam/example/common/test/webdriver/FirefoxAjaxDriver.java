package org.jboss.seam.example.common.test.webdriver;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.firefox.FirefoxDriver;

public class FirefoxAjaxDriver extends FirefoxDriver implements AjaxWebDriver
{

   private int waitTime;

   public FirefoxAjaxDriver()
   {
      this(AjaxWebElement.DEFAULT_WAIT_TIME);
   }

   public FirefoxAjaxDriver(int waitTime)
   {
      this.waitTime = waitTime;
   }

   //@Override
   public AjaxWebElement findElement(By by)
   {
      return new DelegatedWebElement(super.findElement(by), waitTime);
   }

   //@Override
   public void setWaitTime(int millis)
   {
      this.waitTime = millis;
   }
   
   public boolean isElementPresent(By by)
   {
      try
      {
         findElement(by);
         return true;
      }
      catch (NoSuchElementException e)
      {
         return false;
      }
   }
}
