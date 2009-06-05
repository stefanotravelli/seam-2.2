package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies that the annotated component should be automatically
 * instantiated whenever it is asked for, even if @In does
 * not specify create=true. If this annotation appears at the
 * package level, it applies to all components in the package.
 * 
 * @author Gavin King
 *
 */
@Target({TYPE, PACKAGE})
@Retention(RUNTIME)
@Documented
public @interface AutoCreate {}
