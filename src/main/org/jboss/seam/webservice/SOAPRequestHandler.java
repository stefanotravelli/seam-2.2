package org.jboss.seam.webservice;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.core.ConversationPropagation;
import org.jboss.seam.core.Manager;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.servlet.ServletRequestSessionMap;
import org.jboss.seam.web.ServletContexts;

/**
 * A SOAP request handler for controlling Seam's lifecycle and managing
 * conversation propagation.
 * 
 * @author Shane Bryzak
 */
public class SOAPRequestHandler implements SOAPHandler
{
   /**
    * The QName of the conversation ID element in the SOAP request header
    */
   public static final QName CIDQN = new QName("http://www.jboss.org/seam/webservice", "conversationId", "seam");
   
   private static final LogProvider log = Logging.getLogProvider(SOAPRequestHandler.class);   
   
   private Set<QName> headers = new HashSet<QName>();
   
   private String handlerName;
   
   /**
    * Handle inbound and outbound messages
    * 
    * @param msgContext The message context
    * @return boolean true if processing should continue
    */
   public boolean handleMessage(MessageContext msgContext)
   {
      Boolean outbound = (Boolean)msgContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
      if (outbound == null)
         throw new IllegalStateException("Cannot obtain required property: " + MessageContext.MESSAGE_OUTBOUND_PROPERTY);

      return outbound ? handleOutbound(msgContext) : handleInbound(msgContext);
   }   

   /**
    * Inbound message handler. Seam contexts should be initialized here, and
    * the conversation ID (if present) is extracted from the request.
    * 
    * @param messageContext The message context
    * @return boolean true if processing should continue
    */
   public boolean handleInbound(MessageContext messageContext)
   {
      try
      {
         HttpServletRequest request = (HttpServletRequest) messageContext.get(MessageContext.SERVLET_REQUEST);      
         ServletLifecycle.beginRequest(request, ServletLifecycle.getServletContext());

         ServletContexts.instance().setRequest(request);
                 
         String conversationId = extractConversationId(messageContext);
         ConversationPropagation.instance().setConversationId( conversationId );
         Manager.instance().restoreConversation();
         
         ServletLifecycle.resumeConversation(request);             
   
         return true;
      }
      catch (SOAPException ex)
      {
         log.error("Error handling inbound SOAP request", ex);
         return false;
      }
   }

   /**
    * Sets the conversation ID in the outbound SOAP message.
    * 
    * @param messageContext The message context
    * @return boolean true if processing should continue
    */
   public boolean handleOutbound(MessageContext messageContext)
   {
      try
      {                
         HttpServletRequest request = (HttpServletRequest) messageContext.get(MessageContext.SERVLET_REQUEST);
         
         String conversationId = Manager.instance().getCurrentConversationId();
         if (conversationId != null)
         {
            SOAPMessageContext smc = (SOAPMessageContext) messageContext;
            
            SOAPHeader header = smc.getMessage().getSOAPHeader();
            if (header != null)
            {
               SOAPElement element = header.addChildElement(CIDQN);
               element.addTextNode(conversationId);
               smc.getMessage().saveChanges();               
            }            
            else
            {
               SOAPEnvelope envelope = smc.getMessage().getSOAPPart().getEnvelope();
               header =  envelope.addHeader();
               SOAPElement element = header.addChildElement(CIDQN);
               element.addTextNode(conversationId);
               smc.getMessage().saveChanges();
            }
         }
         
         Manager.instance().endRequest( new ServletRequestSessionMap(request) );
         
         return true;
      }
      catch (SOAPException ex)
      {
         log.error("Exception processing outbound message", ex);
         return false;
      }
   }
   
   /**
    * Extracts the conversation ID from an incoming SOAP message
    * 
    * @param messageContext
    * @return The conversation ID, or null if there is no conversation ID set
    * @throws SOAPException
    */
   private String extractConversationId(MessageContext messageContext)
      throws SOAPException
   {
      SOAPMessageContext smc = (SOAPMessageContext) messageContext;
      SOAPHeader header = smc.getMessage().getSOAPHeader();
      
      if (header != null)
      {
         Iterator iter = header.getChildElements(CIDQN);
         if (iter.hasNext())
         {
            SOAPElement element = (SOAPElement) iter.next();
            return element.getFirstChild().getNodeValue();
         }
      }
      
      return null;
   }
   
   /**
    * Called just prior to dispatching a message, fault or exception. The 
    * Seam request lifecycle is ended here
    */
   public void close(MessageContext messageContext)
   {     
      Lifecycle.endRequest();
   }
   
   public Set<QName> getHeaders()
   {
      return headers;
   }

   public void setHeaders(Set<QName> headers)
   {
      this.headers = headers;
   }   
   
   public String getHandlerName()
   {
      return handlerName;
   }

   public void setHandlerName(String handlerName)
   {
      this.handlerName = handlerName;
   }

   public boolean handleFault(MessageContext messagecontext)
   {
      return true;
   }
   
   @Override
   public String toString()
   {
      return (handlerName != null ? handlerName : super.toString());
   }      
}
