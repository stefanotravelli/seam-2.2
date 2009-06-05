package org.jboss.seam.annotations.async;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method as an asynchronous method, to be
 * dispatched by the EJB3 TimerService.
 * 
 * If the method returns Timer, the return value
 * will be the Timer used for dispatching the
 * method call. Otherwise, the method should return
 * void.
 * 
 * Note that asynchronous calls are processed in
 * a different EVENT, SESSION and CONVERSATION
 * context to the caller, so the actual recieving
 * object may be a different instance of the 
 * component to the object that was called.
 * 
 * @author Gavin King
 * 
 * @see org.jboss.seam.annotations.async.Expiration
 * @see org.jboss.seam.annotations.async.Duration
 * @see org.jboss.seam.annotations.async.IntervalDuration
 * 
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Asynchronous {}
