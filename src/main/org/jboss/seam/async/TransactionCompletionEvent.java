package org.jboss.seam.async;

import javax.transaction.Synchronization;

/**
 * An event that is processed when a transaction ends
 * 
 * @author Gavin King
 *
 */
public class TransactionCompletionEvent extends AsynchronousEvent implements Synchronization
{
   public TransactionCompletionEvent(String type, Object... params)
   {
      super(type, params);
   }
   
   public void afterCompletion(int status)
   {
      execute(null); 
   }
   
   public void beforeCompletion() {}
   
   @Override
   public String toString()
   {
      return "TransactionCompletionEvent(" + getType() + ')';
   }
   
}
