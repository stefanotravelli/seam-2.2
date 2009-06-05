package org.jboss.seam.annotations.async;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The duration of the wait before an asynchronous 
 * call is processed (or before the first occurrence
 * of a repeating asynchronous call).
 * 
 * This annotation occurs on a parameter of type long
 * or Long of a method marked @Asynchronous.
 * 
 * @author Gavin King
 *
 */
@Target(PARAMETER)
@Retention(RUNTIME)
@Documented
public @interface Duration
{

}
