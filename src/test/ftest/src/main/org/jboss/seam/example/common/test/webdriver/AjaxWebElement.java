package org.jboss.seam.example.common.test.webdriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Adds AJAX functionality to an ordinary WebElement
 * 
 * @author kpiwko
 * 
 */
public interface AjaxWebElement extends WebElement
{

   public static final int DEFAULT_WAIT_TIME = 3000;

   //@Override
   public AjaxWebElement findElement(By by);

   public void setWaitTime(int millis);

   public void clickAndWait();

   public void clearAndSendKeys(CharSequence... keysToSend);
}
