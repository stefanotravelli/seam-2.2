package org.jboss.seam.ui.component;

import javax.faces.component.UIComponentBase;

/**
 * JSF component class
 * 
 */
public abstract class UIValidateAll extends UIComponentBase
{

   // TODO Make this a hidden=true, el=false property in validateAll.xml
   private boolean validatorsAdded = false;

   public boolean isValidatorsAdded()
   {
      return validatorsAdded;
   }

   public void setValidatorsAdded(boolean validatorsAdded)
   {
      this.validatorsAdded = validatorsAdded;
   }
}
