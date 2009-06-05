package org.jboss.seam.annotations.exception;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies that an exception should result in a 
 * browser redirect.
 * 
 * @author Gavin King
 *
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
public @interface Redirect
{
   /**
    * The message to be displayed as a FacesMessage, default
    * to using the exception message.
    * 
    * @return a templated message
    */
   String message() default "";
   /**
    * The view to redirect to, default to the current view.
    * 
    * @return a JSF view id
    */
   String viewId();
   /**
    * Should the current long-running conversation end
    * when this exception occurs.
    * 
    * @return true if we should end the conversation
    * @deprecated use @ApplicationException(end=true)
    */
   boolean end() default false;
}
