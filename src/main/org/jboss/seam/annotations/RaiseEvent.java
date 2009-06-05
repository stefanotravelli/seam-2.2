/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Causes an event to be raised after the method returns
 * a non-null result without exception.
 * 
 * @author Gavin King
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface RaiseEvent 
{
   
   /**
    * The event name, defaults to the name
    * of the method.
    * 
    * @return the event name
    */
   String[] value() default {};
   
   //TODO: String[] ifOutcome() default {};
   
}
