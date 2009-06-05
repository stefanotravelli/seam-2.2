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

/**
 * Marks a method as causing an existing jBPM 
 * {@link org.jbpm.graph.exe.ProcessInstance process instance}
 * to be associated with the current conversation, unless the 
 * annotated method returns a null outcome.
 * 
 * @author Steve Ebersole
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface ResumeProcess
{
   /**
    * The name of the request parameter under which we should locate the
    * the id of process to be resumed.
    * (not required for lookup by business key)
    * 
    * @return a request parameter name
    */
   String processIdParameter() default "";
   /**
    * An EL expression that evaluates to the process id.
    * (not required for lookup by business key)
    * 
    * @return an EL expression
    */
   String processId() default "#{param.processId}";
   /**
    * An EL expression that evaluates to the process 
    * business key.
    * (optional, only required for lookup by business key)
    * 
    * @return an EL expression
    */
   String processKey() default "";
   /**
    * The name of the {@link org.jbpm.graph.def.ProcessDefinition}
    * (optional, only required for lookup by business key)
    */
   String definition() default "";
}
