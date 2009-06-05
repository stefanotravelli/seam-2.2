//$Id$
package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jboss.seam.ScopeType;

/**
 * Marks a method as a factory method for a context variable.
 * A factory method is called whenever no value is bound to
 * the named context variable, and is expected to initialize
 * the value of the context variable. There are two kinds of 
 * factory methods. Factory methods with void return type are 
 * responsible for outjecting a value to the context variable. 
 * Factory methods which return a value do not need to 
 * explicitly ouject the value, since Seam will bind the
 * returned value to the specified scope.
 * 
 * This annotation supports use of the Seam "factory component"
 * pattern.
 * 
 * @author Gavin King
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Factory {
   /**
    * @return the name of the context variable
    */
   String value() default "";
   /**
    * The scope() element is meaningful only for factory 
    * methods that return the value to be outjected. 
    * Factory methods that return void are expected to
    * take responsibility for outjecting the value,
    * and then scope() is ignored.
    * 
    * If the factory method returns a value, but no 
    * scope is explicitly specified, the scope of 
    * the component with the @Factory attribute is used. 
    * But if the component scope is STATELESS, the EVENT 
    * scope is used.
    * 
    * @return the scope to outject any returned value
    */
   ScopeType scope() default ScopeType.UNSPECIFIED;
   /**
     * Specifies that this factory method should be automatically
     * called whenever the variable is asked for, even if @In does
     * not specify create=true.
     * 
    */
   boolean autoCreate() default false;
}
