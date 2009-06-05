package org.jboss.seam.wicket.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.wicket.Page;

@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface NoConversationPage
{

   Class<? extends Page> value();
   
}
