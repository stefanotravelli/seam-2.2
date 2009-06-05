package org.jboss.seam.example.remoting.chatroom.test.selenium;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertEquals;

public class SeleniumChatroomTest extends SeamSeleniumTest
{
   public static final String HOME_PAGE = "/chatroom.seam";
   public static final String HOME_PAGE_TITLE = "Chat Room Example";
   
   @BeforeMethod
   @Override
   public void setUp() {
      super.setUp();
      browser.open(CONTEXT_PATH + HOME_PAGE);
   }
   
   @Test // place holder - should be replaced by better tests as soon as JBSEAM-3944 is resolved
   public void homePageLoadTest() {
      assertEquals("Unexpected page title.", HOME_PAGE_TITLE, browser.getTitle());
   }

}
