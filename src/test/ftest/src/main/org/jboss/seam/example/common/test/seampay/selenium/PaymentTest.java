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
package org.jboss.seam.example.common.test.seampay.selenium;

import static org.testng.AssertJUnit.*;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author jharting
 * 
 */
public class PaymentTest extends SeamSeleniumTest
{

   protected NumberFormat nf = NumberFormat.getNumberInstance();

   @Override
   @BeforeMethod
   public void setUp()
   {
      super.setUp();
      browser.open(CONTEXT_PATH + getProperty("MAIN_PAGE"));
      browser.waitForPageToLoad(TIMEOUT);
   }

   /**
    * Submits new payment and asserts that remaining account balance is correct.
    * 
    * @throws ParseException
    */
   @Test
   public void payOnceTest() throws ParseException
   {
      int account = 0;
      BigDecimal amount = new BigDecimal(10);
      String to = "foo";
      String accountLinkLocator = MessageFormat.format(getProperty("ACCOUNT_TABLE_LINK"), account);
      String accountBalanceLocator = MessageFormat.format(getProperty("ACCOUNT_TABLE_BALANCE"), account);

      browser.click(accountLinkLocator);
      browser.waitForPageToLoad(TIMEOUT);
      BigDecimal expectedBalance = BigDecimal.valueOf(parseBalance(browser.getText(accountBalanceLocator)));
      submitPayment(to, amount.toString(), getProperty("PAYMENT_ONLY_ONCE_RADIO"));
      assertTrue("Scheduled payment not confirmed.", browser.isTextPresent(MessageFormat.format(getProperty("PAYMENT_CONFIRMATION_MESSAGE"), to)));
      assertEquals("Invalid count of payments.", 1, browser.getXpathCount(getProperty("PAYMENTS_COUNT")));
      browser.click(accountLinkLocator);
      browser.waitForPageToLoad(TIMEOUT);
      assertEquals("No money were subtracted from account", expectedBalance.subtract(amount), BigDecimal.valueOf(parseBalance(browser.getText(accountBalanceLocator))));
   }

   /**
    * Submits new payment with one minute interval and verifies the balance
    * after 60 seconds
    * 
    * @throws ParseException
    * @throws InterruptedException
    */
   @Test
   public void payEveryMinuteTest() throws ParseException, InterruptedException
   {
      int account = 1;
      BigDecimal amount = new BigDecimal(10);
      String to = "foo";
      String accountLinkLocator = MessageFormat.format(getProperty("ACCOUNT_TABLE_LINK"), account);
      String accountBalanceLocator = MessageFormat.format(getProperty("ACCOUNT_TABLE_BALANCE"), account);

      browser.click(accountLinkLocator);
      browser.waitForPageToLoad(TIMEOUT);
      // create new payment
      submitPayment(to, amount.toString(), getProperty("PAYMENT_EVERY_MINUTE_RADIO"));
      assertTrue("Scheduled payment not confirmed.", browser.isTextPresent(MessageFormat.format(getProperty("PAYMENT_CONFIRMATION_MESSAGE"), to)));
      assertEquals("Invalid count of payments.", 1, browser.getXpathCount(getProperty("PAYMENTS_COUNT")));
      // wait
      Thread.sleep(5000);
      // get first balance
      browser.click(accountLinkLocator);
      browser.waitForPageToLoad(TIMEOUT);
      BigDecimal firstBalance = BigDecimal.valueOf(parseBalance(browser.getText(accountBalanceLocator)));
      // wait 60 seconds
      Thread.sleep(60000);
      // get second balance
      browser.click(accountLinkLocator);
      browser.waitForPageToLoad(TIMEOUT);
      BigDecimal secondBalance = BigDecimal.valueOf(parseBalance(browser.getText(accountBalanceLocator)));
      BigDecimal expectedSecondBalance = firstBalance.subtract(amount);
      assertEquals("No money were subtracted from account after a minute", expectedSecondBalance, secondBalance);
   }

   protected void submitPayment(String to, String amount, String radio)
   {
      browser.type(getProperty("PAYMENT_TO_FIELD"), to);
      browser.type(getProperty("PAYMENT_AMOUNT_FIELD"), amount.toString());
      browser.check(radio);
      browser.click(getProperty("PAYMENT_SUBMIT"));
      browser.waitForPageToLoad(TIMEOUT);
   }

   protected Double parseBalance(String text) throws ParseException
   {
      // dirty but can hardly be parsed nicer
      String number = text.replaceAll("\\$", new String()).replaceAll(" ", new String()).trim();
      return nf.parse(number).doubleValue();
   }
}
