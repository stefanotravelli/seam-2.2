/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies whether or not a component should be installed if it is scanned
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Install 
{
   /**
    * Precedence of all built-in Seam components
    */
   public static final int BUILT_IN = 0;
   /**
    * Precedence to use for components of frameworks 
    * which extend Seam
    */
   public static final int FRAMEWORK = 10;
   /**
    * Predence of application components (the
    * default precedence)
    */
   public static final int APPLICATION = 20;
   /**
    * Precedence to use for components which override
    * application components in a particular deployment
    */
   public static final int DEPLOYMENT = 30;

   /**
    * Precedence to use for mock objects in tests
    */
   public static final int MOCK = 40;

   /**
    * Is this component installed by default?
    * 
    * @return indicates if the component should be installed
    */
   boolean value() default true;
   
   /**
    * Is this a debug component?
    * 
    * @return indicates that the component should only be installed in debug mode
    */
   boolean debug() default false;
   
   /**
    * Indicates that the component should not be installed unless the
    * dependent components are installed
    *  
    * @return the dependent component names
    */
   String[] dependencies() default {};
   /**
    * Indicates that the component should not be installed unless the
    * dependent components are installed
    *  
    * @return the dependent component types
    */
   Class[] genericDependencies() default {};
   /**
    * Indicates that the component should not be installed unless the
    * the given class definitions are available on the classpath
    *  
    * @return the dependent classes
    */
   String[] classDependencies() default {};
   /**
    * The precedence of the component. If multiple components with
    * the same name exist, the one with the higher precedence will
    * be installed.
    */
   public int precedence() default APPLICATION;
}


