/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a component as immutable, not needing replication
 * once created, or a method of the component as read-only,
 * not mutating the state. This allows optimization of 
 * performance of JavaBean components in a clustered
 * environment, without the need to implement Mutable.
 * 
 * @see org.jboss.seam.core.Mutable
 * @author Gavin King
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Documented
public @interface ReadOnly {}


