/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.bpm;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import javax.naming.NamingException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.transaction.Transaction;
import org.jboss.seam.transaction.UserTransaction;
import org.jbpm.JbpmContext;
import org.jbpm.persistence.db.DbPersistenceServiceFactory;
import org.jbpm.svc.Services;

/**
 * Manages a reference to a JbpmContext.
 *
 * @author <a href="mailto:steve@hibernate.org">Steve Ebersole</a>
 * @author Gavin King
 */
@Scope(ScopeType.EVENT)
@Name("org.jboss.seam.bpm.jbpmContext")
@BypassInterceptors
@Install(precedence=BUILT_IN, dependencies="org.jboss.seam.bpm.jbpm")
public class ManagedJbpmContext implements Synchronization
{
   private static final LogProvider log = Logging.getLogProvider(ManagedJbpmContext.class);

   private JbpmContext jbpmContext;
   private boolean synchronizationRegistered;

   @Create
   public void create() throws NamingException, RollbackException, SystemException
   {
      jbpmContext = Jbpm.instance().getJbpmConfiguration().createJbpmContext();
      assertNoTransactionManagement();
      log.debug( "created seam managed jBPM context");
   }

   private void assertNoTransactionManagement()
   {
      DbPersistenceServiceFactory dpsf = (DbPersistenceServiceFactory) jbpmContext.getJbpmConfiguration()
            .getServiceFactory(Services.SERVICENAME_PERSISTENCE);
      if ( dpsf.isTransactionEnabled() )
      {
         throw new IllegalStateException("jBPM transaction management is enabled, disable in jbpm.cfg.xml");
      }
   }

   @Unwrap
   public JbpmContext getJbpmContext() throws NamingException, RollbackException, SystemException
   {
      joinTransaction();
      return jbpmContext;
   }

   private void joinTransaction() throws SystemException
   {
      UserTransaction transaction = Transaction.instance();
      
      if ( !transaction.isActiveOrMarkedRollback() )
      {
         throw new IllegalStateException("JbpmContext may only be used inside a transaction");
      }
      
      if ( !synchronizationRegistered && !Lifecycle.isDestroying() && transaction.isActive() )
      {
         jbpmContext.getSession().isOpen();
         try //TODO: what we really want here is if (!cmt)
         {
            transaction.registerSynchronization(this);
         }
         catch (UnsupportedOperationException uoe)
         {
            jbpmContext.getSession().getTransaction().registerSynchronization(this);
         }
         synchronizationRegistered = true;
      }
   }
   
   public void beforeCompletion()
   {
      log.debug( "flushing seam managed jBPM context" );
      /*org.jbpm.graph.exe.ProcessInstance processInstance = ProcessInstance.instance();
      if (processInstance!=null)
      {
         jbpmContext.save(processInstance);
      }*/
      if ( Contexts.isBusinessProcessContextActive() )
      {
         //in requests that come through SeamPhaseListener,
         //transactions are committed before the contexts are
         //destroyed, flush here:
         Contexts.getBusinessProcessContext().flush();
      }
      jbpmContext.getSession().flush();
      log.debug( "done flushing seam managed jBPM context" );
   }
   
   public void afterCompletion(int status) 
   {
      synchronizationRegistered = false;
      if ( !Contexts.isEventContextActive() )
      {
         //in calls to MDBs and remote calls to SBs, the 
         //transaction doesn't commit until after contexts
         //are destroyed, so wait until the transaction
         //completes before closing the session
         //on the other hand, if we still have an active
         //event context, leave it open
         closeContext();
      }
   }
   
   @Destroy
   public void destroy()
   {
      if ( !synchronizationRegistered )
      {
         //in requests that come through SeamPhaseListener,
         //there can be multiple transactions per request,
         //but they are all completed by the time contexts
         //are dstroyed
         //so wait until the end of the request to close
         //the session
         //on the other hand, if we are still waiting for
         //the transaction to commit, leave it open
         closeContext();
      }
   }

   private void closeContext()
   {
      log.debug( "destroying seam managed jBPM context" );
      jbpmContext.close();
      log.debug( "done destroying seam managed jBPM context" );
   }
      
   public static JbpmContext instance()
   {
      if ( !Contexts.isEventContextActive() )
      {
         throw new IllegalStateException("no active event context");
      }
      return (JbpmContext) Component.getInstance(ManagedJbpmContext.class, ScopeType.EVENT);
   }

}
