package org.jboss.seam.annotations.faces;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allows a Seam component to act as a JSF converter. The
 * annotated class must be a Seam component, and must
 * implement javax.faces.convert.Converter.
 * 
 * @author Gavin King
 *
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Converter
{
   /**
    * The JSF converter id. Default to the component name.
    */
   String id() default "";
   /**
    * If specified, register this component as the default
    * converter for a type.
    */
   Class forClass() default void.class;
}
