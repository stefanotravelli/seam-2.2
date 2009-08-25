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
package org.jboss.seam.example.ui.test.htmlunit;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import static org.testng.AssertJUnit.fail;
import java.net.URL;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Functional test for uploadLink and graphicImage testcases of UI example
 * 
 * @author mgencur
 *
 */
public class HtmlUnitUITest
{
   public static final String PAGE_URL = "http://localhost:8080/seam-ui";
   public static final String HOME_PAGE_TITLE = "UI Example:";
   public static final String FILE_UPLOAD_FILE= "//input[@type='file']";
   public static final String FILE_UPLOAD_UPDATE="//input[@type='submit'][@value='Update']";
   public static final String IMAGE_TO_UPLOAD = "photo.jpg";
   public static final String FILE_UPLOAD_RESPONSE="//ul/li[contains(text(),'Successfully updated')]";
   public static final String FILE_UPLOAD_LINK = "//a[contains(@href,'fileUpload')]";
   public static final String GRAPHIC_IMAGE_LINK = "//a[contains(@href,'graphicImage')]";
   public static final String IMAGE = "//img";
   
   public WebClient wc;
   public HtmlPage page;   
   
   @BeforeMethod
   public void setUp() throws Exception{
      URL url = new URL(PAGE_URL);      
      wc = new WebClient(BrowserVersion.FIREFOX_2);      
      page = (HtmlPage) wc.getPage(url);  
   }

   
   @AfterMethod
   public void tearDown() {
      wc.closeAllWindows();       
   }
   
   
   @Test
   public void homePageLoadTest()
   {      
      assertEquals("Unexpected page title.", HOME_PAGE_TITLE, page.getTitleText());
   } 
   
   
   @Test(dependsOnMethods={"homePageLoadTest"})
   public void fileUploadTest() throws IOException {
      final HtmlAnchor linkEl = (HtmlAnchor) page.getFirstByXPath(FILE_UPLOAD_LINK);
      
      final HtmlPage uploadPage = (HtmlPage) linkEl.click();
      if (uploadPage == null){
         fail("Could not read page");
      }
            
      final HtmlInput el1 = (HtmlInput) uploadPage.getFirstByXPath(FILE_UPLOAD_FILE);
      if (el1 == null) {
         fail("Element file upload file doesn't exist");
      } else {         
         el1.type(IMAGE_TO_UPLOAD);
      }      
      
      final HtmlInput el2 = (HtmlInput) uploadPage.getFirstByXPath(FILE_UPLOAD_UPDATE);
      final HtmlPage finishPage = (HtmlPage) el2.click();
      final HtmlElement el3 = (HtmlElement) finishPage.getFirstByXPath(FILE_UPLOAD_RESPONSE);
      
      assertFalse("Page should contain \"Successfully updated\"", el3 == null);
   } 
   
   
   @Test(dependsOnMethods={"homePageLoadTest","fileUploadTest"})
   public void graphicImageTest() throws IOException { 
      final HtmlAnchor linkEl = (HtmlAnchor) page.getFirstByXPath(GRAPHIC_IMAGE_LINK);
      
      final HtmlPage graphicPage = (HtmlPage) linkEl.click();
      if (graphicPage == null){
         fail("Could not read page");
      }
      
      final HtmlImage image = (HtmlImage) graphicPage.getFirstByXPath(IMAGE);
      
      assertFalse("Page should contain image of Pete Muir", image == null);      
   }      
}
