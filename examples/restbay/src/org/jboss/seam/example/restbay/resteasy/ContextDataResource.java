package org.jboss.seam.example.restbay.resteasy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

@Path("/contextData")
@Produces("text/plain")
public class ContextDataResource
{
   @GET
   @Path("/providers")
   public boolean providersAvailable()
   {
      return ResteasyProviderFactory.getContextData(Providers.class) != null;
   }
   
   @GET
   @Path("/registry")
   public boolean registryAvailable()
   {
      return ResteasyProviderFactory.getContextData(Providers.class) != null;
   }
   
   @GET
   @Path("/dispatcher")
   public boolean dispatcherAvailable()
   {
      return ResteasyProviderFactory.getContextData(Providers.class) != null;
   }
   
}
