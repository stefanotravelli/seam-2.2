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

import java.io.IOException;
import java.util.Properties;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Base class for all Seam based selenium tests.
 * @author jbalunas
 * @author jharting
 *
 */
public abstract class SeamSeleniumTest {

    private static String HOST;
    private static int PORT;
    private static String BROWSER;
    private static String BROWSER_URL;
    private static String SPEED;
    public static String TIMEOUT;
    private static String PROPERTY_FILE;
    protected String CONTEXT_PATH = "";
    private static Properties properties = new Properties();
    private static boolean propertiesLoaded = false;

    protected SeamSelenium browser;

    @BeforeClass
    @Parameters( { "selenium.host", "selenium.server.port", "selenium.browser",
            "selenium.browser.url", "selenium.speed", "selenium.timeout",
            "PROPERTY_FILE", "example.context.path" })
    public void setParameters(String host, String port, String browser,
            String browserUrl, String speed, String timeout, String propertyFile, @Optional("") String contextPath) {
        HOST = host;
        PORT = Integer.parseInt(port);
        BROWSER = browser;
        BROWSER_URL = browserUrl;
        SPEED = speed;
        TIMEOUT = timeout;
        PROPERTY_FILE = propertyFile;
        CONTEXT_PATH = contextPath;
    }

    @BeforeMethod
    public void setUp() {
        browser = startBrowser();
    }

    @AfterMethod
    public void tearDown() {
        stopBrowser();
    }

    public SeamSelenium startBrowser() {
        SeamSelenium newBrowser = new SeamSelenium(HOST, PORT, BROWSER, BROWSER_URL);
        newBrowser.start();
        newBrowser.allowNativeXpath("false");
        newBrowser.setSpeed(SPEED);
        newBrowser.setTimeout(TIMEOUT);
        return newBrowser;
    }
    
    public void stopBrowser() {
        browser.stop();
    }

    public String getProperty(String key) {
        if (!propertiesLoaded) {
            try {
                properties.load(SeamSeleniumTest.class.getResourceAsStream(PROPERTY_FILE));
                propertiesLoaded = true;
            } catch (IOException e) {
                e.printStackTrace();
                fail("Property file not found.");
            }
        }
        return properties.getProperty(key, "Property not found: " + key);
    }
}
