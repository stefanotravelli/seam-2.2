//$Id$
package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies that an instance of this component is
 * created at system initialization time for an
 * application scoped component, or when a session
 * is started for a session scoped component. May only
 * be applied to APPLICATION or SESSION scoped 
 * components.
 * 
 * @author Gavin King
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Startup 
{
   /**
    * A list of other Seam Components that should be started
    * before this one, if they are installed.
    * 
    * If applied to an APPLICATION scope component, the
    * dependant components should be APPLICATION scope. If
    * applied to a SESSION scope component, the components
    * should be in SESSION scope.
    */
   String[] depends() default {};
}
