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
package org.jboss.seam.example.numberguess.test.selenium;

import java.text.MessageFormat;
import static org.testng.AssertJUnit.fail;
import static org.testng.AssertJUnit.assertTrue;

import org.jboss.seam.example.common.test.numberguess.selenium.CommonNumberGuessTest;
import org.testng.annotations.Test;

/**
 * 
 * @author Jozef Hartinger
 *
 */
public class NumberGuessTest extends CommonNumberGuessTest
{

   @Override
   protected void enterGuess(int guess)
   {
      if (browser.isElementPresent(getProperty("GUESS_FIELD")))
      {
         // using input text field
         super.enterGuess(guess);
      }
      else
      {
         if (browser.isElementPresent(getProperty("GUESS_MENU")))
         {
            // using menu
            browser.select(getProperty("GUESS_MENU"), String.valueOf(guess));
         }
         else if (browser.isElementPresent(getProperty("GUESS_RADIO")))
         {
            // using radio buttons
            int min = Integer.parseInt(browser.getText(getProperty("GUESS_MIN_VALUE")));
            int radio = guess - min;
            browser.check(MessageFormat.format(getProperty("GUESS_RADIO_ITEM"), radio));
         } else {
            fail("Unable to enter guess. No input found.");
         }
         browser.click(getProperty("GUESS_SUBMIT"));
         browser.waitForPageToLoad(TIMEOUT);
      }
   }
   
   @Test
   public void cheatingTest() {
      int number;
      
      browser.click(getProperty("CHEAT_BUTTON"));
      browser.waitForPageToLoad(TIMEOUT);
      browser.click(getProperty("CHEAT_YES_BUTTON"));
      browser.waitForPageToLoad(TIMEOUT);
      number = Integer.parseInt(browser.getText(getProperty("CHEAT_NUMBER")));
      browser.click(getProperty("CHEAT_DONE_BUTTON"));
      browser.waitForPageToLoad(TIMEOUT);
      enterGuess(number);
      assertTrue("User should win when cheating. Random number was " + number, isOnWinPage());
   }

}
