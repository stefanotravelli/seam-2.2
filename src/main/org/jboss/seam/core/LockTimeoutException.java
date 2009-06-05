package org.jboss.seam.core;

/**
 * Thrown by the SynchronizationInterceptor when it fails to get a lock within the allocated time.
 *  
 * @author Shane Bryzak
 */
public class LockTimeoutException extends Exception
{
   public LockTimeoutException()
   {
      super();   
   }
   
   public LockTimeoutException(String message)
   {
      super(message);
   }
   
   public LockTimeoutException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
