package org.jboss.seam.annotations.async;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The unix cron expression to specify the frequency 
 * and other conditions for the repeating asynchronous call
 * to be invoked (after the initial delay specified in Expiration
 * or Duration parameters). If this parameter is set, the 
 * IntervalDuration parameter will have no effect.
 * 
 * This annotation occurs on a parameter of type String
 * of a method marked @Asynchronous.
 * 
 * @author Michael Yuan
 *
 */
@Target(PARAMETER)
@Retention(RUNTIME)
@Documented
public @interface IntervalCron {}
