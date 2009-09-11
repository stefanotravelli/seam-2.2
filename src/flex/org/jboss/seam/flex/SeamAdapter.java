package org.jboss.seam.flex;


import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import flex.messaging.services.remoting.adapters.JavaAdapter;
import flex.messaging.messages.Message;

/**
 * The Seam adaptor should translate seam exceptions and do any other additional 
 * management needed  
 */
public class SeamAdapter
    extends JavaAdapter
{
   public static final String SEAM_ADAPTER_ID = "seam-adapter";
   
   private static final LogProvider log = Logging.getLogProvider(SeamAdapter.class);

   @Override
   public Object invoke(Message message) {
      log.info("SeamAdapter: " + message);
      return super.invoke(message);      
   }
   
}
