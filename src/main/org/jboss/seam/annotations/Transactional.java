package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies that the transaction propagation for a JavaBean
 * component or method of a JavaBean component. JavaBean 
 * components have @Transactional(SUPPORTS) behavior 
 * if no @Transactional annotation is specified.
 * 
 * @author Gavin King
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Documented
@Inherited
public @interface Transactional
{
   /**
    * The transaction propagation type.
    * 
    * @return REQUIRED by default
    */
   TransactionPropagationType value() default TransactionPropagationType.REQUIRED;
}
