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
package org.jboss.seam.example.common.test.numberguess.selenium;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 * 
 * @author Jozef Hartinger
 *
 */
public class CommonNumberGuessTest extends SeamSeleniumTest
{

   @Override
   @BeforeMethod
   public void setUp()
   {
      super.setUp();
      browser.open(CONTEXT_PATH + getProperty("MAIN_PAGE"));
      browser.waitForPageToLoad(TIMEOUT);
   }

   @Test
   public void smartTest()
   {

      int min;
      int max;
      int guess;
      int i = 0;

      while (browser.getLocation().contains(getProperty("GUESS_LOCATION")))
      {
         if (i > 9)
         {
            fail("Game should not be longer than 10 guesses");
         }
         min = Integer.parseInt(browser.getText(getProperty("GUESS_MIN_VALUE")));
         max = Integer.parseInt(browser.getText(getProperty("GUESS_MAX_VALUE")));
         guess = min + ((max - min) / 2);
         enterGuess(guess);
         i++;
      }
      assertTrue("Win page expected after playing smart.", isOnWinPage());
   }

   @Test
   public void linearTest()
   {
      int guess = 0;
      
      while (browser.getLocation().contains(getProperty("GUESS_LOCATION")))
      {
         enterGuess(++guess);
         assertTrue("Guess count exceeded.", guess <= 10);
      }
      if (guess < 10)
      {
         assertTrue("Player should not lose before 10th guess.", isOnWinPage());
      }
      else
      {
         assertTrue("After 10th guess player should lose or win.", isOnLosePage() || isOnWinPage());
      }

   }

   protected void enterGuess(int guess)
   {
      browser.type(getProperty("GUESS_FIELD"), String.valueOf(guess));
      browser.click(getProperty("GUESS_SUBMIT"));
      browser.waitForPageToLoad(TIMEOUT);
   }

   protected boolean isOnWinPage()
   {
      return browser.getLocation().contains(getProperty("WIN_LOCATION"));
   }

   protected boolean isOnLosePage()
   {
      return browser.getLocation().contains(getProperty("LOSE_LOCATION"));
   }

}
