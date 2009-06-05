package org.jboss.seam.international;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletRequest;

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
import org.jboss.seam.util.Strings;
import org.jboss.seam.web.ServletContexts;

/**
 * Selects the current user's locale
 * 
 * @author Gavin King
 */
@Scope(ScopeType.SESSION)
@Name("org.jboss.seam.international.localeSelector")
@BypassInterceptors
@Install(precedence=BUILT_IN, classDependencies="javax.faces.context.FacesContext")
public class LocaleSelector extends Selector
{
   private static final long serialVersionUID = -6087667065688208261L;
   
   private String language;
   private String country;
   private String variant;
   
   @Create
   public void initLocale()
   {
      String localeString = getCookieValueIfEnabled();
      if (localeString!=null) setLocaleString(localeString);
   }
   
   @Override
   protected String getCookieName()
   {
      return "org.jboss.seam.core.Locale";
   }
   
   /**
    * Force the resource bundle to reload, using the current locale,
    * and raise the org.jboss.seam.localeSelected event.
    */
   public void select()
   {
      FacesContext.getCurrentInstance().getViewRoot().setLocale( getLocale() );
      //Contexts.removeFromAllContexts("org.jboss.seam.core.resourceBundle");
      Contexts.removeFromAllContexts("org.jboss.seam.international.messages");
      
      setCookieValueIfEnabled( getLocaleString() );

      if ( Events.exists() ) 
      {
          Events.instance().raiseEvent( "org.jboss.seam.localeSelected", getLocaleString() );
      }
   }
   
   public void select(ValueChangeEvent event) 
   {
      setLocaleString( (String) event.getNewValue() );
      select();
   }

   /**
    * Set the language and force resource bundle reload, useful for quick action links:
    * <tt>&lt;h:commandLink value="DE" action="#{localeSelector.selectLanguage('de')}"/>"/></tt>
    */
   public void selectLanguage(String language) {
      setLanguage(language);
      select();
   }

   public Locale calculateLocale(Locale jsfLocale)
   {
      if ( !Strings.isEmpty(variant) )
      {
         return new java.util.Locale(language, country, variant);
      }
      else if ( !Strings.isEmpty(country) )
      {
         return new java.util.Locale(language, country);
      }
      else if ( !Strings.isEmpty(language) )
      {
         return new java.util.Locale(language);
      }
      else
      {
         return jsfLocale;
      }
   }
   
   public void setLocale(Locale locale)
   {
      language = Strings.nullIfEmpty( locale.getLanguage() );
      country = Strings.nullIfEmpty( locale.getCountry() );
      variant = Strings.nullIfEmpty( locale.getVariant() );
   }
   
   public String getLocaleString()
   {
      return getLocale().toString();
   }
   
   public void setLocaleString(String localeString)
   {
      StringTokenizer tokens = new StringTokenizer(localeString, "-_");
      language = tokens.hasMoreTokens() ? tokens.nextToken() : null;
      country =  tokens.hasMoreTokens() ? tokens.nextToken() : null;
      variant =  tokens.hasMoreTokens() ? tokens.nextToken() : null;
   }
   
   public List<SelectItem> getSupportedLocales()
   {
      List<SelectItem> selectItems = new ArrayList<SelectItem>();
      Iterator<Locale> locales = FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
      while ( locales.hasNext() )
      {
         Locale locale = locales.next();
         if ( !Strings.isEmpty( locale.getLanguage() ) )
         {
            selectItems.add( new SelectItem( locale.toString(), locale.getDisplayName(locale) ) );
         }
      }
      return selectItems;
   }

   /**
    * Get the selected locale
    */
   public Locale getLocale() 
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      if (facesContext!=null)
      {
         //Note: this does a double dispatch back to LocaleSelector.calculateLocale()
         return facesContext.getApplication().getViewHandler().calculateLocale(facesContext);
      }
      
      ServletContexts servletContexts = ServletContexts.getInstance();
      if (servletContexts!=null)
      {
         ServletRequest request = servletContexts.getRequest();
         if (request!=null)
         {
            return calculateLocale( request.getLocale() );
         }
      }

      return calculateLocale( Locale.getDefault() );
   }

   public static LocaleSelector instance()
   {
      if ( !Contexts.isSessionContextActive() )
      {
         throw new IllegalStateException("No active session context");
      }
      return (LocaleSelector) Component.getInstance(LocaleSelector.class, ScopeType.SESSION);
   }

   public String getCountry() 
   {
      if (country==null) return getLocale().getCountry();
      return country;
   }

   public void setCountry(String country) 
   {
      setDirty(this.country, country);
      this.country = country;
   }

   public String getLanguage() 
   {
      if (language==null) return getLocale().getLanguage();
      return language;
   }

   public void setLanguage(String language) 
   {
      setDirty(this.language, language);
      this.language = language;
   }

   public String getVariant() 
   {
      if (variant==null) return getLocale().getVariant();
      return variant;
   }

   public void setVariant(String variant) 
   {
      setDirty(this.variant, variant);
      this.variant = variant;
   }

}
