package org.jboss.seam.example.restbay.resteasy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.Identity;

/**
 * 
 * @author Jozef Hartinger
 *
 */

@Path("/secured")
@Name("securedResource")
@Produces("text/plain")
public class SecuredResource
{

   @In
   private Identity identity;

   @GET
   public String getHello()
   {
      return "Hello world!";
   }

   @GET
   @Path("/admin")
   public boolean isAdmin() {
      return identity.hasRole("admin"); 
   }
   
   @GET
   @Path("/restrictedAdmin")
   @Restrict("#{s:hasRole('admin')}")
   public boolean restrictedIsAdmin() {
      return identity.hasRole("admin"); 
   }
   
}
