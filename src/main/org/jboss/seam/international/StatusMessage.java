package org.jboss.seam.international;

import java.io.Serializable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jboss.seam.core.Interpolator;
import org.jboss.seam.core.SeamResourceBundle;
import org.jboss.seam.util.Strings;

/**
 * A status message which can be created in the business layer and displayed
 * in the view layer
 *
 * @author Pete Muir
 *
 */
public class StatusMessage implements Serializable
{
   
   /**
    * The severity of the status message
    *
    */
   public enum Severity
   {
      INFO, 
      WARN, 
      ERROR, 
      FATAL;
   }
   
   private String summaryTemplate;
   private String summary;
   private String detailTemplate;
   private String detail;
   private Severity severity = Severity.INFO;
   
   /**
    * Create a status message, looking up the message in the resource bundle
    * using the provided key. If the message is found, it is used, otherwise, 
    * the defaultMessageTemplate will be used.
    * 
    */
   public StatusMessage(Severity severity, String key, String detailKey, String defaultMessageTemplate, String defaultMessageDetailTemplate)
   {
      this.summaryTemplate = getBundleMessage(key, defaultMessageTemplate);
      this.detailTemplate = getBundleMessage(detailKey, defaultMessageDetailTemplate);
      if ( !Strings.isEmpty(summaryTemplate) )
      {
         this.severity = severity;
      }
   }
   
   public boolean isEmpty()
   {
      return Strings.isEmpty(summary) && Strings.isEmpty(summaryTemplate);
   }
   
   public void interpolate(Object... params)
   {
      if (!Strings.isEmpty(summaryTemplate))
      {
         this.summary = Interpolator.instance().interpolate(summaryTemplate, params);
      }
      if (!Strings.isEmpty(detailTemplate))
      {
         this.detail = Interpolator.instance().interpolate(detailTemplate, params);
      }
   }

   /**
    * Get the message
    * 
    */
   public String getSummary()
   {
      return summary;
   }
   
   /**
    * Get the message severity
    */
   public Severity getSeverity()
   {
      return severity;
   }
   
   public String getDetail()
   {
      return detail;
   }
   
   public static String getBundleMessage(String key, String defaultMessageTemplate)
   {
      String messageTemplate = defaultMessageTemplate;
      if ( key!=null )
      {
         ResourceBundle resourceBundle = SeamResourceBundle.getBundle();
         if ( resourceBundle!=null ) 
         {
            try
            {
               String bundleMessage = resourceBundle.getString(key);
               if (bundleMessage!=null) 
               {
                  messageTemplate = bundleMessage;
               }
            }
            catch (MissingResourceException mre) {} //swallow
         }
      }
      return messageTemplate;
   }
   
   @Override
   public String toString()
   {
      return "[" + severity + "] " + summary + " (" + detail +")";
   }
   
}
