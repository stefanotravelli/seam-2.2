package org.jboss.seam.annotations.intercept;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Synonym for {@link javax.ejb.PostConstruct}, for
 * use in a pre Java EE 5 environment.
 * 
 * @author Denis Forveille
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface PostConstruct {}
