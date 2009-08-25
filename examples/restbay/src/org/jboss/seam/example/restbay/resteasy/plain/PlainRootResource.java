package org.jboss.seam.example.restbay.resteasy.plain;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;

/**
 * @author Christian Bauer
 */
@Path("/")
public class PlainRootResource
{

   @GET
   @Produces("text/plain")
   public String getResource()
   {
      return "Root";
   }
}
