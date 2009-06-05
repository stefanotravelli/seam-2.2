package org.jboss.seam.annotations.intercept;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Annotates an interceptor class and specifies what 
 * kind of interceptor it is (client side or server 
 * side), and its ordering with respect to other
 * interceptors in the stack.
 * 
 * @author Gavin King
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Interceptor
{
   /**
    * Specifies that the interceptor is a SERVER or CLIENT
    * side interceptor.
    * 
    * @return SERVER by default
    */
   InterceptorType type() default InterceptorType.SERVER;
   /**
    * Specifies that an interceptor is called "around" 
    * another interceptor or interceptors.
    */
   Class[] around() default {};
   /**
    * Specifies that an interceptor is called "within" 
    * another interceptor or interceptors.
    */
   Class[] within() default {};
   /**
    * Performance optimization for stateless interceptors.
    */
   boolean stateless() default false;
}
