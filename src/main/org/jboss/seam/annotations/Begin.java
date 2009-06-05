//$Id$
package org.jboss.seam.annotations;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
/**
 * Marks a method as beginning a long-running conversation, 
 * if none exists, and if the method returns a non-null value 
 * without throwing an exception.
 *  
 * A null outcome never begins a conversation.
 * If the method is of type void, a conversation always
 * begins.
 * 
 * @author Gavin King
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Begin 
{
	/**
	 * An empty outcome list is interpreted to mean any 
	 * outcome except for the null (redisplay) outcome.
    * 
    * @deprecated use Conversation.instance().begin();
	 */
	String[] ifOutcome() default {};
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
    * The name of the jBPM process definition defining 
    * the page flow for this conversation.
    */
   String pageflow() default "";
   /**
    * An EL expression for the conversation id. If a 
    * conversation with the same id aready exists, Seam 
    * will redirect to that conversation.
    * 
    * @deprecated use <conversation/> in pages.xml
    */
   String id() default "";
   /**
    * Set the FlushMode for any EntityManager used in
    * this conversation.
    */
   FlushModeType flushMode() default FlushModeType.AUTO;

}
