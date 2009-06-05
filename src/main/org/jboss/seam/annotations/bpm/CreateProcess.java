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
 * Marks a method creating a jBPM 
 * {@link org.jbpm.graph.exe.ProcessInstance process instance}
 * unless the method throws an exception or returns a null outcome.
 * 
 * @author Steve Ebersole
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface CreateProcess
{
   /**
    * The name of the {@link org.jbpm.graph.def.ProcessDefinition} from which
    * to create the {@link org.jbpm.graph.exe.ProcessInstance}
    */
   String definition();
   /**
    * An EL expression that evaluates to the process 
    * business key.
    * 
    * @return an EL expression or an empty string to indicate a null key
    */
   String processKey() default "";
}
