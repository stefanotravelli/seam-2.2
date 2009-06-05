package org.jboss.seam.example.restbay.resteasy.statelessejb;

import org.jboss.seam.example.restbay.resteasy.TestResource;

import javax.ejb.Stateless;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author Christian Bauer
 */
@Stateless
public class StatelessEjbTestResourceBean extends TestResource implements StatelessEjbTestResource
{

   @javax.annotation.Resource // EJB injection!
   javax.ejb.SessionContext ejbSessionContext;


   public String echoUri(@Context UriInfo uriInfo)
   {
      assert ejbSessionContext != null; // Ensure this is executed in the EJB container
      setUriInfo(uriInfo);
      return super.echoUri();
   }

   public List<String[]> getCommaSeparated(@Context HttpHeaders headers)
   {
      setHeaders(headers);
      return super.getCommaSeparated();
   }

}