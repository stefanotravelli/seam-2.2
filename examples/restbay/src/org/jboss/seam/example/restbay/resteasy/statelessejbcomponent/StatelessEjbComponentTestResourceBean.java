package org.jboss.seam.example.restbay.resteasy.statelessejbcomponent;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.example.restbay.resteasy.TestComponent;
import org.jboss.seam.example.restbay.resteasy.TestResource;

import javax.ejb.Stateless;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author Christian Bauer
 */
@Name("statelessEjbComponentTestResource")
@Scope(ScopeType.STATELESS)
@Stateless
public class StatelessEjbComponentTestResourceBean extends TestResource implements StatelessEjbComponentTestResource
{

   @javax.annotation.Resource // EJB injection!
   javax.ejb.SessionContext ejbSessionContext;

   @In
   TestComponent testComponent;

   public String echoUri(@Context UriInfo uriInfo)
   {
      assert ejbSessionContext != null; // Ensure this is executed in the EJB container
      setUriInfo(uriInfo);
      return super.echoUri();
   }

   public List<String[]> getCommaSeparated(@Context HttpHeaders headers)
   {
      setHeaders(headers);
      super.getCommaSeparated(); // Ignore return, just to run the assertions
      return testComponent.getCommaSeparated();
   }

}