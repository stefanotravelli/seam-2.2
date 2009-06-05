/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.web;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.IOException;

import javax.faces.component.UIViewRoot;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.FacesLifecycle;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.core.ConversationPropagation;
import org.jboss.seam.core.Manager;
import org.jboss.seam.exception.Exceptions;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.mock.MockApplication;
import org.jboss.seam.mock.MockExternalContext;
import org.jboss.seam.mock.MockFacesContext;
import org.jboss.seam.transaction.Transaction;
import org.jboss.seam.transaction.UserTransaction;

/**
 * Delegate uncaught exceptions to Seam exception handling.
 * As a last line of defence, rollback uncommitted transactions,
 * and clean up Seam contexts.
 * 
 * @author Gavin King
 */
@Scope(APPLICATION)
@Name("org.jboss.seam.web.exceptionFilter")
@Install(precedence = BUILT_IN, classDependencies="javax.faces.context.FacesContext")
@BypassInterceptors
@Filter(within="org.jboss.seam.web.ajax4jsfFilter")
public class ExceptionFilter extends AbstractFilter
{
   
   private static final LogProvider log = Logging.getLogProvider(ExceptionFilter.class);
   
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
         throws IOException, ServletException
   {
      try
      {
         chain.doFilter(request, response);
      }
      catch (Exception e)
      {
         log.debug( "handling uncaught exception", e );
         log.debug( "exception root cause", org.jboss.seam.util.Exceptions.getCause(e) );
         endWebRequestAfterException( (HttpServletRequest) request, (HttpServletResponse) response, e);
      }
   }
   
   protected void endWebRequestAfterException(HttpServletRequest request, HttpServletResponse response, Exception e) 
         throws ServletException, IOException
   {
      
      log.debug("running exception handlers");
      
      //if the event context was cleaned up, fish the conversation id 
      //directly out of the ServletRequest attributes, else get it from
      //the event context
      Manager manager = Contexts.isEventContextActive() ?
              (Manager) Contexts.getEventContext().get(Manager.class) :
              (Manager) request.getAttribute( Seam.getComponentName(Manager.class) );
      String conversationId = manager==null ? null : manager.getCurrentConversationId();
      
      // Ensure that the call in which the exception occurred was cleaned up - it might not be, and there is no harm in trying
      Lifecycle.endRequest();
      
      //the FacesContext is gone - create a fake one for Redirect and HttpError to call
      MockFacesContext facesContext = createFacesContext(request, response);
      facesContext.setCurrent();
      
      //Initialize the temporary context objects
      FacesLifecycle.beginExceptionRecovery( facesContext.getExternalContext() );
      
      //If there is an existing long-running conversation on
      //the failed request, propagate it
      if (conversationId==null)
      {
          Manager.instance().initializeTemporaryConversation();
      }
      else
      {
          ConversationPropagation.instance().setConversationId(conversationId);
          Manager.instance().restoreConversation();
      }
      
      //Now do the exception handling
      try
      {
         rollbackTransactionIfNecessary();
         Exceptions.instance().handle(e);
      }
      catch (ServletException se)
      {
         throw se;
      }
      catch (IOException ioe)
      {
         throw ioe;
      }
      catch (Exception ehe)
      {
         throw new ServletException(ehe);
      }
      finally
      {
         //Finally, clean up the contexts
         try 
         {
            FacesLifecycle.endRequest( facesContext.getExternalContext() );
            facesContext.release();
            log.debug("done running exception handlers");
         }
         catch (Exception ere)
         {
            log.error("could not destroy contexts", ere);
         }
      }
   }
   
   private MockFacesContext createFacesContext(HttpServletRequest request, HttpServletResponse response)
   {
      MockFacesContext mockFacesContext = new MockFacesContext( new MockExternalContext(getServletContext(), request, response), new MockApplication() );
      mockFacesContext.setViewRoot( new UIViewRoot() );
      return mockFacesContext;
   }
   
   protected void rollbackTransactionIfNecessary()
   {
      try 
      {
         UserTransaction transaction = Transaction.instance();
         if ( transaction.isActiveOrMarkedRollback() || transaction.isRolledBack() )
         {
            log.debug("killing transaction");
            transaction.rollback();
         }
      }
      catch (Exception te)
      {
         log.error("could not roll back transaction", te);
      }
   }
}
