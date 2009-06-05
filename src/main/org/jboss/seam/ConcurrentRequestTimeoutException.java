/**
 * 
 */
package org.jboss.seam;

public class ConcurrentRequestTimeoutException extends RuntimeException
{

   public ConcurrentRequestTimeoutException()
   {
      super();
   }

   public ConcurrentRequestTimeoutException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public ConcurrentRequestTimeoutException(String message)
   {
      super(message);
   }

   public ConcurrentRequestTimeoutException(Throwable cause)
   {
      super(cause);
   }
   
}