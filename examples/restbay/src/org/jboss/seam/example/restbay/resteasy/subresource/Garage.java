package org.jboss.seam.example.restbay.resteasy.subresource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;

@Path("/garage")
@Produces("text/plain")
@Name("garage")
public class Garage
{
   @GET
   public String getInfo() {
      return "garage";
   }
   
   @Path("/1")
   public Vehicle getVehicle() {
      return (Vehicle) Component.getInstance(Vehicle.class);
   }
}
