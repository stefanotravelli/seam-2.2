/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies the JNDI name of a seam component.
 * 
 * @author Gavin King
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface JndiName 
{
   String value();
}


