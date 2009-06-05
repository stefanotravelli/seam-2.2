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
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * When no kind of transaction management exists.
 * 
 * @author Mike Youngstrom
 * @author Gavin King
 * 
 */
@Name("org.jboss.seam.transaction.transaction")
@Scope(ScopeType.EVENT)
@Install(value = false, precedence = FRAMEWORK)
@BypassInterceptors
public class NoTransaction extends AbstractUserTransaction
{
   
   public void begin() throws NotSupportedException, SystemException
   {
      throw new UnsupportedOperationException("no transaction");
   }

   public void commit() throws RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SecurityException, IllegalStateException, SystemException
   {
      throw new UnsupportedOperationException("no transaction");
   }

   public int getStatus() throws SystemException
   {
      return Status.STATUS_NO_TRANSACTION;
   }

   public void rollback() throws IllegalStateException, SecurityException, SystemException
   {
      throw new UnsupportedOperationException("no transaction");
   }

   public void setRollbackOnly() throws IllegalStateException, SystemException
   {
      throw new UnsupportedOperationException("no transaction");
   }

   public void setTransactionTimeout(int timeout) throws SystemException
   {
      throw new UnsupportedOperationException("no transaction");
   }
   
   @Override
   public void registerSynchronization(Synchronization sync)
   {
      throw new UnsupportedOperationException("no transaction");
   }
   
   @Override
   public void enlist(EntityManager entityManager) throws SystemException
   {
      //no-op
   }

}
