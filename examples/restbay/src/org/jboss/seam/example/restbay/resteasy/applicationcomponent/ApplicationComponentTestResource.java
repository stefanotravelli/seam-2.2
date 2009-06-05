package org.jboss.seam.example.restbay.resteasy.applicationcomponent;

import org.jboss.resteasy.annotations.Form;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.example.restbay.resteasy.TestResource;
import org.jboss.seam.example.restbay.resteasy.TestComponent;
import org.jboss.seam.example.restbay.resteasy.TestForm;
import org.jboss.seam.example.restbay.resteasy.SubResource;
import org.jboss.seam.ScopeType;
import org.jboss.seam.contexts.Contexts;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Christian Bauer
 */
@Name("applicationComponentTestResource")
@Path("/applicationComponentTest")
@Scope(ScopeType.APPLICATION)
public class ApplicationComponentTestResource extends TestResource
{
   protected final String INSTANCE_CODE_KEY = "applicationComponentTestResource.instanceCode";

   // We want to verify that this is really an application-scoped single instance
   public ApplicationComponentTestResource()
   {
      // Yes, this check is really required, probably because Seam instantiates this twice or something for proxying?! :)
      if (Contexts.getApplicationContext().get(INSTANCE_CODE_KEY) == null)
      {
         Contexts.getApplicationContext().set(INSTANCE_CODE_KEY, hashCode());
      }
   }

   protected void assertSingleton()
   {
      assert Contexts.getApplicationContext().get(INSTANCE_CODE_KEY).equals(hashCode());
   }

   @In
   TestComponent testComponent;

   @GET
   @Path("/echouri")
   @Override
   public String echoUri()
   {
      assertSingleton();
      return super.echoUri();
   }

   @GET
   @Path("/echoquery")
   @Override
   public String echoQueryParam(@QueryParam("bar") String bar)
   {
      assertSingleton();
      return super.echoQueryParam(bar);
   }

   @GET
   @Path("/echoheader")
   @Override
   public String echoHeaderParam(@HeaderParam("bar") String bar)
   {
      assertSingleton();
      return super.echoHeaderParam(bar);
   }

   @GET
   @Path("/echocookie")
   @Override
   public String echoCookieParam(@CookieParam("bar") String bar)
   {
      assertSingleton();
      return super.echoCookieParam(bar);
   }

   @GET
   @Path("/echotwoparams/{1}/{2}")
   @Override
   public String echoTwoParams(@PathParam("1") String one, @PathParam("2") String two)
   {
      assertSingleton();
      return super.echoTwoParams(one, two);
   }

   @GET
   @Path("/echoencoded/{val}")
   @Override
   public String echoEncoded(@PathParam("val") @Encoded String val)
   {
      assertSingleton();
      return super.echoEncoded(val);
   }

   @POST
   @Path("/echoformparams")
   @Consumes("application/x-www-form-urlencoded")
   @Override
   public String echoFormParams(MultivaluedMap<String, String> formMap)
   {
      assertSingleton();
      return super.echoFormParams(formMap);
   }

   @POST
   @Path("/echoformparams2")
   @Override
   public String echoFormParams2(@FormParam("foo") String[] foo)
   {
      assertSingleton();
      return super.echoFormParams2(foo);
   }

   @POST
   @Path("/echoformparams3")
   @Override
   public String echoFormParams3(@Form TestForm form)
   {
      assertSingleton();
      return super.echoFormParams3(form);
   }

   @Path("/foo/bar/{baz}")
   @Override
   public SubResource getBar(@PathParam("baz") String baz)
   {
      assertSingleton();
      return super.getBar(baz);
   }

   @GET
   @Path("/convertDate/{isoDate}")
   @Override
   public long convertPathParam(@PathParam("isoDate") GregorianCalendar isoDate)
   {
      assertSingleton();
      return super.convertPathParam(isoDate);
   }

   @GET
   @Path("/trigger/unsupported")
   @Override
   public String throwException()
   {
      assertSingleton();
      return super.throwException();
   }

   @GET
   @Path("/commaSeparated")
   @Produces("text/csv")
   @Override
   public List<String[]> getCommaSeparated()
   {
      assertSingleton();
      return testComponent.getCommaSeparated();
   }

   @GET
   @Path("/commaSeparatedStrings")
   @Produces("text/plain")
   @Override
   public String[] getCommaSeparatedStrings()
   {
      assertSingleton();
      return super.getCommaSeparatedStrings();
   }

   @GET
   @Path("/commaSeparatedIntegers")
   @Produces("text/plain")
   @Override
   public Integer[] getCommaSeparatedIntegers()
   {
      assertSingleton();
      return super.getCommaSeparatedIntegers();
   }
}