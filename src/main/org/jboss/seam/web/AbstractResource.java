package org.jboss.seam.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Superclass of Seam components that serve up
 * "resources" to the client via the Seam
 * resource servlet. Note that since a filter is
 * potentially called outside of a set of Seam
 * contexts, it is not a true Seam component.
 * However, we are able to reuse the functionality
 * for component scanning, installation and
 * configuration for filters. All resources
 * must extend this class.
 *
 * @author Shane Bryzak
 *
 */
public abstract class AbstractResource
{
   private ServletContext context;

   protected ServletContext getServletContext()
   {
      return context;
   }

   public void setServletContext(ServletContext context)
   {
      this.context = context;
   }

   public abstract void getResource(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException;

   public abstract String getResourcePath();

   protected OutputStream selectOutputStream(HttpServletRequest request, HttpServletResponse response)
         throws IOException
   {

      String acceptEncoding = request.getHeader("Accept-Encoding");
      String mimeType = response.getContentType();

      if (isGzipEnabled()
            && acceptEncoding != null
            && acceptEncoding.length() > 0
            && acceptEncoding.indexOf("gzip") > -1
            && isCompressedMimeType(mimeType))
      {
         return new GZIPResponseStream(response);
      }
      else
      {
         return response.getOutputStream();
      }
   }

   protected boolean isCompressedMimeType(String mimeType)
   {
      return mimeType.matches("text/.+");
   }

   protected boolean isGzipEnabled()
   {
      return true;
   }

   /*
    * Copyright 2004-2008 the original author or authors.
    *
    * Licensed under the Apache License, Version 2.0 (the "License");
    * you may not use this file except in compliance with the License.
    * You may obtain a copy of the License at
    *
    *      http://www.apache.org/licenses/LICENSE-2.0
    *
    * Unless required by applicable law or agreed to in writing, software
    * distributed under the License is distributed on an "AS IS" BASIS,
    * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    * See the License for the specific language governing permissions and
    * limitations under the License.
    *
    * @See org/springframework/js/resource/ResourceServlet.java
    */
   private class GZIPResponseStream extends ServletOutputStream
   {

      private ByteArrayOutputStream byteStream = null;

      private GZIPOutputStream gzipStream = null;

      private boolean closed = false;

      private HttpServletResponse response = null;

      private ServletOutputStream servletStream = null;

      public GZIPResponseStream(HttpServletResponse response) throws IOException
      {
         super();
         closed = false;
         this.response = response;
         this.servletStream = response.getOutputStream();
         byteStream = new ByteArrayOutputStream();
         gzipStream = new GZIPOutputStream(byteStream);
      }

      @Override
      public void close() throws IOException
      {
         if (closed)
         {
            throw new IOException("This output stream has already been closed");
         }
         gzipStream.finish();

         byte[] bytes = byteStream.toByteArray();

         response.setContentLength(bytes.length);
         response.addHeader("Content-Encoding", "gzip");
         servletStream.write(bytes);
         servletStream.flush();
         servletStream.close();
         closed = true;
      }

      @Override
      public void flush() throws IOException
      {
         if (closed)
         {
            throw new IOException("Cannot flush a closed output stream");
         }
         gzipStream.flush();
      }

      @Override
      public void write(int b) throws IOException
      {
         if (closed)
         {
            throw new IOException("Cannot write to a closed output stream");
         }
         gzipStream.write((byte) b);
      }

      @Override
      public void write(byte b[]) throws IOException
      {
         write(b, 0, b.length);
      }

      @Override
      public void write(byte b[], int off, int len) throws IOException
      {
         if (closed)
         {
            throw new IOException("Cannot write to a closed output stream");
         }
         gzipStream.write(b, off, len);
      }

      @SuppressWarnings("unused")
      public boolean closed()
      {
         return (this.closed);
      }

      @SuppressWarnings("unused")
      public void reset()
      {
         // noop
      }
   }
}
