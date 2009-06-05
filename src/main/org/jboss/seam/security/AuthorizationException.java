package org.jboss.seam.security;

import javax.ejb.ApplicationException;

/**
 * Thrown when an authenticated user has insufficient rights to carry out an action.
 * 
 * @author Shane Bryzak
 */
@ApplicationException(rollback=true)
public class AuthorizationException extends RuntimeException
{ 
   public AuthorizationException(String message)
   {
      super(message);
   }
}
