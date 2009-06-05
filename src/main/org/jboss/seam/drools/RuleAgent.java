package org.jboss.seam.drools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Resources;

/**
 * Manager component for a rule base loaded from a drools RulesAgent
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
public class RuleAgent
{
   private static final LogProvider log = Logging.getLogProvider(RuleAgent.class);
   
   private org.drools.agent.RuleAgent agent;
   private String configurationFile;  

   private String newInstance;
   private String files;
   private String url;
   private String localCacheDir;
   private String poll;
   private String configName;
   
   @Create
   public void createAgent() throws Exception
   {  
      Properties properties = new Properties();
      
      loadFromPath(properties, configurationFile);
      setLocalProperties(properties);
      
      agent = org.drools.agent.RuleAgent.newRuleAgent(properties);    
      log.debug("Creating new rules agent");
   }
   
   protected void setLocalProperties(Properties properties)
   {
      if (newInstance != null) {
         properties.setProperty(org.drools.agent.RuleAgent.NEW_INSTANCE, newInstance);
      }
      if (files != null) {
         properties.setProperty(org.drools.agent.RuleAgent.FILES, files);
      }
      if (url != null) {
         properties.setProperty(org.drools.agent.RuleAgent.URLS, url);
      }
      if (localCacheDir != null) {
         properties.setProperty(org.drools.agent.RuleAgent.LOCAL_URL_CACHE, localCacheDir);
      }
      if (poll != null) {
         properties.setProperty(org.drools.agent.RuleAgent.POLL_INTERVAL, poll);
      }
      if (configName != null) {
         properties.setProperty(org.drools.agent.RuleAgent.CONFIG_NAME, configName);
      }

   }

   protected void loadFromPath(Properties properties, String configurationFile)
      throws IOException
   {
      if (configurationFile != null) {
         InputStream inputStream = Resources.getResourceAsStream(configurationFile, null);
         if (inputStream != null) {
            try {
               properties.load(inputStream);
            } finally {
               inputStream.close();
            }         
         }
      }
   }

   @Unwrap
   public org.drools.RuleBase getRuleBase()
   {
      return agent.getRuleBase();   
   }
   
   public String getNewInstance()
   {
      return newInstance;
   }

   public void setNewInstance(String newInstance)
   {
      this.newInstance = newInstance;
   }

   public String getFiles()
   {
      return files;
   }

   public void setFiles(String files)
   {
      this.files = files;
   }

   public String getUrl()
   {
      return url;
   }

   public void setUrl(String url)
   {
      this.url = url;
   }

   public String getLocalCacheDir()
   {
      return localCacheDir;
   }

   public void setLocalCacheDir(String localCacheDir)
   {
      this.localCacheDir = localCacheDir;
   }

   public String getPoll()
   {
      return poll;
   }

   public void setPoll(String poll)
   {
      this.poll = poll;
   }

   public String getConfigName()
   {
      return configName;
   }

   public void setConfigName(String name)
   {
      this.configName = name;
   }
  
   public String getConfigurationFile()
   {
      return configurationFile;
   }

   public void setConfigurationFile(String brmsConfig)
   {
      this.configurationFile = brmsConfig;
   }
   
}
