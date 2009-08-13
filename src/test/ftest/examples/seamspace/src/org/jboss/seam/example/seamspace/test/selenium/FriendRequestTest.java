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
package org.jboss.seam.example.seamspace.test.selenium;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertTrue;


/**
 * This class tests "Send a friend request" at seamspace example. This
 * option is available when user opens somebody's profile.
 * 
 * @author Martin Gencur
 *
 */

public class FriendRequestTest extends SeleniumSeamSpaceTest
{
   public static final String DUKE_IMAGE = "xpath=//img[contains(@src,\"id=2\")]";
   public static final String FRIEND_REQUEST_LINK = "link=Send a friend request";
   public static final String MESSAGE_AREA = "xpath=//textarea[contains(@id,\"introduction\")]";
   public static final String MESSAGE_TEXT = "Hi Duke, how are you?";
   public static final String REQUEST_SEND_BUTTON = "xpath=//input[contains(@value,\"Send request\")]";
   public static final String REQUEST_SENT_MESSAGE = "Friend request sent";
   
   @Test
   public void sendFriendRequestText(){
      browser.clickAndWait(DUKE_IMAGE);
      browser.clickAndWait(FRIEND_REQUEST_LINK);
      browser.type(MESSAGE_AREA, MESSAGE_TEXT);
      browser.clickAndWait(REQUEST_SEND_BUTTON);
      assertTrue("Friend request sent page expected",browser.isTextPresent(REQUEST_SENT_MESSAGE));      
   }   
   
}
