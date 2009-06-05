package org.jboss.seam.security.management;

/**
 * Thrown when an operation is performed on a non-existent role.
 *  
 * @author Shane Bryzak
 */
public class NoSuchRoleException extends RuntimeException
{
   public NoSuchRoleException(String message)
   {
      super(message);
   }
   
   public NoSuchRoleException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
