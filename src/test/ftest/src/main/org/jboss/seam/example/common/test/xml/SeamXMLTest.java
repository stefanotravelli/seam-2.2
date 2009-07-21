/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.seam.example.common.test.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class encapsulates XML factories to makes XML testing easier. 
 * It accepts and requires this properties:
 * 
 * <ul>
 *    <li><b>selenium.browser.url</b> for URL where server is running</li>
 *    <li><b>example.context.path</b> for context path of example</li> 
 *    <li><b>xml.namespace.aware</b> for namaspace awareness during parse, default <code>true</code></li>
 * </ul>
 * 
 * @author Karel Piwko
 * 
 */
public abstract class SeamXMLTest
{

   private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
   private static XPathFactory xpf = XPathFactory.newInstance();

   protected String BROWSER_URL;
   protected String CONTEXT_PATH;
   protected boolean NAMESPACE_AWARE;
   protected DocumentBuilder db;
   protected XPath xp;

   /**
    * Initializes context path for given test
    * 
    * @param contextPath
    */
   @BeforeClass
   @Parameters( { "selenium.browser.url", "example.context.path", "xml.namespace.aware" })
   public void setParameters(String browserURL, @Optional("") String contextPath, @Optional("true") String namespaceAware)
   {
      BROWSER_URL = browserURL;
      CONTEXT_PATH = contextPath;
      NAMESPACE_AWARE = Boolean.parseBoolean(namespaceAware);
   }

   /**
    * Initializes DocumentBuilder and XPath generic factories. 
    * Sets document builder factory to ignore namespaces.
    * 
    * @throws ParserConfigurationException If document builder factory couldn't
    *            be created
    */
   @BeforeClass
   @Parameters( {"xml.namespace.aware"})
   public void initializeBuilders() throws ParserConfigurationException
   {      
      dbf.setNamespaceAware(NAMESPACE_AWARE);
      db = dbf.newDocumentBuilder();      
      xp = xpf.newXPath();
   }

   /**
    * Evaluates XPath on given part of DOM document
    * 
    * @param root Relative root for XPath evaluation
    * @param xpath XPath expression
    * @return List of node returned by evaluation
    * @throws XPathExpressionException If XPath expression is invalid
    */
   protected List<Node> evaluateXPath(Node root, String xpath) throws XPathExpressionException
   {
      NodeList nl = (NodeList) xp.compile(xpath).evaluate(root, XPathConstants.NODESET);
      List<Node> list = new ArrayList<Node>(nl.getLength());
      for (int i = 0, max = nl.getLength(); i < max; i++)
      {
         list.add(nl.item(i));
      }
      return list;
   }

   /**
    * Evaluates XPath on given part of DOM document and tests all returned
    * results againts condition
    * 
    * @param root Relative root for XPath evaluation
    * @param xpath XPath expression
    * @param conditions Conditions evaluated on each node
    * @return List of node returned by evaluation
    * @throws XPathExpressionException If XPath expression is invalid
    */
   protected boolean evaluateXPathCondition(Node root, String xpath, NodeCondition... conditions) throws XPathExpressionException
   {
      return evaluateCondition(evaluateXPath(root, xpath), conditions);
   }

   protected boolean evaluateCondition(List<Node> list, NodeCondition... conditions)
   {
      for (Node node : list)
      {
         for (NodeCondition condition : conditions)
         {
            if (!condition.match(node))
               return false;
         }
      }
      return true;
   }

}