package org.jboss.seam.servlet;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.core.ConversationPropagation;
import org.jboss.seam.core.Manager;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.web.ServletContexts;

/**
 * Perform work in a full set of Seam contexts
 * 
 * @author Gavin King
 * @author Marek Novotny
 *
 */
public abstract class ContextualHttpServletRequest
{
   private static final LogProvider log = Logging.getLogProvider(ContextualHttpServletRequest.class);

   private final HttpServletRequest request;
   
   private static ThreadLocal<AtomicInteger> count = new ThreadLocal<AtomicInteger>();
     
   public ContextualHttpServletRequest(HttpServletRequest request)
   {
      this.request = request;      
   }
   
   public abstract void process() throws Exception;
   
   public void run() throws ServletException, IOException
   {      
      log.debug("beginning request"); 
           
      // Force creation of the session
      if (request.getSession(false) == null)
      {
         request.getSession(true);
      }
      
      // Begin request and Seam life cycle only if it is not nested
      // ContextualHttpServletRequest
      if (getCounterValue() == 0)
      {         
         ServletLifecycle.beginRequest(request);
         ServletContexts.instance().setRequest(request);         
         restoreConversationId();
         Manager.instance().restoreConversation();
         ServletLifecycle.resumeConversation(request);
         handleConversationPropagation();
      }
      
      try
      {
         incrementCounterValue();
         
         process();                 

         decrementCounterValue();
         
         // End request only if it is not nested ContextualHttpServletRequest
         if (getCounterValue() == 0)
         {
            //TODO: conversation timeout
            Manager.instance().endRequest( new ServletRequestSessionMap(request)  );
            ServletLifecycle.endRequest(request);
         }
      }
      catch (IOException ioe)
      {
         removeCounter();
         Lifecycle.endRequest();
         log.debug("ended request due to exception");
         throw ioe;
      }
      catch (ServletException se)
      {
         removeCounter();
         Lifecycle.endRequest();
         log.debug("ended request due to exception");
         throw se;
      }
      catch (Exception e)
      {
         removeCounter();
         Lifecycle.endRequest();
         log.debug("ended request due to exception");
         throw new ServletException(e);
      }
      finally
      {
         log.debug("ended request");
      }
   }

   protected void handleConversationPropagation()
   {
      Manager.instance().handleConversationPropagation( request.getParameterMap() );
   }

   protected void restoreConversationId()
   {
      ConversationPropagation.instance().restoreConversationId( request.getParameterMap() );
   }
   
   /*
    * Getter for ThreadLocal counter value
    */
   private int getCounterValue()
   {
      AtomicInteger i = count.get();
      if (i == null || i.intValue() < 0)
      {
         log.trace("Getting 0" );
         return 0;
      }
      else
      {
         log.trace("Getting " + i.intValue());
         return i.intValue();
      }
   }
   
   /*
    * Increments ThreadLocal counter value 
    */
   private void incrementCounterValue()
   {
      AtomicInteger i = count.get();
      if (i == null || i.intValue() < 0)
      {
         i = new AtomicInteger(0);
         count.set(i);
      }
      i.incrementAndGet();
      log.trace("Incrementing to " + count.get());
   }
   
   /*
    *  Decrements ThreadLocal counter value
    */
   private void decrementCounterValue()
   {
      AtomicInteger i = count.get();
      if (i == null)
      {
         log.trace("OOps, something removed counter befor end of request!");
         // we should never get here...
         throw new IllegalStateException("Counter for nested ContextualHttpServletRequest was removed before it should be!");
      }
      if (i.intValue() > 0)
      {
         i.decrementAndGet();
         log.trace("Decrementing to " + count.get());
      }
   }
   
   /*
    * Removes ThreadLocal counter
    */
   private void removeCounter() 
   {
      log.trace("Removing ThreadLocal counter");
      count.remove();
   } 
   
}
