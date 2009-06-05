package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies that a stateful component has
 * multiple concurrent clients, and so access
 * to the component must be synchronized. This
 * annotation is not required for session scoped
 * components, which are synchronized by default.
 *
 * @author Gavin King
 *
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
public @interface Synchronized
{
   public static final int DEFAULT_TIMEOUT = 1000;
   /**
    * How long should we wait for the lock
    * before throwing an exception?
    *
    * @return the timeout in milliseconds
    */
   long timeout() default DEFAULT_TIMEOUT;
}

