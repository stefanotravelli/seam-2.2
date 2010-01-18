package org.jboss.seam.international;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

/**
 * Configures the JSF locale support from the Seam container.
 * 
 * <p>
 * This component merely passes on configuration settings to the JSF runtime, so
 * you still have to option of configure the locale support in the JSF
 * configuration file. However, if you enable this component, it will overwrite
 * any settings from that file.
 * </p>
 * 
 * <code>
 *   &lt;i18n:locale-config default-locale="en" supported-locales="en fr de"/&gt;
 * </code>
 * 
 * @author Dan Allen
 */
@Scope(APPLICATION)
@BypassInterceptors
@Startup
@Name("org.jboss.seam.international.localeConfig")
@Install(value = false, precedence = BUILT_IN, classDependencies = "javax.faces.context.FacesContext")
public class LocaleConfig
{
   private String defaultLocale;

   private List<String> supportedLocales;
   
   private static final LogProvider log = Logging.getLogProvider(LocaleConfig.class);

   @Create
   public void initLocaleConfig()
   {
      Application application = getApplication();
      if (application == null)
      {
         return;
      }

      String defaultAsString = getDefaultLocale();
      if (defaultAsString != null)
      {
         application.setDefaultLocale(getLocaleFromString(defaultAsString));
      }

      List<String> supportedAsStrings = getSupportedLocales();
      int numSupported = supportedAsStrings != null ? supportedAsStrings.size() : 0;
      if (numSupported > 0)
      {
         // use set to prevent duplicates, yet retain order just to be nice
         Set<java.util.Locale> locales = new LinkedHashSet<java.util.Locale>(numSupported);
         for (String supportedAsString : supportedAsStrings)
         {
            locales.add(getLocaleFromString(supportedAsString));
         }
         application.setSupportedLocales(locales);
      }
   }

   public String getDefaultLocale()
   {
      return defaultLocale;
   }

   public void setDefaultLocale(String defaultLocale)
   {
      this.defaultLocale = defaultLocale;
      log.debug("Default locale was set to " + this.defaultLocale);
   }

   public List<String> getSupportedLocales()
   {
      return supportedLocales;
   }

   public void setSupportedLocales(List<String> supportedLocales)
   {
      this.supportedLocales = supportedLocales;
      log.debug("Supported locales are " + this.supportedLocales);
   }

   public static LocaleConfig instance()
   {
      return (LocaleConfig) Component.getInstance(LocaleConfig.class, ScopeType.APPLICATION);
   }

   private java.util.Locale getLocaleFromString(String localeString)
   {
      if (localeString == null || localeString.length() < 2)
      {
         throw new IllegalArgumentException("Invalid locale string: " + localeString);
      }

      StringTokenizer tokens = new StringTokenizer(localeString, "-_");
      String language = tokens.hasMoreTokens() ? tokens.nextToken() : null;
      String country = tokens.hasMoreTokens() ? tokens.nextToken() : null;
      String variant = tokens.hasMoreTokens() ? tokens.nextToken() : null;
      if (!Strings.isEmpty(variant))
      {
         return new java.util.Locale(language, country, variant);
      }
      else if (!Strings.isEmpty(country))
      {
         return new java.util.Locale(language, country);
      }
      else
      {
         return new java.util.Locale(language);
      }
   }

   private Application getApplication()
   {
      try
      {
         ApplicationFactory factory = (ApplicationFactory) FactoryFinder
            .getFactory(FactoryFinder.APPLICATION_FACTORY);
         return factory.getApplication();
      }
      catch (IllegalStateException e)
      {
         log.warn("JSF is not properly initialized, see http://jira.jboss.org/jira/browse/JBSEAM-4401");
         // just in case, for units and the like
         // if we can't do it, it just wan't meant to be
         return null;
      }
   }
}
