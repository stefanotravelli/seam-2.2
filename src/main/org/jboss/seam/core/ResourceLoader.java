package org.jboss.seam.core;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.InputStream;
import java.net.URL;
import java.util.MissingResourceException;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Resources;
import org.jboss.seam.util.Strings;

/**
 * Access to application resources and resource bundles.
 * 
 * @author Gavin King
 *
 */
@Scope(ScopeType.STATELESS)
@BypassInterceptors
@Install(precedence=BUILT_IN)
@Name("org.jboss.seam.core.resourceLoader")
public class ResourceLoader
{
   private static final LogProvider log = Logging.getLogProvider(ResourceLoader.class);
   
   private String[] bundleNames = {"messages"};
   
   /**
    * The configurable list of delegate resource bundle names
    * 
    * @return an array of resource bundle names
    */
   public String[] getBundleNames() 
   {
      return bundleNames;
   }
   
   public void setBundleNames(String[] bundleNames) 
   {
      this.bundleNames = bundleNames;
   }
   
   public InputStream getResourceAsStream(String resource)
   {
      return Resources.getResourceAsStream( resource, ServletLifecycle.getCurrentServletContext() );
   }
   
   public URL getResource(String resource) 
   {
      return Resources.getResource( resource, ServletLifecycle.getCurrentServletContext() );
   }
   
   /**
    * Load a resource bundle by name (may be overridden by subclasses
    * who want to use non-standard resource bundle types).
    * 
    * @param bundleName the name of the resource bundle
    * @return an instance of java.util.ResourceBundle
    */
   public java.util.ResourceBundle loadBundle(String bundleName) 
   {
      try
      {
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle( 
               bundleName, 
               Locale.instance(), 
               Thread.currentThread().getContextClassLoader() 
         );
         
         // for getting bundle from page level message properties
         if (bundle == null){
            bundle = java.util.ResourceBundle.getBundle( 
                  bundleName, 
                  Locale.instance(), 
                  ServletLifecycle.getCurrentServletContext().getClass().getClassLoader() 
            );
         }
         log.debug("loaded resource bundle: " + bundleName);
         return bundle;
      }
      catch (MissingResourceException mre)
      {
         log.debug("resource bundle missing: " + bundleName);
         return null;
      }
   }
   
   @Override
   public String toString()
   {
      String concat = bundleNames==null ? "" : Strings.toString( ", ", (Object[]) bundleNames );
      return "ResourceBundle(" + concat + ")";
   }
   
   public static ResourceLoader instance()
   {
      if (!Contexts.isApplicationContextActive()) {
         return new ResourceLoader();
      } else {
         return (ResourceLoader) Component.getInstance(ResourceLoader.class, ScopeType.STATELESS);
      }
   }
   
}
