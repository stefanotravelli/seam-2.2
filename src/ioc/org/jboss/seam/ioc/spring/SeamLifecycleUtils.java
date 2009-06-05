package org.jboss.seam.ioc.spring;

import org.jboss.seam.ScopeType;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Utility Class for managing a spring Transactional Seam lifecycle
 * 
 * @author Mike Youngstrom
 * 
 */
public class SeamLifecycleUtils
{
   private static final LogProvider log = Logging.getLogProvider(SeamLifecycleUtils.class);

   /**
    * Ensure Seam lifecycle Synchronization happens last
    */
   public static final int SEAM_LIFECYCLE_SYNCHRONIZATION_ORDER = Integer.MAX_VALUE;

   /**
    * Starts a Seam Call if one is not available and if executing within a Spring
    * Transaction.
    */
   public static void beginTransactionalSeamCall()
   {
      if (ScopeType.APPLICATION.isContextActive())
      {
         log.debug("Application available.  Won't start a new call");
         return;
      }
      if (TransactionSynchronizationManager.isSynchronizationActive())
      {
         TransactionSynchronizationManager
                  .registerSynchronization(new SeamLifecycleSynchronization());
         log.debug("Beginning Transactional Seam Call");
         Lifecycle.beginCall();
         return;
      }
      throw new IllegalStateException(
               "Seam application context not available and cannot be started.  "
                        + "Seam Managed Persistence Context not available.  "
                        + "Try placing the spring bean call inside of a spring transaction or try making the spring bean "
                        + "a Seam Component using <seam:component/>.");
   }

   /**
    * Callback for resource cleanup at the end of a transaction where a Seam
    * Lifecycle was created.
    * 
    * @see org.springframework.transaction.jta.JtaTransactionManager
    */
   private static class SeamLifecycleSynchronization extends TransactionSynchronizationAdapter
   {

      @Override
      public int getOrder()
      {
         return SEAM_LIFECYCLE_SYNCHRONIZATION_ORDER;
      }

      @Override
      public void afterCompletion(int status)
      {
         //Close the seam call we started if it is still active.
         if(ScopeType.APPLICATION.isContextActive()) {
            log.debug("Ending Transactional Seam Call");
            Lifecycle.endCall();
         } else {
            log.warn("Spring started a transactional Seam call but somebody else closed before it before the transaction committed.");
         }
      }
   }
}
