package org.jboss.seam.annotations.async;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The expiration time at which an asynchronous call may
 * first be processed (or the first occurrence of a repeating 
 * asynchronous call).
 * 
 * This annotation occurs on a parameter of type Date
 * of a method marked @Asynchronous.
 * 
 * @author Gavin King
 *
 */
@Target(PARAMETER)
@Retention(RUNTIME)
@Documented
public @interface Expiration
{

}
