package org.jboss.seam.wicket;

import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;

/**
 * This subclass of WebSession exists so that calls to invalidate the wicket session
 * result in a delegation to the seam session invalidation code.
 * @author cpopetz
 *
 */
public class SeamWebSession extends WebSession
{
   public
   SeamWebSession(Request request)
   {
      super(request);
   }

   @Override
   public void invalidate() 
   {
      org.jboss.seam.web.Session.getInstance().invalidate();
   }

   @Override
   public void invalidateNow() 
   {
      // sorry, can't support this with Seam
      org.jboss.seam.web.Session.getInstance().invalidate();
   }
}