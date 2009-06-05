package org.jboss.seam.annotations.async;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The stop date of the repeating asynchronous call.
 * 
 * This a parameter level annotation and it is only
 * available for the Quartz timer.
 * 
 * @author Michael Yuan
 *
 */
@Target(PARAMETER)
@Retention(RUNTIME)
@Documented
public @interface FinalExpiration
{
}
