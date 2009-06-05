//$Id$
package org.jboss.seam;

/**
 * Thrown when a null value is encountered during bijection
 * of an attribute with required=true.
 * 
 * @author Gavin King
 *
 */
public class RequiredException extends RuntimeException
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -5437284703541833879L;

   public RequiredException(String message)
   {
      super(message);
   }

}
