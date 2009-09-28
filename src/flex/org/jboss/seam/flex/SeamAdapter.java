package org.jboss.seam.flex;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.core.ConversationPropagation;
import org.jboss.seam.core.Manager;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.servlet.ServletRequestSessionMap;
import org.jboss.seam.web.ServletContexts;

import flex.messaging.FlexContext;
import flex.messaging.services.remoting.adapters.JavaAdapter;
import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.Message;

/**
 * The Seam adaptor should translate seam exceptions and do any other additional 
 * management needed  
 */
public class SeamAdapter
    extends JavaAdapter
{
   public static final String SEAM_ADAPTER_ID = "seam-adapter";
   private static final String CONVERSATION_ID = "conversationId";

   private static final LogProvider log = Logging.getLogProvider(SeamAdapter.class);

   @Override
   public Object invoke(Message message) {
      log.info("SeamAdapter: " + message);

      try {         
         startSeamContexts(message, FlexContext.getHttpRequest());       
         
         Object result = wrapResult(super.invoke(message));       
         
         endSeamContexts(FlexContext.getHttpRequest());
         
         return result;
     } catch (RuntimeException e) {
         // XXX end request properly....
         e.printStackTrace();
         throw e;
      }
   }
   
   
   protected Object wrapResult(Object result)
   {
      AcknowledgeMessage response = new AcknowledgeMessage();
      response.setHeader(CONVERSATION_ID, Manager.instance().getCurrentConversationId());
      response.setBody(result);
      
      return response;
   }


   protected void startSeamContexts(Message message, HttpServletRequest request)
   {
      ServletLifecycle.beginRequest(request);
      ServletContexts.instance().setRequest(request);
      
      Map conversationParameters = conversationMap(message);
      ConversationPropagation.instance().restoreConversationId(conversationParameters);     
      Manager.instance().restoreConversation();
      ServletLifecycle.resumeConversation(request);
      Manager.instance().handleConversationPropagation(conversationParameters);
     
      // Force creation of the session
      if (request.getSession(false) == null) {
         request.getSession(true);
      }
      
   }
   
   protected void endSeamContexts(HttpServletRequest request)
   {
      Manager.instance().endRequest( new ServletRequestSessionMap(request)  );
      ServletLifecycle.endRequest(request);
   }
   
   protected Map conversationMap(Message message) {
      Map result = new HashMap();
      
      result.put(Manager.instance().getConversationIdParameter(), conversationId(message));
      return result;
   }    

   protected String conversationId(Message message) {
      if (message == null) {
         return null;
      }
      
      Object header = message.getHeader(CONVERSATION_ID);
      
      return header == null ? null : header.toString();
   }
}
