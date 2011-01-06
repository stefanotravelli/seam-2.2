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
package org.jboss.seam.example.common.test.selenium;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Selects date using Selenium in calendar provided by framework. Each framework
 * is responsible for managing movement in calendar grid
 * 
 * @author kpiwko
 * 
 */
public enum SeleniumDateSelector
{
   RICHFACES
   {
      @Override
      public void setDay(SeamSelenium browser, int day)
      {
         String locator = "xpath=//td[contains(@class, 'rich-calendar-cell') and not(contains(@class,'rich-calendar-boundary-dates')) and text() ='" + Integer.toString(day) + "']";
         browser.waitForElement(locator);
         browser.click(locator);
      }
      
      @Override
      public void setMonth(SeamSelenium browser, int month)
      {
         String monthLocator = "xpath=//div[@class='rich-calendar-tool-btn' and contains(.,',')]";
         StringTokenizer stk = new StringTokenizer(browser.getText(monthLocator), ",");
         String calMonth = stk.nextToken().trim();
         int steps = monthStepsCount(calMonth, month);
         
         movement(browser, "xpath=//div[@class='rich-calendar-tool-btn' and normalize-space(text())='<']", "xpath=//div[@class='rich-calendar-tool-btn' and normalize-space(text())='>']", steps);
      }
      
      @Override
      public void setYear(SeamSelenium browser, int year)
      {
         String yearLocator = "xpath=//div[@class='rich-calendar-tool-btn' and contains(.,',')]";
         StringTokenizer stk = new StringTokenizer(browser.getText(yearLocator), ",");
         // omit first token
         stk.nextToken();
         String calYear = stk.nextToken().trim();
         int steps = yearStepsCount(calYear, year);
         
         movement(browser, "xpath=//div[@class='rich-calendar-tool-btn' and normalize-space(text())='<<']", "xpath=//div[@class='rich-calendar-tool-btn' and normalize-space(text())='>>']", steps);
      }
      
   },
   ICEFACES
   {
      @Override
      public void setDay(SeamSelenium browser, int day)
      {
         String locator = "xpath=//td[normalize-space(@class)='iceSelInpDateDay']/a[./span/text()='" + Integer.toString(day) + "']";
         browser.waitForElement(locator);
         browser.click(locator);
      }
      
      @Override
      public void setMonth(SeamSelenium browser, int month)
      {
         String monthLocator = "xpath=//td[@class='iceSelInpDateMonthYear'][2]";
         String calMonth = browser.getText(monthLocator).trim();
         int steps = monthStepsCount(calMonth, month);
         
         movement(browser, "xpath=//td[@class='iceSelInpDateMonthYear'][1]/a", "xpath=//td[@class='iceSelInpDateMonthYear'][3]/a", steps);
      }
      
      @Override
      public void setYear(SeamSelenium browser, int year)
      {
         String yearLocator = "xpath=//td[@class='iceSelInpDateMonthYear'][6]";
         String calYear = browser.getText(yearLocator).trim();
         int steps = yearStepsCount(calYear, year);
         
         movement(browser, "xpath=//td[@class='iceSelInpDateMonthYear'][5]/a", "xpath=//td[@class='iceSelInpDateMonthYear'][7]/a", steps);
      }
      
      /**
       * IceFaces forces partial submit, so we must wait for page reload 
       */
      @Override
      protected void click(SeamSelenium browser, String locator)
      {         
         browser.clickAndWait(locator);
      }
      
   };
   
   /**
    * Selects date using Selenium browser
    * 
    * @param browser
    *           Selenium browser instance
    * @param date
    *           Date to be selected
    */
   public void setDate(SeamSelenium browser, Date date)
   {
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      
      setYear(browser, cal.get(Calendar.YEAR));
      setMonth(browser, cal.get(Calendar.MONTH));
      setDay(browser, cal.get(Calendar.DAY_OF_MONTH));
   }
   
   /**
    * Selects day in calendar
    * 
    * @param browser
    *           Selenium browser instance
    * @param day
    *           Integer containing the day to be selected, indexing from 1
    */
   protected abstract void setDay(SeamSelenium browser, int day);
   
   /**
    * Selects month in calendar
    * 
    * @param browser
    *           Selenium browser instance
    * @param month
    *           Integer containing the month to be selected, indexing from 1
    */
   protected abstract void setMonth(SeamSelenium browser, int month);
   
   /**
    * Selects year in calendar
    * 
    * @param browser
    *           Selenium browser instance
    * @param year
    *           Integer containing the year to be selected
    */
   protected abstract void setYear(SeamSelenium browser, int year);
   
   /**
    * Determines direction of month in calendar interface
    * 
    * @param calMonth
    *           Which month is currently shown on calendar
    * @param month
    *           Which month is desired to be set, indexing from 1
    * @return Number of steps which must be done in either of direction, where
    *         sign has meaning:
    *         <ul>
    *         <li>- goes to past</li>
    *         <li>+ goes to future</li>
    *         </ul>
    */
   protected int monthStepsCount(String calMonth, int month)
   {
      final List<String> months = Arrays.asList((new DateFormatSymbols()).getMonths());
      int mindex = months.indexOf(calMonth);
      if (mindex == -1)
         throw new IllegalArgumentException("Unknown month: " + calMonth + " for locale: " + Locale.getDefault());
      
      return month - (mindex + 1);
   }
   
   /**
    * Determines direction of year in calendar interface
    * 
    * @param calYear
    *           Which year is currently shown on calendar
    * @param year
    *           Which month is desired to be set, indexing from 1
    * @return Number of steps which must be done in either of direction, where
    *         sign has meaning:
    *         <ul>
    *         <li>- goes to past</li>
    *         <li>+ goes to future</li>
    *         </ul>
    */
   protected final int yearStepsCount(String calYear, int year)
   {
      int yindex;
      try
      {
         yindex = Integer.valueOf(calYear);
      }
      catch (NumberFormatException nfe)
      {
         throw new IllegalArgumentException("Invalid year: " + calYear, nfe);
      }
      
      return year - yindex;
   }
   
   /**
    * Moves in either backward or forward direction according to step count.
    * Uses locator of element for both directions.
    * 
    * @param browser
    *           Selenium browser instance
    * @param backLocator
    *           Element which moves calendar to past
    * @param forwardLocator
    *           Element which moves calendar to future
    * @param steps
    *           Number of steps to be done, determined by monthStepsCount() or
    *           yearStepsCount() function
    * @see SeleniumDateSelector#monthStepsCount(String, int)
    * @see SeleniumDateSelector#yearStepsCount(String, int)
    */
   protected void movement(SeamSelenium browser, String backLocator, String forwardLocator, int steps)
   {
      // going to past
      if (steps < 0)
      {
         for (int i = 0; i > steps; i--)
            click(browser, backLocator);
      }
      // going to future
      else
      {
         for (int i = 0; i < steps; i++)
            click(browser, forwardLocator);
      }
   }
   
   /**
    * Clicks on element. Allow differentiate action according to framework, such
    * as wait for page to load for IceFaces
    * 
    * @param browser Selenium browser
    * @param locator Locator of element to be clicked on
    */
   protected void click(SeamSelenium browser, String locator)
   {
      browser.click(locator);
   }
}
