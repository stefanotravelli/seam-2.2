package org.jboss.seam.example.restbay.resteasy.statelessejb;

import org.jboss.resteasy.annotations.Form;
import org.jboss.seam.example.restbay.resteasy.TestForm;
import org.jboss.seam.example.restbay.resteasy.SubResource;

import javax.ejb.Local;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Encoded;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Christian Bauer
 */
@Local
@Path("/statelessEjbTest")
public interface StatelessEjbTestResource
{

   // TODO: RESTEasy can not inject setter/fields on plain EJBs, see https://jira.jboss.org/jira/browse/RESTEASY-151
   // That's why we have to do it through parameter injection below
   @Context
   public void setUriInfo(UriInfo uriInfo);

   @Context
   public void setHeaders(HttpHeaders headers);


   @GET
   @Path("/echouri")
   String echoUri(@Context UriInfo uriInfo); // TODO

   @GET
   @Path("/echoquery")
   String echoQueryParam(@QueryParam("bar") String bar);

   @GET
   @Path("/echoheader")
   String echoHeaderParam(@HeaderParam("bar") String bar);

   @GET
   @Path("/echocookie")
   String echoCookieParam(@CookieParam("bar") String bar);

   @GET
   @Path("/echotwoparams/{1}/{2}")
   String echoTwoParams(@PathParam("1") String one, @PathParam("2") String two);

   @GET
   @Path("/echoencoded/{val}")
   String echoEncoded(@PathParam("val") @Encoded String val);

   @POST
   @Path("/echoformparams")
   @Consumes("application/x-www-form-urlencoded")
   String echoFormParams(MultivaluedMap<String, String> formMap);

   @POST
   @Path("/echoformparams2")
   String echoFormParams2(@FormParam("foo") String[] foo);

   @POST
   @Path("/echoformparams3")
   String echoFormParams3(@Form TestForm form);

   @Path("/foo/bar/{baz}")
   SubResource getBar(@PathParam("baz") String baz);

   @GET
   @Path("/convertDate/{isoDate}")
   long convertPathParam(@PathParam("isoDate") GregorianCalendar isoDate);

   @GET
   @Path("/trigger/unsupported")
   String throwException();

   @GET
   @Path("/commaSeparated")
   @Produces("text/csv")
   List<String[]> getCommaSeparated(@Context HttpHeaders headers); // TODO

   @GET
   @Path("/commaSeparatedStrings")
   @Produces("text/plain")
   String[] getCommaSeparatedStrings();

   @GET
   @Path("/commaSeparatedIntegers")
   @Produces("text/plain")
   Integer[] getCommaSeparatedIntegers();

}
