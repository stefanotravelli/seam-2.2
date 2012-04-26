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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

/**
 * Adds AjaxWebElement functionality to an ordinary WebElement
 * 
 * @author kpiwko
 * 
 */
public class DelegatedWebElement implements AjaxWebElement
{
   
   private int waitTime;
   private WebElement element;
   
   public DelegatedWebElement(WebElement element)
   {
      this(element, DEFAULT_WAIT_TIME);
   }
   
   public DelegatedWebElement(WebElement element, int waitTime)
   {
      this.element = element;
      this.waitTime = waitTime;
   }
   
   // @Override
   public void clear()
   {
      element.clear();
   }
   
   // @Override
   public void clearAndSendKeys(CharSequence... keysToSend)
   {
      element.clear();
      element.sendKeys(keysToSend);
   }
   
   // @Override
   public void click()
   {
      element.click();
   }
   
   // @Override
   public void clickAndWait()
   {
      element.click();
      try
      {
         Thread.sleep(waitTime);
      }
      catch (InterruptedException e)
      {
      }
   }
   
   public void clickAndWait(int millis)
   {
      element.click();
      try
      {
         Thread.sleep(millis);
      }
      catch (InterruptedException e)
      {
      }
   }
   
   // @Override
   public AjaxWebElement findElement(By by)
   {
      return new DelegatedWebElement(element.findElement(by));
   }
   
   // @Override
   public List<WebElement> findElements(By by)
   {
      List<WebElement> elements = new ArrayList<WebElement>();
      List<WebElement> original = element.findElements(by);
      if (original == null || original.size() == 0)
         return Collections.emptyList();
      
      for (WebElement e : original)
         elements.add(new DelegatedWebElement(e));
      
      return elements;
   }
   
   // @Override
   public String getAttribute(String name)
   {
      return element.getAttribute(name);
   }

   // @Override
   public String getCssValue(String propertyName)
   {
      return element.getCssValue(propertyName);
   }

   // @Override
   public Point getLocation()
   {
      return element.getLocation();
   }

   // @Override
   public Dimension getSize()
   {
      return element.getSize();
   }

   // @Override
   public String getTagName()
   {
      return element.getTagName();
   }
   
   // @Override
   public String getText()
   {
      return element.getText();
   }

   // @Override
   public boolean isDisplayed()
   {
      return element.isDisplayed();
   }
   
   // @Override
   public boolean isEnabled()
   {
      return element.isEnabled();
   }
   
   // @Override
   public boolean isSelected()
   {
      return element.isSelected();
   }
   
   // @Override
   public void sendKeys(CharSequence... keysToSend)
   {
      element.sendKeys(keysToSend);
   }
   
   // @Override
   public void setWaitTime(int millis)
   {
      this.waitTime = millis;
   }
   
   // @Override
   public void submit()
   {
      element.submit();
   }

}
