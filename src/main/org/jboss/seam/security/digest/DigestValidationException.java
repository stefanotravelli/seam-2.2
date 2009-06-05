package org.jboss.seam.security.digest;

/**
 * Thrown when a DigestRequest fails validation.
 * 
 * @author Shane Bryzak
 */
public class DigestValidationException extends Exception
{
   private boolean nonceExpired = false;
   
   public DigestValidationException(String message)
   {
      super(message);
   }
   
   public DigestValidationException(String message, boolean nonceExpired)
   {
      super(message);
      this.nonceExpired = nonceExpired;
   }
   
   public boolean isNonceExpired()
   {
      return nonceExpired;
   }
}
