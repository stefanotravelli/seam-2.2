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
package org.jboss.seam.example.dvd.test.selenium;

import static org.testng.AssertJUnit.*;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import org.testng.annotations.Test;

/**
 * This class tests shopping cart
 * 
 * @author jbalunas
 * @author jharting
 * 
 */
public class ShoppingCartTest extends SeleniumDvdTest {
   
    private NumberFormat nf = NumberFormat.getNumberInstance();

    @Test(dependsOnGroups = { "search" })
    public void simpleCartTest() {
        String[] dvds = new String[] { "Top Gun", "Pulp Fiction",
                "Forrest Gump" };
        for (String dvd : dvds) {
            addDVDToCart(dvd);
        }
        browser.click(getProperty("CART"));
        browser.waitForPageToLoad(TIMEOUT);
        for (String dvd : dvds) {
            assertTrue("Expected item not present in cart.", browser
                    .isElementPresent(MessageFormat.format(
                            getProperty("CART_TABLE_ROW_BY_NAME"), dvd)));
        }
    }

    @Test(dependsOnMethods = { "simpleCartTest" })
    public void testCartCostCalculation() throws ParseException {
        String[] dvds = new String[] { "Top Gun", "Pulp Fiction", "Top Gun" };
        BigDecimal expectedSum = BigDecimal.ZERO;
        for (String dvd : dvds) {
            addDVDToCart(dvd);
        }
        browser.click(getProperty("CART"));
        browser.waitForPageToLoad(TIMEOUT);
        int items = browser.getXpathCount(getProperty("CART_TABLE_ITEM"))
                .intValue();
        assertNotSame("Cart should not be empty.", 0, items);
        for (int i = 0; i < items; i++) {
            BigDecimal quantity = parseBalance(browser.getValue(MessageFormat.format(
                            getProperty("CART_TABLE_QUANTITY_BY_ID"), i)));
            BigDecimal price = parseBalance(browser.getText(MessageFormat.format(
                            getProperty("CART_TABLE_PRICE_BY_ID"), i)));
            BigDecimal priceForCurrentRow = price.multiply(quantity);
            expectedSum = expectedSum.add(priceForCurrentRow);
        }
        BigDecimal actualSum = parseBalance(browser.getText(getProperty("CART_SUBTOTAL")));
        assertEquals("Price sum in cart is incorrect.", 0, expectedSum
                .compareTo(actualSum));
    }

    @Test(dependsOnMethods = { "simpleCartTest" })
    public void testRemovingCartItem() {
        String dvd = "Top Gun";
        addDVDToCart(dvd);
        browser.click(getProperty("CART"));
        browser.waitForPageToLoad(TIMEOUT);
        assertTrue("DVD is not in the cart.", browser
                .isElementPresent(MessageFormat.format(
                        getProperty("CART_TABLE_ROW_BY_NAME"), dvd)));
        browser.check(MessageFormat.format(
                getProperty("CART_TABLE_CHECKBOX_BY_NAME"), dvd));
        browser.click(getProperty("CART_TABLE_UPDATE_BUTTON"));
        browser.waitForPageToLoad(TIMEOUT);
        assertFalse("Cart item was not removed.", browser
                .isElementPresent(MessageFormat.format(
                        getProperty("CART_TABLE_ROW_BY_NAME"), dvd)));
    }

    /**
     * This method tries purchasing more copies of The Bourne Identity than are
     * available in stock
     */
    @Test(dependsOnMethods = { "simpleCartTest" })
    public void testExceedingAvailableItemLimit() {
        String dvd = "The Bourne Identity";
        String amount = "300";
        addDVDToCart(dvd);
        browser.click(getProperty("CART"));
        browser.waitForPageToLoad(TIMEOUT);
        browser.type(getProperty("CART_TABLE_FIRST_ROW_QUANTITY"), amount);
        browser.click(getProperty("CART_TABLE_UPDATE_BUTTON"));
        browser.waitForPageToLoad(TIMEOUT);
        browser.click(getProperty("CART_PURCHASE_BUTTON"));
        browser.waitForPageToLoad(TIMEOUT);
        browser.click(getProperty("CART_CONFIRM_BUTTON"));
        browser.waitForPageToLoad(TIMEOUT);
        assertTrue("Message not displayed.", browser
                .isElementPresent(MessageFormat.format(
                        getProperty("CART_NOT_ENOUGH_COPIES_LEFT"), dvd)));
        assertTrue(
                "Order should not be completed.",
                browser
                        .isElementPresent(getProperty("CART_UNABLE_TO_COMPLETE_ORDER_MESSAGE")));
    }

    private void addDVDToCart(String dvdName) {
        assertTrue("User should be logged in.", isLoggedIn(browser));
        browser.click(getProperty("SHOP"));
        browser.waitForPageToLoad(TIMEOUT);
        browser.type(getProperty("SEARCH_FIELD"), dvdName);
        browser.click(getProperty("SEARCH_SUBMIT"));
        browser.waitForPageToLoad(TIMEOUT);
        browser.check(getProperty("SEARCH_RESULT_FIRST_ROW_CHECKBOX"));
        browser.click(getProperty("SEARCH_RESULT_UPDATE_BUTTON"));
        browser.waitForPageToLoad(TIMEOUT);
    }
    
    private BigDecimal parseBalance(String text) throws ParseException {
       String number = text.replaceAll("\\$", "").trim();
       return BigDecimal.valueOf(nf.parse(number).doubleValue());
    }
}
