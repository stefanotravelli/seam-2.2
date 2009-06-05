//$Id$
package org.jboss.seam.intercept;

import java.io.Serializable;

import org.jboss.seam.Component;

/**
 * Superclass of built-in interceptors
 * 
 * @author Gavin King
 */
public abstract class AbstractInterceptor implements Serializable, OptimizedInterceptor
{
   private static final long serialVersionUID = -8838873111255032911L;
   private transient Component component; //a cache of the Component reference
   private String componentName;

   public void setComponent(Component component)
   {
      componentName = component.getName();
      this.component = component;
   }

   protected Component getComponent()
   {
      if (component==null)
      {
         component = Component.forName(componentName);
      }
      return component;
   }

}
