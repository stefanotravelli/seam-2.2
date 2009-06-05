package org.jboss.seam.international;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import javax.faces.event.ValueChangeEvent;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.Selector;

/**
 * Selects the current user's time zone, defaulting
 * to the server time zone.
 * 
 * @author Gavin King
 */
@Scope(ScopeType.SESSION)
@Name("org.jboss.seam.international.timeZoneSelector")
@BypassInterceptors
@Install(precedence=BUILT_IN, classDependencies="javax.faces.context.FacesContext")
public class TimeZoneSelector extends Selector
{
   private static final long serialVersionUID = -5013819375360015369L;
   
   private String id;
   
   @Create
   public void initTimeZone()
   {
      String timeZoneId = getCookieValueIfEnabled();
      if (timeZoneId!=null) setTimeZoneId(timeZoneId);
   }
   
   @Override
   protected String getCookieName()
   {
      return "org.jboss.seam.core.TimeZone";
   }
   
   /**
    * Force the resource bundle to reload, using the current locale, 
    * and raise the org.jboss.seam.timeZoneSelected event
    */
   public void select()
   {
      setCookieValueIfEnabled( getTimeZoneId() );

      if ( Events.exists() ) 
      {
          Events.instance().raiseEvent( "org.jboss.seam.timeZoneSelected", getTimeZoneId() );
      }
   }

   public void select(ValueChangeEvent event) 
   {
      selectTimeZone( (String) event.getNewValue() );
   }
   
   public void selectTimeZone(String timeZoneId)
   {
      setTimeZoneId(timeZoneId);
      select();
   }
   
   public void setTimeZone(java.util.TimeZone timeZone)
   {
      setTimeZoneId( timeZone.getID() );
   }

   public void setTimeZoneId(String id)
   {
      setDirty(this.id, id);
      this.id = id;
   }
   
   public String getTimeZoneId()
   {
      return id;
   }

   /**
    * Get the selected timezone
    */
   public java.util.TimeZone getTimeZone() 
   {
      if (id==null)
      {
         return java.util.TimeZone.getDefault();
      }
      else
      {
         return java.util.TimeZone.getTimeZone( getTimeZoneId() );
      }
   }

   public static TimeZoneSelector instance()
   {
      if ( !Contexts.isSessionContextActive() )
      {
         throw new IllegalStateException("No active session context");
      }
      return (TimeZoneSelector) Component.getInstance(TimeZoneSelector.class, ScopeType.SESSION);
   }
   
}
