package org.jboss.seam.ui.component;

import javax.faces.component.UIComponentBase;

/**
 * Tag that auto-generates script imports for Seam Remoting
 *  
 * @author Shane Bryzak
 */
public abstract class UIRemote extends UIComponentBase
{
   
   public abstract String getInclude();

   public abstract void setInclude(String include);
   
}
