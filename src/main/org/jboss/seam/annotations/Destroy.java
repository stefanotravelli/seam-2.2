//$Id$
package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Alternative to javax.annotations.PreDestroy
 * for use in a pre Java EE 5 environment.
 * 
 * Designates a destroy method that is called when a
 * context ends and the component is being disposed.
 * 
 * @author Gavin King
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
@Inherited
public @interface Destroy {}
