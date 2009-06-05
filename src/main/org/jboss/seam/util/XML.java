package org.jboss.seam.util;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XML
{
    public static Element getRootElement(InputStream stream) throws DocumentException
    {
        try {
            SAXReader saxReader = new SAXReader();
            saxReader.setEntityResolver(new DTDEntityResolver());
            saxReader.setMergeAdjacentText(true);
            return saxReader.read(stream).getRootElement();
        } catch (DocumentException e) {
            Throwable nested = e.getNestedException();
            if (nested!= null) {
                if (nested instanceof FileNotFoundException) {
                    throw new RuntimeException("Can't find schema/DTD reference: " +
                            nested.getMessage(), e);
                } else if (nested instanceof UnknownHostException) {
                    throw new RuntimeException("Cannot connect to host from schema/DTD reference: " + 
                            nested.getMessage() + 
                            " - check that your schema/DTD reference is current", e);
                }
            }
            throw e;
        }
    }

    
   /**
    * Parses an XML document safely, as to not resolve any external DTDs
    */
   public static Element getRootElementSafely(InputStream stream) 
       throws DocumentException
   {
       SAXReader saxReader = new SAXReader();
       saxReader.setEntityResolver(new NullEntityResolver());
       saxReader.setMergeAdjacentText(true);
       return saxReader.read(stream).getRootElement();       
   }
   
   
   public static class NullEntityResolver 
       implements EntityResolver 
   {
       private static final byte[] empty = new byte[0];

       public InputSource resolveEntity(String systemId, String publicId) 
           throws SAXException, 
                  IOException 
       {
           return new InputSource(new ByteArrayInputStream(empty));
       }

   }
}
