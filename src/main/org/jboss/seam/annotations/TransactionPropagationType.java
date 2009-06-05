package org.jboss.seam.annotations;

/**
 * Transaction propagation strategies for Seam JavaBean 
 * components. Note that unlike EJB3 components, there 
 * are no strategies for suspending transactions.
 * 
 * @author Gavin King
 *
 */
public enum TransactionPropagationType
{
   REQUIRED,
   MANDATORY, 
   SUPPORTS,
   NEVER;
   
   public boolean isNewTransactionRequired(boolean transactionActive)
   {
      switch (this)
      {
         case REQUIRED:
            return !transactionActive;
         case SUPPORTS:
            return false;
         case MANDATORY:
            if ( !transactionActive )
            {
               throw new IllegalStateException("No transaction active on call to MANDATORY method");
            }
            else
            {
               return false;
            }
         case NEVER:
            if ( transactionActive )
            {
               throw new IllegalStateException("Transaction active on call to NEVER method");
            }
            else
            {
               return false;
            }
         default:
            throw new IllegalArgumentException();
      }
   }
   
}
