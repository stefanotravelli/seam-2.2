/*
 * Copyright 2004-2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.seam.mail.ui.context;

import java.io.IOException;
import java.io.Writer;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;

/**
 * The ResponseWriter for mail objects needs to support unencoded plain text
 * output.
 */
public class MailResponseWriter extends ResponseWriter
{
   
   public static String TEXT_PLAIN_CONTENT_TYPE = "text/plain";
   
   public static String HTML_PLAIN_CONTENT_TYPE = "text/html";
   
   private String contentType;
   
   private ResponseWriter delegate;

   public MailResponseWriter(ResponseWriter responseWriter, String contentType)
            throws FacesException
   {
      this.delegate = responseWriter;
      this.contentType = contentType;
   }

   @Override
   public String getContentType()
   {
      if (contentType == null)
      {
         return delegate.getContentType();
      }
      else
      {
         return contentType;
      }
   }

   @Override
   public String getCharacterEncoding()
   {
      return delegate.getCharacterEncoding();
   }

   @Override
   public void flush() throws IOException
   {
      delegate.flush();
   }

   @Override
   public void startDocument() throws IOException
   {
      if (TEXT_PLAIN_CONTENT_TYPE.equals(getContentType()))
      {
         // Do nothing, can't write tags/attributes to plaintext!
      }
      else
      {
         delegate.startDocument();
      }
   }

   @Override
   public void endDocument() throws IOException
   {
      if (TEXT_PLAIN_CONTENT_TYPE.equals(getContentType()))
      {
         // Do nothing, can't write tags/attributes to plaintext!
      }
      else
      {
         delegate.endDocument();
      }
   }

   @Override
   public void startElement(String name, UIComponent component) throws IOException
   {
      if (TEXT_PLAIN_CONTENT_TYPE.equals(getContentType()))
      {
         // Do nothing, can't write tags/attributes to plaintext!
      }
      else
      {
         delegate.startElement(name, component);
      }
   }

   @Override
   public void endElement(String name) throws IOException
   {
      if (TEXT_PLAIN_CONTENT_TYPE.equals(getContentType()))
      {
         // Do nothing, can't write tags/attributes to plaintext!
      }
      else
      {
         delegate.endElement(name);
      }
   }

   @Override
   public void writeAttribute(String name, Object value, String property)
            throws IOException
   {
      if (TEXT_PLAIN_CONTENT_TYPE.equals(getContentType()))
      {
         // Do nothing, can't write tags/attributes to plaintext!
      }
      else
      {
         delegate.writeAttribute(name, value, property);
      }
      
   }

   @Override
   public void writeURIAttribute(String name, Object value, String componentPropertyName)
            throws IOException
   {
      if (TEXT_PLAIN_CONTENT_TYPE.equals(getContentType()))
      {
         // Do nothing, can't write tags/attributes to plaintext!
      }
      else
      {
         delegate.writeURIAttribute(name, value, componentPropertyName);
      }
     
   }

   @Override
   public void writeComment(Object comment) throws IOException
   {
      if (TEXT_PLAIN_CONTENT_TYPE.equals(getContentType()))
      {
         // Do nothing, can't write comments to plaintext!
      }
      else
      {
         delegate.writeComment(comment);
      }
   }

   @Override
   public void writeText(Object value, String componentPropertyName) throws IOException
   {
      if (TEXT_PLAIN_CONTENT_TYPE.equals(getContentType()))
      {
         if (value == null)
         {
            throw new NullPointerException("Text must not be null.");
         }
         String strValue = value.toString();
         write(strValue);
      }
      else
      {
         delegate.writeText(value, componentPropertyName);
      }
   }

   @Override
   public void writeText(char cbuf[], int off, int len) throws IOException
   {
      if (cbuf == null)
      {
         throw new NullPointerException("cbuf name must not be null");
      }
      if (cbuf.length < off + len)
      {
         throw new IndexOutOfBoundsException((off + len) + " > " + cbuf.length);
      }
      String strValue = new String(cbuf, off, len);
      write(strValue);
   }

   @Override
   public ResponseWriter cloneWithWriter(Writer writer)
   {
      return cloneWithWriter(writer, null);
   }
   
   public MailResponseWriter cloneWithWriter(Writer writer, String contentType)
   {
      MailResponseWriter newWriter = new MailResponseWriter(delegate.cloneWithWriter(writer), contentType);
      return newWriter;
   }

   // Writer methods

   @Override
   public void close() throws IOException
   {
      delegate.close();
   }

   @Override
   public void write(char cbuf[], int off, int len) throws IOException
   {
      String strValue = new String(cbuf, off, len);
      write(strValue);
   }

   @Override
   public void write(int c) throws IOException
   {
      delegate.write(c);
   }

   @Override
   public void write(char cbuf[]) throws IOException
   {
      String strValue = new String(cbuf);
      write(strValue);
   }

   @Override
   public void write(String str) throws IOException
   {
      delegate.write(str);
   }

   @Override
   public void write(String str, int off, int len) throws IOException
   {
      String strValue = str.substring(off, off + len);
      write(strValue);
   }
}
