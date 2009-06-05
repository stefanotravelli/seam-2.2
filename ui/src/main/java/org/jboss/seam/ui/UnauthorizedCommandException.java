package org.jboss.seam.ui;

import javax.faces.FacesException;

/**
 * An exception is thrown when the authenticity of a JSF command (i.e., form post)
 * that relies on a UIToken cannot be verified.
 * 
 * @author Dan Allen
 */
public class UnauthorizedCommandException extends FacesException
{
   private String viewId;
   
   /**
    * <p>Construct a new exception with no detail message or root cause.</p>
    */
   public UnauthorizedCommandException() {
      super();
   }
   
   /**
    * <p>Construct a new exception with a detail message and the view ID</p>
    */
   public UnauthorizedCommandException(String viewId, String message) {
      super(message);
      this.viewId = viewId;
   }

   /**
    * <p>Returns the view ID to which the authorized command was directed.</p>
    */
   public String getViewId()
   {
      return viewId;
   }

   /**
    * <p>Returns the detail message explaining the reason for the denial.
    * Includes the view ID if specified.</p>
    */
   @Override
   public String getMessage()
   {
      if (viewId != null) {
         return "viewId: " + viewId + " - " + super.getMessage();
      }
      return super.getMessage();
   }
}
