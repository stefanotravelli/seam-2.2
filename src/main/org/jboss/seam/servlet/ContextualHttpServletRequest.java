package org.jboss.seam.servlet;

import java.io.IOException;

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
 *
 */
public abstract class ContextualHttpServletRequest
{
   private static final LogProvider log = Logging.getLogProvider(ContextualHttpServletRequest.class);

   private final HttpServletRequest request;
   
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
      ServletLifecycle.beginRequest(request);
      ServletContexts.instance().setRequest(request);
      restoreConversationId();
      Manager.instance().restoreConversation();
      ServletLifecycle.resumeConversation(request);
      handleConversationPropagation();
      
      
      
      try
      {
         process();
         //TODO: conversation timeout
         Manager.instance().endRequest( new ServletRequestSessionMap(request)  );
         ServletLifecycle.endRequest(request);
      }
      catch (IOException ioe)
      {
         Lifecycle.endRequest();
         log.debug("ended request due to exception");
         throw ioe;
      }
      catch (ServletException se)
      {
         Lifecycle.endRequest();
         log.debug("ended request due to exception");
         throw se;
      }
      catch (Exception e)
      {
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
   
}
