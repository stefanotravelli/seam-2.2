package org.jboss.seam.annotations.async;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The interval between occurrences of a repeating
 * asynchronous call.
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
public @interface IntervalDuration
{

}
