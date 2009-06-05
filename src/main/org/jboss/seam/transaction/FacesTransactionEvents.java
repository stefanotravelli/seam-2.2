package org.jboss.seam.transaction;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.international.StatusMessage.Severity;

/**
 * Produces StatusMessages for JSF in response of certain transaction events.
 * These events can be observed by support classes for other UI frameworks
 * to produce similar messages.
 * 
 * @author Dan Allen
 */
@Name("org.jboss.seam.transaction.facesTransactionEvents")
@Scope(APPLICATION)
@Install(precedence = BUILT_IN, classDependencies = "javax.faces.context.FacesContext")
@BypassInterceptors
@Startup
public class FacesTransactionEvents 
{  
   private boolean transactionFailedMessageEnabled = true;
   
   @Observer(Transaction.TRANSACTION_FAILED)
   public void addTransactionFailedMessage(int status)
   {
      if (transactionFailedMessageEnabled) {
         StatusMessages.instance().addFromResourceBundleOrDefault(
                  getTransactionFailedMessageSeverity(), 
                  getTransactionFailedMessageKey(), 
                  getTransactionFailedMessage());
      }
   }

   public String getTransactionFailedMessage()
   {
      return "Transaction failed";
   }

   public Severity getTransactionFailedMessageSeverity()
   {
      return Severity.WARN;
   }

   public String getTransactionFailedMessageKey()
   {
      return "org.jboss.seam.TransactionFailed";
   }

   public boolean isTransactionFailedMessageEnabled()
   {
      return transactionFailedMessageEnabled;
   }

   public void setTransactionFailedMessageEnabled(boolean enabled)
   {
      this.transactionFailedMessageEnabled = enabled;
   }
}
