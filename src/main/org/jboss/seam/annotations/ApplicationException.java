package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Synonym for javax.ejb.ApplicationException, 
 * for use in a pre Java EE 5 environment.
 * 
 * @author Gavin King
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
public @interface ApplicationException
{
   /**
    * @return true if this exception should set 
    * the transaction to rollback only
    */
   public boolean rollback() default false;
   /**
    * @return true if this exception should end
    * the current long-running conversation
    */
   public boolean end() default false;
}
