//$Id: AroundInvoke.java 5522 2007-06-25 22:28:50Z gavin $
package org.jboss.seam.annotations.intercept;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Synonym for {@link javax.ejb.PostActivate}, for
 * use in a pre Java EE 5 environment.
 * 
 * @author Pete Muir
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface PostActivate {}
