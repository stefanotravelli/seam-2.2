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
      },      
      iexplorer
      {
         public AjaxWebDriver getDriver()
         {
            return new InternetExplorerAjaxDriver();
         }
      };

      public abstract AjaxWebDriver getDriver();
   }

   public static final AjaxWebDriver getDriver(String browser) throws IllegalArgumentException, NullPointerException
   {
      if(browser.contains("firefox")) {
         return Browser.firefox.getDriver();
      }
      
      if(browser.contains("explore")) {
         return Browser.iexplorer.getDriver();
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
