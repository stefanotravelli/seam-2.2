 package org.jboss.seam.mock;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;

public class MockResponseWriter extends ResponseWriter
{

   private Writer writer; 
    
   public MockResponseWriter(Writer writer)
   {
       this.writer = writer;
   }
   
   public MockResponseWriter()
   {
       this.writer = new StringWriter();
   }
   
   @Override
   public ResponseWriter cloneWithWriter(Writer writer)
   {
      return new MockResponseWriter(writer);
   }

   @Override
   public void endDocument() throws IOException
   {
      // Do nothing
   }

   @Override
   public void endElement(String element) throws IOException
   {
      // Do nothing
   }

   @Override
   public void flush() throws IOException
   {
      writer.flush();

   }

   @Override
   public String getCharacterEncoding()
   {
      return null;
   }

   @Override
   public String getContentType()
   {
      return null;
   }

   @Override
   public void startDocument() throws IOException
   {
      // Do nothing
   }

   @Override
   public void startElement(String element, UIComponent component) throws IOException
   {
      // Do nothing
   }

   @Override
   public void writeAttribute(String attribute, Object object, String string) throws IOException
   {
      // Do nothing
   }

   @Override
   public void writeComment(Object object) throws IOException
   {
      // TODO Do nothing

   }

   @Override
   public void writeText(Object value, String string) throws IOException
   {
       if (value == null)
       {
          throw new NullPointerException("Text must not be null.");
       }
       String strValue = value.toString();
       write(strValue);
   }

   @Override
   public void writeText(char[] chars, int start, int end) throws IOException
   {
       if (chars== null)
       {
          throw new NullPointerException("cbuf name must not be null");
       }
       if (chars.length < start + end)
       {
          throw new IndexOutOfBoundsException((start + end) + " > " + chars.length);
       }
       String strValue = new String(chars, start, end);
       write(strValue);
   }

   @Override
   public void writeURIAttribute(String attribute, Object object, String string) throws IOException
   {
      // Do nothing

   }

   @Override
   public void close() throws IOException
   {
      writer.close();

   }

   @Override
   public void write(char[] chars, int start, int end) throws IOException
   {
       writer.write(chars, start, end);
   }

   @Override
   public void write(String str) throws IOException
   {
       writer.write(str);
   }
   
   @Override
   public void write(int c) throws IOException
   {
      writer.write(c);
   }

   @Override
   public void write(char cbuf[]) throws IOException
   {
      writer.write(cbuf);
   }
   
   @Override
   public void write(String str, int off, int len) throws IOException
   {
      writer.write(str, off, len);
   }
   
   public Writer getWriter()
   {
       return this.writer;
   }
   
}
