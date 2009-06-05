package org.jboss.seam.annotations.exception;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.servlet.http.HttpServletResponse;

/**
 * Specifies that an exception results in a HTTP error.
 * 
 * @author Gavin King
 *
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
public @interface HttpError
{
   /**
    * The message to be sent in the HTTP error, default
    * to using the exception message.
    * 
    * @return a templated message
    */
   String message() default "";
   
   /**
    * The HTTP error code, default to 500.
    * 
    * @return an error code
    */
   int errorCode() default HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
   /**
    * Should the current long-running conversation end
    * when this exception occurs.
    * 
    * @return true if we should end the conversation
    * @deprecated use @ApplicationException(end=true)
    */
   boolean end() default false;
}
