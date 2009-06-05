package org.jboss.seam.deployment;

import static org.jboss.seam.util.Strings.split;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.jboss.seam.util.EnumerationEnumeration;

public class SeamDeploymentProperties
{
   
   private ClassLoader classLoader;
   private Enumeration<URL> urlEnum;
   
   public SeamDeploymentProperties(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
   }

   /**
    * The resource bundle used to control Seam deployment
    */
   public static final String RESOURCE_BUNDLE = "META-INF/seam-deployment.properties";
   
   // All resource bundles to use, including legacy names
   private static final String[] RESOURCE_BUNDLES = { RESOURCE_BUNDLE, "META-INF/seam-scanner.properties" };
   
   /**
    * Get a list of possible values for a given key.
    * 
    * First, System properties are tried, followed by the specified resource
    * bundle (first in classpath only).
    * 
    * Colon (:) deliminated lists are split out.
    * 
    */
   public List<String> getPropertyValues(String key)
   {
      List<String>values = new ArrayList<String>();
      addPropertiesFromSystem(key, values);
      addPropertiesFromResourceBundle(key, values);
      return values;
   }
   
   private void addPropertiesFromSystem(String key, List<String> values)
   {
      addProperty(key, System.getProperty(key), values);
   }
   
   private void addPropertiesFromResourceBundle(String key, List<String> values)
   {
      try
      {  
         while ( getResources().hasMoreElements() )
         {
            URL url = getResources().nextElement();
            Properties properties = new Properties();
            InputStream propertyStream = url.openStream();
            try
            {
               properties.load(propertyStream);
               addProperty(key, properties.getProperty(key), values);
            }
            finally
            {
               if (propertyStream != null)
               {
                  propertyStream.close();
               }
            }
         }
      }
      catch (IOException e) 
      {
         // No - op, file is optional
      }
   }
   
   /*
    * Add the property to the set of properties only if it hasn't already been added
    */
   private void addProperty(String key, String value, List<String> values)
   {
      if (value != null)
      {
         String[] properties = split(value, ":");
         for (String property : properties)
         {
            values.add(property);
         }
         
      }
   }
   
   private Enumeration<URL> getResources() throws IOException
   {
      
      if (urlEnum == null)
      {
         Enumeration<URL>[] enumerations = new Enumeration[RESOURCE_BUNDLES.length]; 
         for (int i = 0; i < RESOURCE_BUNDLES.length; i ++)
         {   
            enumerations[i] = classLoader.getResources(RESOURCE_BUNDLES[i]);
         }
         urlEnum = new EnumerationEnumeration<URL>(enumerations);
      }
      return urlEnum;
   }
   
}
