package org.jboss.seam.example.restbay.resteasy.statelesscomponent;

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
@Name("statelessComponentTestResource")
@Path("/statelessComponentTest")
@Scope(ScopeType.STATELESS)
public class StatelessComponentTestResource extends TestResource
{

   protected final String INSTANCE_CODE_KEY = "statelessComponentTestResource.instanceCode";

   // We want to verify that this is really stateless (Seam doesn't pool POJO instances, so that should work)
   public StatelessComponentTestResource()
   {
      Integer instanceCode;
      if ((instanceCode = (Integer)Contexts.getApplicationContext().get(INSTANCE_CODE_KEY)) != null)
      {
         assert !instanceCode.equals(hashCode());
      }
      Contexts.getApplicationContext().set(INSTANCE_CODE_KEY, hashCode());
   }

   protected void assertStateless()
   {
      assert !Contexts.getApplicationContext().get(INSTANCE_CODE_KEY).equals(hashCode());
   }

   @In
   TestComponent testComponent;

   @GET
   @Path("/echouri")
   @Override
   public String echoUri()
   {
      assertStateless();
      return super.echoUri();
   }

   @GET
   @Path("/echoquery")
   @Override
   public String echoQueryParam(@QueryParam("bar") String bar)
   {
      assertStateless();
      return super.echoQueryParam(bar);
   }

   @GET
   @Path("/echoheader")
   @Override
   public String echoHeaderParam(@HeaderParam("bar") String bar)
   {
      assertStateless();
      return super.echoHeaderParam(bar);
   }

   @GET
   @Path("/echocookie")
   @Override
   public String echoCookieParam(@CookieParam("bar") String bar)
   {
      assertStateless();
      return super.echoCookieParam(bar);
   }

   @GET
   @Path("/echotwoparams/{1}/{2}")
   @Override
   public String echoTwoParams(@PathParam("1") String one, @PathParam("2") String two)
   {
      assertStateless();
      return super.echoTwoParams(one, two);
   }

   @GET
   @Path("/echoencoded/{val}")
   @Override
   public String echoEncoded(@PathParam("val") @Encoded String val)
   {
      assertStateless();
      return super.echoEncoded(val);
   }

   @POST
   @Path("/echoformparams")
   @Consumes("application/x-www-form-urlencoded")
   @Override
   public String echoFormParams(MultivaluedMap<String, String> formMap)
   {
      assertStateless();
      return super.echoFormParams(formMap);
   }

   @POST
   @Path("/echoformparams2")
   @Override
   public String echoFormParams2(@FormParam("foo") String[] foo)
   {
      assertStateless();
      return super.echoFormParams2(foo);
   }

   @POST
   @Path("/echoformparams3")
   @Override
   public String echoFormParams3(@Form TestForm form)
   {
      assertStateless();
      return super.echoFormParams3(form);
   }

   @Path("/foo/bar/{baz}")
   @Override
   public SubResource getBar(@PathParam("baz") String baz)
   {
      assertStateless();
      return super.getBar(baz);
   }

   @GET
   @Path("/convertDate/{isoDate}")
   @Override
   public long convertPathParam(@PathParam("isoDate") GregorianCalendar isoDate)
   {
      assertStateless();
      return super.convertPathParam(isoDate);
   }

   @GET
   @Path("/trigger/unsupported")
   @Override
   public String throwException()
   {
      assertStateless();
      return super.throwException();
   }

   @GET
   @Path("/commaSeparated")
   @Produces("text/csv")
   @Override
   public List<String[]> getCommaSeparated()
   {
      assertStateless();
      assert headers.getAcceptableMediaTypes().size() == 2;
      assert headers.getAcceptableMediaTypes().get(0).toString().equals("text/plain");
      assert headers.getAcceptableMediaTypes().get(1).toString().equals("text/csv");
      return testComponent.getCommaSeparated();
   }

   @GET
   @Path("/commaSeparatedStrings")
   @Produces("text/plain")
   @Override
   public String[] getCommaSeparatedStrings()
   {
      assertStateless();
      return super.getCommaSeparatedStrings();
   }

   @GET
   @Path("/commaSeparatedIntegers")
   @Produces("text/plain")
   @Override
   public Integer[] getCommaSeparatedIntegers()
   {
      assertStateless();
      return super.getCommaSeparatedIntegers();
   }
}