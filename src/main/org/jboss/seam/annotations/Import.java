package org.jboss.seam.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allows use of unqualified names in @In by a component
 * or by all components in a package. The semantics are
 * similar to a .* import in Java.
 * 
 * @author Gavin King
 *
 */
@Target({TYPE, PACKAGE})
@Retention(RUNTIME)
@Documented
public @interface Import 
{
   /**
    * Specifies the name qualifiers for which unqualified names
    * may be used.
    * 
    * @return an array of name qualifiers
    */
   String[] value();
}
