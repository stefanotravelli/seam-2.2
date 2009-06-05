/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies the component name of a Seam component.
 * 
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * 
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Name 
{
   /**
    * @return the component name
    */
   String value();
}


