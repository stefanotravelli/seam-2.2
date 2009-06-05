package org.jboss.seam.ui;

import javax.faces.context.FacesContext;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.Selector;
import org.jboss.seam.util.RandomStringUtils;

/**
 * <p>A selector which manages the cookie that gives the browser a
 * unique identifier. This value is shared only between the browser
 * and the server, thus allowing the server to determine if two
 * distinct requests were made by the same source.</p>
 * 
 * <p>The identifier is stored in a cookie named <code>javax.faces.ClientToken</code>.</p>
 * 
 * @author Dan Allen
 */
@Name("org.jboss.seam.ui.clientUidSelector")
public class ClientUidSelector extends Selector
{
   private String clientUid;

   @Create
   public void onCreate()
   {
      setCookiePath(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath());
      setCookieMaxAge(-1);
      setCookieEnabled(true);
      clientUid = getCookieValue();
   }

   public void seed()
   {
      if (!isSet()) {
         clientUid = RandomStringUtils.randomAscii(50);
         setCookieValueIfEnabled(clientUid);
      }
   }

   public boolean isSet()
   {
      return clientUid != null;
   }

   public String getClientUid()
   {
      return clientUid;
   }

   @Override
   protected String getCookieName()
   {
      return "javax.faces.ClientToken";
   }

}
