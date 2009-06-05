package org.jboss.seam.faces;

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.core.AbstractMutable;

/**
 * Support for selector objects which remember their selection as a cookie
 * 
 * @author Gavin King
 */
public abstract class Selector extends AbstractMutable implements Serializable
{
   public static final int DEFAULT_MAX_AGE = 31536000; // 1 year in seconds
   private boolean cookieEnabled;
   private int cookieMaxAge = DEFAULT_MAX_AGE;
   private String cookiePath= "/";
   
   /**
    * Is the cookie enabled?
    * @return false by default
    */
   public boolean isCookieEnabled()
   {
      return cookieEnabled;
   }
   public void setCookieEnabled(boolean cookieEnabled)
   {
      setDirty(this.cookieEnabled, cookieEnabled);
      this.cookieEnabled = cookieEnabled;
   }
   /**
    * The max age of the cookie
    * @return 1 year by default
    */
   public int getCookieMaxAge()
   {
      return cookieMaxAge;
   }
   public void setCookieMaxAge(int cookieMaxAge)
   {
      this.cookieMaxAge = cookieMaxAge;
   }
   
   public String getCookiePath()
   {
      return cookiePath;
   }
   
   public void setCookiePath(String cookiePath)
   {
      this.cookiePath = cookiePath;
   }
   
   /**
    * Override to define the cookie name
    */
   protected abstract String getCookieName();
   
   /**
    * Get the value of the cookie
    */
   protected String getCookieValueIfEnabled()
   {
      return isCookieEnabled() ?
         getCookieValue() : null;
   }
   
   protected Cookie getCookie()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      if (ctx != null)
      {
          return (Cookie) ctx.getExternalContext().getRequestCookieMap()
            .get( getCookieName() );
      }
      else
      {
         return null;
      }
   }
   
   protected String getCookieValue()
   {
      Cookie cookie = getCookie();
      return cookie==null ? null : cookie.getValue();
   }
   
   protected void clearCookieValue()
   {
      Cookie cookie = getCookie();
      if ( cookie!=null )
      {
         HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();         
         cookie.setValue(null);
         cookie.setPath(cookiePath);
         cookie.setMaxAge(0);
         response.addCookie(cookie);
      }
   }
   
   /**
    * Set the cookie
    */
   protected void setCookieValueIfEnabled(String value)
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      
      if ( isCookieEnabled() && ctx != null)
      {
         HttpServletResponse response = (HttpServletResponse) ctx.getExternalContext().getResponse();
         Cookie cookie = new Cookie( getCookieName(), value );
         cookie.setMaxAge( getCookieMaxAge() );
         cookie.setPath(cookiePath);
         response.addCookie(cookie);
      }
   }
}
