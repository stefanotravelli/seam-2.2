package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Limit the scope of a CONVERSATION-scoped component
 * to just the parent conversation in which it was 
 * instantiated. The component instance will not be
 * visible to nested child conversations, which will
 * get their own instance.
 * 
 * Warning: this is ill-defined, since it implies that
 * a component will be visible for some part of a
 * request cycle, and invisible after that. It is not
 * recommended that applications use this feature!
 * 
 * @author Gavin King
 *
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
public @interface PerNestedConversation {}
