package org.jboss.seam.example.restbay;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

/**
 * @author Christian Bauer
 */
@Name("auctionService")
@Path("/auction")
public class AuctionService
{

   @Context
   private UriInfo uriInfo;

   private HttpHeaders headers;

   @Context
   public void setHeaders(HttpHeaders headers) {
      this.headers = headers;
   }

   @In
   EntityManager entityManager;

   @GET
   @Produces("text/plain")
   public String getAuctions()
   {

      URI builtURI = uriInfo.getAbsolutePathBuilder().path("3").build();
      // We can't test for /<context> prefix here, as we don't have a context in unit tests but we
      // have it when the application is deployed... so use endsWith()
      assert builtURI.getPath().endsWith("/seam/resource/restv1/auction/3");

      // This is supposed to test field and setter injection for @Context
      assert uriInfo.getPath().equals("/auction");
      assert headers.getAcceptableMediaTypes().size() == 1;
      assert headers.getAcceptableMediaTypes().get(0).toString().equals("text/plain");

      List<Object[]> auctions =
            entityManager.createQuery("select a.auctionId, a.title from Auction a order by a.auctionId asc").getResultList();
      StringBuilder s = new StringBuilder();
      for (Object[] auction : auctions)
      {
         s.append(auction[0]).append(",").append(auction[1]).append("\n");
      }

      return s.toString();
   }

   @GET
   @Path("/{auctionId}")
   @Produces("text/plain")
   public String getAuction(@Context HttpHeaders httpHeaders, @PathParam("auctionId") int auctionId)
   {
      assert httpHeaders.getAcceptableMediaTypes().size() == 1;
      assert httpHeaders.getAcceptableMediaTypes().get(0).toString().equals("text/plain");
      
      return entityManager.find(Auction.class, auctionId).getTitle();
   }

}