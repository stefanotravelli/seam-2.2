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
package org.jboss.seam.example.quartz.test.selenium;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;

import static org.testng.AssertJUnit.*;

import org.jboss.seam.example.common.test.seampay.selenium.PaymentTest;
import org.testng.annotations.Test;

/**
 * 
 * @author jharting
 * 
 */
public class QuartzPaymentTest extends PaymentTest
{

   /**
    * Submits new payment using CRON and verifies that the balance is subtracted
    * correctly
    * 
    * @throws ParseException
    * @throws InterruptedException
    */
   @Test
   public void testCRON() throws ParseException, InterruptedException
   {
      int account = 3;
      BigDecimal amount = new BigDecimal(10);
      String to = "foo";
      String accountLinkLocator = MessageFormat.format(getProperty("ACCOUNT_TABLE_LINK"), account);
      String accountBalanceLocator = MessageFormat.format(getProperty("ACCOUNT_TABLE_BALANCE"), account);
      // send every 20 seconds
      String cronExpression = "0/20 * * * * ?";

      browser.click(accountLinkLocator);
      browser.waitForPageToLoad(TIMEOUT);
      // submit new cron job
      browser.type(getProperty("PAYMENT_TO_FIELD"), to);
      browser.type(getProperty("PAYMENT_AMOUNT_FIELD"), amount.toString());
      browser.type(getProperty("PAYMENT_CRON_FIELD"), cronExpression);
      browser.click(getProperty("PAYMENT_CRON_SUBMIT"));
      browser.waitForPageToLoad(TIMEOUT);
      assertTrue("Scheduled payment not confirmed.", browser.isTextPresent(MessageFormat.format(getProperty("PAYMENT_CONFIRMATION_MESSAGE"), to)));
      assertEquals("Invalid count of payments.", 1, browser.getXpathCount(getProperty("PAYMENTS_COUNT")));
      // wait
      Thread.sleep(5000);
      // get balance
      browser.click(accountLinkLocator);
      browser.waitForPageToLoad(TIMEOUT);
      BigDecimal firstBalance = BigDecimal.valueOf(parseBalance(browser.getText(accountBalanceLocator)));
      // wait 20 seconds
      Thread.sleep(20000);
      // get balance after 20 seconds
      browser.click(accountLinkLocator);
      browser.waitForPageToLoad(TIMEOUT);
      BigDecimal secondBalance = BigDecimal.valueOf(parseBalance(browser.getText(accountBalanceLocator)));
      // wait 20 seconds
      Thread.sleep(20000);
      // get balance after 40 seconds
      browser.click(accountLinkLocator);
      browser.waitForPageToLoad(TIMEOUT);
      BigDecimal thirdBalance = BigDecimal.valueOf(parseBalance(browser.getText(accountBalanceLocator)));

      BigDecimal expectedSecondBalance = firstBalance.subtract(amount);
      BigDecimal expectedThirdBalance = firstBalance.subtract(amount).subtract(amount);
      assertEquals("Incorrect balance after 20 seconds.", expectedSecondBalance, secondBalance);
      assertEquals("Incorrect balance after 40 seconds.", expectedThirdBalance, thirdBalance);
   }
}
