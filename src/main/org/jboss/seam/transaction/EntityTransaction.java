package org.jboss.seam.transaction;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.persistence.PersistenceProvider;

/**
 * Support for the JPA EntityTransaction API.
 * 
 * Adapts JPA transaction management to a Seam UserTransaction 
 * interface.For use in non-JTA-capable environments.
 * 
 * @author Gavin King
 * 
 */
@Name("org.jboss.seam.transaction.transaction")
@Scope(ScopeType.EVENT)
@Install(value=false, precedence=FRAMEWORK)
@BypassInterceptors
public class EntityTransaction extends AbstractUserTransaction
{
   private static final LogProvider log = Logging.getLogProvider(EntityTransaction.class);
   
   private ValueExpression<EntityManager> entityManager;
   private EntityManager currentEntityManager;
   
   @Create
   public void validate()
   {
      if (entityManager==null)
      {
         entityManager = Expressions.instance().createValueExpression("#{entityManager}", EntityManager.class);
      }
   }
   
   private javax.persistence.EntityTransaction getDelegate()
   {
      if (currentEntityManager==null)
      {
         //should never occur
         throw new IllegalStateException("entity manager is null");
      }
      return currentEntityManager.getTransaction();
   }

   private void initEntityManager()
   {
      currentEntityManager = entityManager.getValue();
      if (currentEntityManager==null)
      {
         throw new IllegalStateException("entity manager was null: " + entityManager.getExpressionString());
      }
   }

   public void begin() throws NotSupportedException, SystemException
   {
      log.debug("beginning JPA resource-local transaction");
      //TODO: translate exceptions that occur into the correct JTA exception
      assertNotActive();
      initEntityManager();
      try
      {
         getDelegate().begin();
         getSynchronizations().afterTransactionBegin();
      }
      catch (RuntimeException re)
      {
         clearEntityManager();
         throw re;
      }
   }

   public void commit() throws RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SecurityException, IllegalStateException, SystemException
   {
      log.debug("committing JPA resource-local transaction");
      assertActive();
      javax.persistence.EntityTransaction delegate = getDelegate();
      clearEntityManager();
      boolean success = false;
      try
      {
         if ( delegate.getRollbackOnly() )
         {
            delegate.rollback();
            throw new RollbackException();
         }
         else
         {
            getSynchronizations().beforeTransactionCommit();
            delegate.commit();
            success = true;
         }
      }
      finally
      {
         getSynchronizations().afterTransactionCommit(success);
      }
   }

   public void rollback() throws IllegalStateException, SecurityException, SystemException
   {
      log.debug("rolling back JPA resource-local transaction");
      //TODO: translate exceptions that occur into the correct JTA exception
      assertActive();
      javax.persistence.EntityTransaction delegate = getDelegate();
      clearEntityManager();
      try
      {
         delegate.rollback();
      }
      finally
      {
         getSynchronizations().afterTransactionRollback();
      }
   }

   public void setRollbackOnly() throws IllegalStateException, SystemException
   {
      log.debug("marking JPA resource-local transaction for rollback");
      assertActive();
      getDelegate().setRollbackOnly();
   }

   public int getStatus() throws SystemException
   {
      if ( isEntityManagerSet() && getDelegate().getRollbackOnly() )
      {
         return Status.STATUS_MARKED_ROLLBACK;
      }
      else if ( isEntityManagerSet() && getDelegate().isActive() )
      {
         return Status.STATUS_ACTIVE;
      }
      else
      {
         return Status.STATUS_NO_TRANSACTION;
      }
   }

   public void setTransactionTimeout(int timeout) throws SystemException
   {
      throw new UnsupportedOperationException();
   }
   
   private boolean isEntityManagerSet()
   {
      return currentEntityManager!=null;
   }
   
   private void clearEntityManager()
   {
      currentEntityManager = null;
   }

   private void assertActive()
   {
      if ( !isEntityManagerSet() )
      {
         throw new IllegalStateException("transaction is not active");
      }
   }

   private void assertNotActive() throws NotSupportedException
   {
      if ( isEntityManagerSet() )
      {
         throw new NotSupportedException("transaction is already active");
      }
   }
   
   @Override
   public void registerSynchronization(Synchronization sync)
   {
      if ( log.isDebugEnabled() )
      {
         log.debug("registering synchronization: " + sync);
      }
      assertActive();
      //try to register the synchronization directly with the
      //persistence provider, but if this fails, just hold
      //on to it myself
      if ( !PersistenceProvider.instance().registerSynchronization(sync, currentEntityManager) )
      {
         getSynchronizations().registerSynchronization(sync);
      }
   }

   @Override
   public boolean isConversationContextRequired()
   {
      return true;
   }

   public ValueExpression<EntityManager> getEntityManager()
   {
      return entityManager;
   }

   public void setEntityManager(ValueExpression<EntityManager> entityManager)
   {
      this.entityManager = entityManager;
   }
   
   @Override
   public void enlist(EntityManager entityManager)
   {
      //no-op
   }

}
