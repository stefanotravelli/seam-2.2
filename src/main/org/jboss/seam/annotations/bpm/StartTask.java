/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.annotations.bpm;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jboss.seam.annotations.FlushModeType;

/**
 * Marks a method as causing jBPM {@link org.jbpm.taskmgmt.exe.TaskInstance task}
 * to be started. The jBPM {@link org.jbpm.context.exe.ContextInstance} 
 * is associated with the BUSINESS_PROCESS scope and the 
 * {@link org.jbpm.taskmgmt.exe.TaskInstance} is associated with a new
 * conversation, unless the annotated method returns a null outcome.
 * <p/>
 * Note that both {@link BeginTask} and {@link StartTask} have effect
 * before invocation of the intercepted method in that they are both
 * about setting up appropriate {@link org.jbpm.context.exe.ContextInstance}
 * for the current {@link org.jboss.seam.contexts.BusinessProcessContext};
 * {@link StartTask} however, also has effect after method invocation
 * as that is the time it actually marks the task as started.
 *
 * @see org.jbpm.taskmgmt.exe.TaskInstance#start()
 * @author Steve Ebersole
 */
@Target( METHOD )
@Retention( RUNTIME )
@Documented
public @interface StartTask
{
   /**
    * The name of the request parameter under which we should locate the
    * the id of task to be started.
    */
   String taskIdParameter() default "";
   /**
    * An EL expression that evaluates to the task id.
    * @return an EL expression
    */
   String taskId() default "#{param.taskId}";
   /**
    * The name of the jBPM process definition defining 
    * the page flow for this conversation.
    */
   String pageflow() default "";
   /**
    * An EL expression for the conversation id. If a conversation with 
    * the same id aready exists, Seam will redirect to that conversation.
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
