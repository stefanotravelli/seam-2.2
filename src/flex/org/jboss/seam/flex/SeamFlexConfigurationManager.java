package org.jboss.seam.flex;

import java.io.InputStream;

import javax.servlet.ServletConfig;

import org.jboss.seam.util.Resources;

import flex.messaging.config.FlexConfigurationManager;
import flex.messaging.config.ServletResourceResolver;

public class SeamFlexConfigurationManager 
extends FlexConfigurationManager
{
   
   private static final String USER_CONFIG_FILE = "/WEB-INF/flex/services-config.xml";
   private static final String SEAM_DEFAULT_CONFIG_FILE = "/META-INF/flex/seam-default-services-config.xml";
   
   @Override
   protected void setupConfigurationPathAndResolver(final ServletConfig config)
   {
      configurationPath = USER_CONFIG_FILE;
      
      if (Resources.getResource(USER_CONFIG_FILE, config.getServletContext()) == null) {
         configurationPath = SEAM_DEFAULT_CONFIG_FILE;
      }            
      
      configurationResolver = new ServletResourceResolver(config.getServletContext()) {
         
         @Override
         public InputStream getConfigurationFile(String path) {          
            return Resources.getResourceAsStream(path, config.getServletContext());
         }
      };
   }
}
