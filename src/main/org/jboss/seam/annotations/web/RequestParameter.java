//$Id$
package org.jboss.seam.annotations.web;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Injects a request parameter value
 * 
 * @see org.jboss.seam.web.Parameters
 * @author Gavin King
 */
@Target({METHOD, FIELD/*, PARAMETER*/})
@Retention(RUNTIME)
@Documented
public @interface RequestParameter 
{
   /**
    * The name of the request parameter
    */
   String value() default "";
}
