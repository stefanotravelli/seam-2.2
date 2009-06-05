package org.jboss.seam.navigation;

import javax.faces.context.FacesContext;

import org.jboss.seam.faces.Navigator;

/**
 * The result of a navigation rule.
 * 
 * @author Gavin King
 *
 */
public abstract class NavigationHandler extends Navigator
{
   /**
    * Go ahead and execute the navigation rule. 
    */
   public abstract boolean navigate(FacesContext context);
}