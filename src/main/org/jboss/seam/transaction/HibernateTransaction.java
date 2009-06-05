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

import org.hibernate.Session;
import org.hibernate.Transaction;
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

/**
 * Support for the Hibernate Transaction API.
 * 
 * Adapts Hibernate transaction management to a Seam UserTransaction 
 * interface. For use in non-JTA-capable environments.
 * 
 * @author Gavin King
 * 
 */
@Name("org.jboss.seam.transaction.transaction")
@Scope(ScopeType.EVENT)
@Install(value=false, precedence=FRAMEWORK)
@BypassInterceptors
public class HibernateTransaction extends AbstractUserTransaction
{
   private static final LogProvider log = Logging.getLogProvider(HibernateTransaction.class);

   private ValueExpression<Session> session;
   private Session currentSession;
   private boolean rollbackOnly; //Hibernate Transaction doesn't have a "rollback only" state
   
   @Create
   public void validate()
   {
      if (session==null)
      {
         session = Expressions.instance().createValueExpression("#{session}", Session.class);
      }
   }
   
   private org.hibernate.Transaction getDelegate()
   {
      if (currentSession==null)
      {
         //should never occur
         throw new IllegalStateException("session is null");
      }
      return currentSession.getTransaction();
   }

   private void initSession()
   {
      currentSession = session.getValue();
      if (currentSession==null)
      {
         throw new IllegalStateException("session was null: " + session.getExpressionString());
      }
   }

   public void begin() throws NotSupportedException, SystemException
   {
      log.debug("beginning Hibernate transaction");
      assertNotActive();
      initSession();
      try
      {
         getDelegate().begin();
      }
      catch (RuntimeException re)
      {
         clearSession();
         throw re;
      }
   }

   public void commit() throws RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SecurityException, IllegalStateException, SystemException
   {
      log.debug("committing Hibernate transaction");
      //TODO: translate exceptions that occur into the correct JTA exception
      assertActive();
      Transaction delegate = getDelegate();
      clearSession();
      if (rollbackOnly)
      {
         rollbackOnly = false;
         delegate.rollback();
         throw new RollbackException();
      }
      else
      {
         delegate.commit();
      }
   }

   public void rollback() throws IllegalStateException, SecurityException, SystemException
   {
      log.debug("rolling back Hibernate transaction");
      //TODO: translate exceptions that occur into the correct JTA exception
      assertActive();
      Transaction delegate = getDelegate();
      clearSession();
      rollbackOnly = false;
      delegate.rollback();
   }

   public void setRollbackOnly() throws IllegalStateException, SystemException
   {
      log.debug("marking Hibernate transaction for rollback");
      assertActive();
      rollbackOnly = true;
   }

   public int getStatus() throws SystemException
   {
      if (rollbackOnly)
      {
         return Status.STATUS_MARKED_ROLLBACK;
      }
      else if ( isSessionSet() && getDelegate().isActive() )
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
      assertActive();
      getDelegate().setTimeout(timeout);
   }
   
   private boolean isSessionSet()
   {
      return currentSession!=null;
   }
   
   private void clearSession()
   {
      currentSession = null;
   }

   private void assertActive()
   {
      if ( !isSessionSet() )
      {
         throw new IllegalStateException("transaction is not active");
      }
   }

   private void assertNotActive() throws NotSupportedException
   {
      //TODO: translate exceptions that occur into the correct JTA exception
      if ( isSessionSet() )
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
      getDelegate().registerSynchronization(sync);
   }
   
   @Override
   public void enlist(EntityManager entityManager) throws SystemException
   {
      throw new UnsupportedOperationException("JPA EntityManager should not be used with Hibernate Transaction API");
   }
   
   @Override
   public boolean isConversationContextRequired()
   {
      return true;
   }

   public ValueExpression<Session> getSession()
   {
      return session;
   }

   public void setSession(ValueExpression<Session> entityManager)
   {
      this.session = entityManager;
   }

}
