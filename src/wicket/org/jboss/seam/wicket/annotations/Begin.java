//$Id: Begin.java 6884 2007-12-03 06:26:39Z sbryzak2 $
package org.jboss.seam.wicket.annotations;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jboss.seam.annotations.FlushModeType;
/**
 * A version of the core @Begin annotation which can be placed on wicket component constructors.
 * The wicket interceptor will scan for this as well as the default @Begin annotation.  They
 * are identical in function, but java does not allow annotation inheritance.  In addition, the 
 * deprecated ifOutcome and id methods have been removed, and the pageflow method as well, as it is
 * not as yet supported in wicket.
 */
@Target({METHOD,CONSTRUCTOR})
@Retention(RUNTIME)
@Documented
public @interface Begin 
{
   /**
    * If enabled, and if a conversation is already active,
    * begin a nested conversation, instead of continuing
    * in the context of the existing conversation.
    */
   boolean nested() default false;
   /**
    * If false (the default), invocation of the begin
    * method in the scope of an existing conversation
    * will cause an exception to be thrown.
    */
   boolean join() default false;
   /**
    * Set the FlushMode for any EntityManager used in
    * this conversation.
    */
   FlushModeType flushMode() default FlushModeType.AUTO;

}
