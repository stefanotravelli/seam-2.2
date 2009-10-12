package org.jboss.seam.example.restbay.resteasy.provider;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Christian Bauer
 */
@Provider
@Produces("text/csv")
public class PlainTestProvider implements MessageBodyWriter
{

   public boolean isWriteable(Class aClass, Type type, Annotation[] annotations, MediaType mediaType)
   {
      return List.class.isAssignableFrom(aClass);
   }

   public long getSize(Object o, Class aClass, Type type, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   public void writeTo(Object o, Class aClass, Type type, Annotation[] annotations, MediaType mediaType,
                       MultivaluedMap httpHeaders, OutputStream outputStream) throws IOException, WebApplicationException
   {
      List<String[]> lines = (List<String[]>) o;
      StringBuilder csv = new StringBuilder();
      for (String[] line : lines)
      {
         for (String field : line)
         {
            csv.append(field).append(",");
         }
         csv.deleteCharAt(csv.length() - 1);
         csv.append("\r\n");
      }
      outputStream.write(csv.toString().getBytes());

   }
}
