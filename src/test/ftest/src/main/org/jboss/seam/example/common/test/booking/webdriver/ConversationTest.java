/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.example.common.test.booking.webdriver;

import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.example.common.test.webdriver.AjaxWebDriverTest;
import org.jboss.seam.example.common.test.webdriver.AjaxWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.Test;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import com.thoughtworks.selenium.Wait;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * This class tests functionality of conversations in booking-like examples. 
 *  
 * @author Martin Gencur
 * 
 */
public class ConversationTest extends AjaxWebDriverTest
{
   public int timeout = 5000;
         
   private final String DEFAULT_USERNAME = "demo";
   private final String DEFAULT_PASSWORD = "demo";
   
   private final int W1 = 0; //first window
   private final int W2 = 1; //second window
   
   public final String MAIN_PAGE = "/main.seam";
   public final String HOME_PAGE = "/home.seam";
   public final String HOTEL1 = "Hotel Rouge";
   public final String HOTEL2 = "Doubletree";
   public final String PAGE_TITLE = "JBoss Suites: Seam Framework";
   public final String WORKSPACE_BOOKING_TEXT_HOTEL1 = "Book hotel: " + HOTEL1;
   public final String WORKSPACE_VIEW_TEXT_HOTEL2 = "View hotel: " + HOTEL2;
   public final By LOGIN_USERNAME_FIELD = By.id("login:username");
   public final By LOGIN_PASSWORD_FIELD = By.id("login:password");
   public final By LOGIN_SUBMIT = By.id("login:login");
   public final By SEARCH_STRING_FIELD = By.id("searchCriteria:searchString");
   public final By SPINNER = By.id("searchCriteria:Spinner:connection-working");
   public final By SEARCH_RESULT_TABLE = By.xpath("//table[@id = 'searchResults:hotels']/tbody");
   public final By NO_HOTELS_FOUND = By.id("searchResults:NoHotelsFoundMessage");
   public final By WORKSPACE_LINK_0 = By.id("ConversationListForm:ConversationListDataTable:0:EntryDescriptionLink");
   public final By WORKSPACE_LINK_1 = By.id("ConversationListForm:ConversationListDataTable:1:EntryDescriptionLink");
   public final By SEARCH_RESULT_TABLE_FIRST_ROW_LINK = By.id("searchResults:hotels:0:viewHotel");
   public final By BOOKING_BOOK = By.id("hotel:bookHotel");
   public final By BOOKING_CANCEL = By.id("hotel:cancel");
   public final By LOGOUT = By.id("logout");
   public final By WORKSPACE_TABLE_ROW_COUNT = By.xpath("//table[@id='ConversationListForm:ConversationListDataTable']/tbody/tr");
   
   public List<String> windows = new ArrayList();      
   
   @BeforeMethod
   public void setUp() 
   {
      //first window is opened automatically so just choose a page to display
      driver.get(serverURL + contextPath + MAIN_PAGE);
      //manually open second window with javascript
      openWindow(driver, serverURL + contextPath + MAIN_PAGE, "window2");
      //get window handles so that we are able to access them by index
      for (String h: driver.getWindowHandles())
      {
         windows.add(h);
      }
   }
   
   @AfterMethod
   public void tearDown() 
   {
      closeWindows(); 
   }
   
   @Test
   public void testConversations() 
   {
      driver.switchTo().window(windows.get(W1));

      if (!isLoggedIn(driver)) 
      {
         login(driver);
      }
      
      enterSearchQueryUsingAJAX(driver, HOTEL1);
      pause(timeout);
      driver.findElement(SEARCH_RESULT_TABLE_FIRST_ROW_LINK).clickAndWait();
      driver.findElement(BOOKING_BOOK).click();
      
      driver.switchTo().window(windows.get(W2));
      driver.navigate().refresh();
      pause(timeout);
      
      if (!isLoggedIn(driver)) 
      {
         login(driver);
      }
      pause(timeout);
      enterSearchQueryUsingAJAX(driver, HOTEL2);
      driver.findElement(SEARCH_RESULT_TABLE_FIRST_ROW_LINK).clickAndWait();
      
      driver.switchTo().window(windows.get(W1));
      driver.navigate().refresh();
      pause(timeout);
      
      assertEquals("#1 workspace not present in workspace table", WORKSPACE_BOOKING_TEXT_HOTEL1, driver.findElement(WORKSPACE_LINK_0).getText());
      assertEquals("#2 workspace not present in workspace table", WORKSPACE_VIEW_TEXT_HOTEL2, driver.findElement(WORKSPACE_LINK_1).getText());
           
      //Switch window 1 to second workspace
      driver.findElement(WORKSPACE_LINK_1).clickAndWait();
      // Switch window 1 back to first workspace
      driver.findElement(WORKSPACE_LINK_1).clickAndWait();
      driver.switchTo().window(windows.get(W2));
      // End conversation in window 2
      driver.findElement(BOOKING_CANCEL).clickAndWait();
      
      assertTrue("Workspace failure.", driver.findElements(WORKSPACE_TABLE_ROW_COUNT).size() == 1);
   }
   
   public void enterSearchQueryUsingAJAX(final AjaxWebDriver driver, String query) {
      driver.findElement(SEARCH_STRING_FIELD).clearAndSendKeys(query.substring(0, query.length() - 1));
      driver.findElement(SEARCH_STRING_FIELD).sendKeys(query.substring(query.length() - 1));

      // wait for javascript to show spinner
      try 
      {
          Thread.sleep(3000);
      } 
      catch (InterruptedException e) 
      {
      }
        
      new Wait() 
      {
         @Override
         public boolean until()
         {
            return (driver.isElementPresent(SEARCH_RESULT_TABLE) || driver.isElementPresent(NO_HOTELS_FOUND));
         }
      }.wait("Search results not found.");
   }
   
   public boolean login(AjaxWebDriver driver) 
   {
      return login(driver, DEFAULT_USERNAME, DEFAULT_PASSWORD);
   }

   public boolean login(AjaxWebDriver driver, String username, String password) 
   {
      /*if (isLoggedIn(driver)) {
          fail("User already logged in.");
      }*/
      driver.get(serverURL + contextPath + HOME_PAGE);
      pause(timeout);
      if (!driver.getTitle().equals(PAGE_TITLE)) 
      {
          return false;
      }
      driver.findElement(LOGIN_USERNAME_FIELD).sendKeys(username);
      driver.findElement(LOGIN_PASSWORD_FIELD).sendKeys(password);
      driver.findElement(LOGIN_SUBMIT).clickAndWait();
      pause(timeout);
      return isLoggedIn(driver);
   }

   public boolean isLoggedIn(AjaxWebDriver driver) 
   {
      return driver.isElementPresent(LOGOUT);
   }
   
   public void openWindow(AjaxWebDriver driver, String url, String windowName)
   {
      ((JavascriptExecutor) driver).executeScript("window.open('"+ url +"','" + windowName +"')");
   }
   
   public void closeWindows()
   {
      for (String h: windows)
      {
         driver.switchTo().window(h);
         driver.close();
      }
   }
   
   private void pause(int millis)
   {
      try
      {
         Thread.sleep(millis);
      }
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }
   }
}
