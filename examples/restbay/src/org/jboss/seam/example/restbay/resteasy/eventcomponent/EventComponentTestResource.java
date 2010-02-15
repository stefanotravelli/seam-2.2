package org.jboss.seam.example.restbay.resteasy.eventcomponent;

import org.jboss.resteasy.annotations.Form;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.In;
import org.jboss.seam.example.restbay.resteasy.TestResource;
import org.jboss.seam.example.restbay.resteasy.TestComponent;
import org.jboss.seam.example.restbay.resteasy.TestForm;
import org.jboss.seam.example.restbay.resteasy.SubResource;
import org.jboss.seam.transaction.Synchronizations;

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
@Name("eventComponentTestResource")
@Path("/eventComponentTest")
public class EventComponentTestResource extends TestResource
{

   @In
   TestComponent testComponent;

   @GET
   @Path("/echouri")
   @Override
   public String echoUri()
   {
      return super.echoUri();
   }

   @GET
   @Path("/echoquery")
   @Override
   public String echoQueryParam(@QueryParam("bar") String bar)
   {
      return super.echoQueryParam(bar);
   }

   @GET
   @Path("/echoheader")
   @Override
   public String echoHeaderParam(@HeaderParam("bar") String bar)
   {
      return super.echoHeaderParam(bar);
   }

   @GET
   @Path("/echocookie")
   @Override
   public String echoCookieParam(@CookieParam("bar") String bar)
   {
      return super.echoCookieParam(bar);
   }

   @GET
   @Path("/echotwoparams/{1}/{2}")
   @Override
   public String echoTwoParams(@PathParam("1") String one, @PathParam("2") String two)
   {
      return super.echoTwoParams(one, two);
   }

   @GET
   @Path("/echoencoded/{val}")
   @Override
   public String echoEncoded(@PathParam("val") @Encoded String val)
   {
      return super.echoEncoded(val);
   }

   @POST
   @Path("/echoformparams")
   @Consumes("application/x-www-form-urlencoded")
   @Override
   public String echoFormParams(MultivaluedMap<String, String> formMap)
   {
      return super.echoFormParams(formMap);
   }

   @POST
   @Path("/echoformparams2")
   @Override
   public String echoFormParams2(@FormParam("foo") String[] foo)
   {
      return super.echoFormParams2(foo);
   }

   @POST
   @Path("/echoformparams3")
   @Override
   public String echoFormParams3(@Form TestForm form)
   {
      return super.echoFormParams3(form);
   }

   @Path("/foo/bar/{baz}")
   @Override
   public SubResource getBar(@PathParam("baz") String baz)
   {
      return super.getBar(baz);
   }

   @GET
   @Path("/convertDate/{isoDate}")
   @Override
   public long convertPathParam(@PathParam("isoDate") GregorianCalendar isoDate)
   {
      return super.convertPathParam(isoDate);
   }

   @GET
   @Path("/trigger/unsupported")
   @Override
   public String throwException()
   {
      return super.throwException();
   }

   @GET
   @Path("/commaSeparated")
   @Produces("text/csv")
   @Override
   public List<String[]> getCommaSeparated()
   {
      return testComponent.getCommaSeparated();
   }

   @GET
   @Path("/commaSeparatedStrings")
   @Produces("text/plain")
   @Override
   public String[] getCommaSeparatedStrings()
   {
      return super.getCommaSeparatedStrings();
   }

   @GET
   @Path("/commaSeparatedIntegers")
   @Produces("text/plain")
   @Override
   public Integer[] getCommaSeparatedIntegers()
   {
      return super.getCommaSeparatedIntegers();
   }
   
   @GET
   @Path("/synchronizationsLookup")
   public boolean synchronizationsLookup()
   {
      Synchronizations ejb = (Synchronizations) Component.getInstance("org.jboss.seam.transaction.synchronizations", ScopeType.EVENT);
      return ejb.isAwareOfContainerTransactions();
   }
}
