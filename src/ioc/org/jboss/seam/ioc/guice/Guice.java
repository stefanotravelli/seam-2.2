package org.jboss.seam.ioc.guice;

import org.jboss.seam.annotations.intercept.Interceptors;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Google Guice support. Allows use of Guice injection inside Seam component.
 *
 * @author Pawel Wrzeszcz (pwrzeszcz [at] jboss . org)
 */
@Target(TYPE)
@Retention(RUNTIME)
@Interceptors(GuiceInterceptor.class)
public @interface Guice
{
   /**
    * Name of the Guice injector component.
    * By default the injector specified in the components.xml file is used.
    */
   String value() default "";
}
