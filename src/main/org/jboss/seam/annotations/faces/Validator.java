package org.jboss.seam.annotations.faces;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allows a Seam component to act as a JSF validator. The
 * annotated class must be a Seam component, and must
 * implement javax.faces.validator.Validator.
 * 
 * @author Gavin King
 *
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Validator
{
   /**
    * The JSF validator id. Default to the component name.
    */
   String id() default "";
}
