package org.jboss.seam.example.rss.test.xml;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.jboss.seam.example.common.test.xml.NodeCondition;
import org.jboss.seam.example.common.test.xml.SeamXMLTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class SeamXMLRSSTest extends SeamXMLTest
{
   public static final String HOME_PAGE = "/rss.seam";
   public static final String HOME_PAGE_TITLE = "Title Feed";
   public static final String TITLE_XPATH = "/feed/title";

   public static final String ATOM_NS_URI = "http://www.w3.org/2005/Atom";

   private Document doc;

   @BeforeMethod
   public void setDocument() throws IOException, SAXException
   {
      doc = db.parse(BROWSER_URL + CONTEXT_PATH + HOME_PAGE);     
   }

   /**
    * Verifies that example deploys and has title
    * 
    * @throws XPathExpressionException If XPath expression cannot be compiled or
    *            executed
    */
   @Test
   public void testRSSTitle() throws XPathExpressionException
   {
      List<Node> list = evaluateXPath(doc.getDocumentElement(), TITLE_XPATH);
      assertEquals("There is only on title", 1, list.size());
      assertTrue("Document title equals to \"Title Feed\"", evaluateCondition(list, titleCondition));
   }

   private final NodeCondition titleCondition = new NodeCondition()
   {

      public boolean match(Node node)
      {
         if (node instanceof Element)
         {
            Element element = (Element) node;
            return HOME_PAGE_TITLE.equals(element.getTextContent());
         }
         return false;
      }
   };
}
