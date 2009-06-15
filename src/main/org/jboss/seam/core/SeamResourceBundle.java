package org.jboss.seam.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.seam.Seam;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.util.EnumerationEnumeration;

/**
 * The Seam resource bundle which searches for resources in delegate resource
 * bundles specified in pages.xml, and a configurable list of delegate resource
 * bundles specified in components.xml.
 * 
 * @see ResourceLoader
 * @author Gavin King
 * 
 */
public class SeamResourceBundle extends java.util.ResourceBundle
{
   private Map<Init,Map<Locale, List<ResourceBundle>>> bundleCache = new ConcurrentHashMap<Init,Map<Locale, List<ResourceBundle>>>();

   private Map<Locale, List<ResourceBundle>> getCachedBundle()
   {
      Init init; 
      if(Contexts.isApplicationContextActive())
      {
         init = (Init)Contexts.getApplicationContext().get(Seam.getComponentName(Init.class));
      }
      else
      {
         //not sure if this is nessesary
         init = (Init)Lifecycle.getApplication().get(Seam.getComponentName(Init.class));
      }
      if(!bundleCache.containsKey(init))
      {
         bundleCache.put(init, new ConcurrentHashMap<Locale, List<ResourceBundle>>());
      }
      return bundleCache.get(init);
   }
   
   /**
    * Get an instance for the current Seam Locale
    * 
    * @see Locale
    * 
    * @return a SeamResourceBundle
    */
   public static java.util.ResourceBundle getBundle()
   {
      return java.util.ResourceBundle.getBundle(SeamResourceBundle.class.getName(),
               org.jboss.seam.core.Locale.instance()); //note: it does not really matter what we pass here
   }

   
   public static java.util.ResourceBundle getBundleNamed(String bundleName)
   {
      return java.util.ResourceBundle.getBundle(bundleName, 
              org.jboss.seam.core.Locale.instance()); 
   }

   
   private List<java.util.ResourceBundle> getBundlesForCurrentLocale()
   {
      Locale instance = org.jboss.seam.core.Locale.instance();
      List<ResourceBundle> bundles = getCachedBundle().get(instance);
      if ( bundles==null )
      {
         bundles = loadBundlesForCurrentLocale();
         getCachedBundle().put(instance, bundles);
      }
      return bundles;

   }

   private List<ResourceBundle> loadBundlesForCurrentLocale()
   {
      List<ResourceBundle> bundles = new ArrayList<ResourceBundle>();
      ResourceLoader resourceLoader = ResourceLoader.instance();
      for (String bundleName : resourceLoader.getBundleNames())
      {
         ResourceBundle bundle = resourceLoader.loadBundle(bundleName);
         if (bundle != null) bundles.add(bundle);
      }
      ResourceBundle bundle = resourceLoader.loadBundle("ValidatorMessages");
      if (bundle != null)
      {
         bundles.add(bundle);
      }
      bundle = resourceLoader.loadBundle("org/hibernate/validator/resources/DefaultValidatorMessages");
      if (bundle != null) bundles.add(bundle);
      bundle = resourceLoader.loadBundle("javax.faces.Messages");
      if (bundle != null) bundles.add(bundle);
      return Collections.unmodifiableList(bundles);
   }

   @Override
   public Enumeration<String> getKeys()
   {
      List<java.util.ResourceBundle> pageBundles = getPageResourceBundles();
      List<ResourceBundle> bundles = getBundlesForCurrentLocale();
      Enumeration<String>[] enumerations = new Enumeration[bundles.size() + pageBundles.size()];

      int i = 0;
      for (java.util.ResourceBundle bundle: pageBundles) {
          enumerations[i++] = bundle.getKeys();
      }
       
      for (ResourceBundle bundle: bundles) {
          enumerations[i++] = bundle.getKeys();
      }

      return new EnumerationEnumeration<String>(enumerations);
   }

   @Override
   protected Object handleGetObject(String key)
   {
      if (!Contexts.isApplicationContextActive())
      {
         return null;
      }
      List<java.util.ResourceBundle> pageBundles = getPageResourceBundles();
      for (java.util.ResourceBundle pageBundle : pageBundles)
      {
         try
         {
            return interpolate(pageBundle.getObject(key));
         }
         catch (MissingResourceException mre) {}
      }

      for (java.util.ResourceBundle littleBundle : getBundlesForCurrentLocale())
      {
         try
         {
            return interpolate( littleBundle.getObject(key) );
         }
         catch (MissingResourceException mre) {}
      }

      return null; // superclass is responsible for throwing MRE
   }

   private Object interpolate(Object message)
   {
      return message!=null && message instanceof String ?
               Interpolator.instance().interpolate( (String) message ) :
               message;
   }

   private List<java.util.ResourceBundle> getPageResourceBundles()
   {
      // TODO: oops! A hard dependency to JSF!
      String viewId = Pages.getCurrentViewId();
      if (viewId != null)
      {
         // we can't cache these bundles, since the viewId
         // may change in the middle of a request
         return Pages.instance().getResourceBundles(viewId);
      }
      else
      {
         return Collections.EMPTY_LIST;
      }
   }
   
   @Override
   public Locale getLocale()
   {
      return org.jboss.seam.core.Locale.instance();
   }
   
}
