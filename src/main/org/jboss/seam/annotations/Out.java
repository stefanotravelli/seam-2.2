/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jboss.seam.ScopeType;

/**
 * Specifies that a seam component should be outjected from
 * the annotated field or getter method of a session bean.
 * 
 * @author Gavin King
 */
@Target({FIELD, METHOD})
@Retention(RUNTIME)
@Documented
public @interface Out 
{
   /**
    * The context variable name. Defaults to the name of 
    * the annotated field or getter method.
    */
	String value() default "";
   /**
    * Specifies that the outjected value must not be
    * null, by default.
    */
   boolean required() default true;
   /**
    * Specifies the scope to outject to. If no scope is
    * explicitly specified, the default scope depends
    * upon whether the value is an instance of a Seam 
    * component. If it is, the component scope is 
    * used. Otherwise, the scope of the component with
    * the @Out attribute is used. But if the component
    * scope is STATELESS, the EVENT scope is used.
    */
   ScopeType scope() default ScopeType.UNSPECIFIED;
}
