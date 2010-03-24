package org.jboss.seam.example.common.test.webdriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Modifies WebDriver to return elements with enhanced AJAX functionality
 * 
 * @author kpiwko
 * 
 */
public interface AjaxWebDriver extends WebDriver
{

   //@Override
   public AjaxWebElement findElement(By by);

   public void setWaitTime(int millis);
}
