package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method as an observer of an event type or
 * multiple event types.
 * 
 * @author Gavin King
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Observer
{
   /**
    * @return the event type or types to observe
    */
   String[] value() default {};
   /**
    * In case no component instance exists,
    * should a component instance be created to
    * handle the event, or should the event be
    * ignored.
    * 
    * @return true by default
    */
   boolean create() default true;
}
