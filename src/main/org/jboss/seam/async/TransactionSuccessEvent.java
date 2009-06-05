package org.jboss.seam.async;

import javax.transaction.Status;
import javax.transaction.Synchronization;

/**
 * An event that is processed when a transaction completes
 * succesfully
 * 
 * @author Gavin King
 *
 */
public class TransactionSuccessEvent extends AsynchronousEvent implements Synchronization
{
   public TransactionSuccessEvent(String type, Object... params)
   {
      super(type, params);
   }
   
   public void afterCompletion(int status)
   {
      if (status==Status.STATUS_COMMITTED)
      {
         execute(null); 
      }
   }
   
   public void beforeCompletion() {}
   
   @Override
   public String toString()
   {
      return "TransactionSuccessEvent(" + getType() + ')';
   }
   
}
