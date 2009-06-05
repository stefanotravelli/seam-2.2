package org.jboss.seam.annotations.web;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotates a servlet filter that should be installed
 * by Seam's master filter and specifies its ordering 
 * with respect to other filters in the stack.
 * 
 * @see org.jboss.seam.web.AbstractFilter
 * 
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Filter
{
   
   /**
    * Specifies that a filter is called "around" 
    * another filter or filters.
    */
   String[] around() default {};
   /**
    * Specifies that an filter is called "within" 
    * another filter or filterss.
    */
   String[] within() default {};

}
