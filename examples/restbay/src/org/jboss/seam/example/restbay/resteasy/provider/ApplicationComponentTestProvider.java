package org.jboss.seam.example.restbay.resteasy.provider;

import org.jboss.seam.example.restbay.resteasy.TestComponent;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.ScopeType;

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

/**
 * @author Christian Bauer
 */
@Name("applicationComponentTestProvider")
@Scope(ScopeType.APPLICATION)
@Provider
@Produces("text/plain")
public class ApplicationComponentTestProvider implements MessageBodyWriter
{

   // TODO: Retracted support for Seam component providers, injection shouldn't happen, see https://jira.jboss.org/jira/browse/JBSEAM-4247
   @In
   TestComponent testComponent = new TestComponent();

   public boolean isWriteable(Class aClass, Type type, Annotation[] annotations, MediaType mediaType)
   {
      return aClass.isArray() && (aClass.getComponentType().equals(String.class)) ;
   }

   public long getSize(Object o, Class aClass, Type type, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   public void writeTo(Object o, Class aClass, Type type, Annotation[] annotations, MediaType mediaType,
                       MultivaluedMap httpHeaders, OutputStream outputStream) throws IOException, WebApplicationException
   {
      String[] strings = (String[])o;

      StringBuilder line = new StringBuilder();

      line.append(testComponent.getTestString()).append(",");

      for (String string : strings)
      {
         line.append(string).append(",");
      }
      if (line.length()>0) {
         line.deleteCharAt(line.length() - 1);
      }

      outputStream.write(line.toString().getBytes());

   }
}
