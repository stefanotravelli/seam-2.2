package org.jboss.seam.example.restbay.resteasy.provider;

import org.jboss.seam.ScopeType;
import org.jboss.seam.example.restbay.resteasy.TestComponent;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.In;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Christian Bauer
 */
@Name("interfaceApplicationComponentTestProvider")
@Scope(ScopeType.APPLICATION)
public class InterfaceApplicationComponenTestProviderBean implements InterfaceApplicationComponentTestProvider
{

   @In
   TestComponent testComponent;

   public boolean isWriteable(Class aClass, Type type, Annotation[] annotations, MediaType mediaType)
   {
      return aClass.isArray() && (aClass.getComponentType().equals(Integer.class)) ;
   }

   public long getSize(Object o, Class aClass, Type type, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   public void writeTo(Object o, Class aClass, Type type, Annotation[] annotations, MediaType mediaType,
                       MultivaluedMap httpHeaders, OutputStream outputStream) throws IOException, WebApplicationException
   {
      Integer[] integers = (Integer[])o;

      StringBuilder line = new StringBuilder();

      line.append(testComponent.getTestString()).append(",");

      for (Integer integer : integers)
      {
         line.append(integer).append(",");
      }
      if (line.length()>0) {
         line.deleteCharAt(line.length() - 1);
      }

      outputStream.write(line.toString().getBytes());

   }
}
