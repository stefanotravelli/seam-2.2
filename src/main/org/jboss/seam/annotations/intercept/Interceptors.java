//$Id$
package org.jboss.seam.annotations.intercept;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Synonym for javax.interceptors.Interceptors, for
 * use in a pre Java EE 5 environment. Note that this
 * may only be used as a meta-annotation.
 * 
 * @author Gavin King
 */
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
public @interface Interceptors 
{
   Class[] value();
}
