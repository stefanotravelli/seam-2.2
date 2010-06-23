package org.jboss.seam.example.restbay.resteasy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.Identity;
import org.jboss.seam.transaction.Synchronizations;

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
   
   @GET
   @Path("/synchronizationsLookup")
   @Transactional
   public boolean synchronizationsLookup()
   {
      Synchronizations ejb = (Synchronizations) Component.getInstance("org.jboss.seam.transaction.synchronizations", ScopeType.EVENT);
      return ejb.isAwareOfContainerTransactions();
   }
   
   @GET
   @Path("/ejbLookup")
   public boolean ejbLookup()
   {
      TestEjbLocal ejb = (TestEjbLocal) Component.getInstance("securedEjb", ScopeType.EVENT);
      return ejb.foo();
   }
   
}
