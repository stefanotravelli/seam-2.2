package org.jboss.seam.example.restbay.resteasy.subresource;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.jboss.seam.annotations.Name;

@Name("vehicle")
@Produces("text/plain")
public class Vehicle
{
   @GET
   public String getInfo() {
      return "Honda";
   }
}
