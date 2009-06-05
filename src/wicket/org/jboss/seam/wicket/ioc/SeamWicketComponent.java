package org.jboss.seam.wicket.ioc;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies that a class should be instrumented to allow for use as a Seam Wicket component
 * @author Clint Popetz
 * 
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface SeamWicketComponent 
{
}


