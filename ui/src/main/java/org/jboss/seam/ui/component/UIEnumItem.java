
package org.jboss.seam.ui.component;

import javax.faces.component.UISelectItem;
import javax.faces.model.SelectItem;

/**
 * JSF component class
 * 
 */
public abstract class UIEnumItem extends UISelectItem
{

   public abstract String getEnumValue();

   public abstract void setEnumValue(String enumValue);
   
   public abstract void setLabel(String label);
   
   public abstract String getLabel();
   
   @Override
   public Object getItemValue()
   {
      return getEnumValue();
   }
   
   @Override
   public void setItemValue(Object itemValue)
   {
      setEnumValue(itemValue == null ? null : itemValue.toString());
   }
   
   @Override
   public String getItemLabel()
   {
      return getLabel();
   }

   @Override
   public void setItemLabel(String itemLabel)
   {
      setLabel(itemLabel);
   }
   
   @Override
   public Object getValue()
   {
      Class c = getParent().getValueExpression("value").getType(getFacesContext().getELContext());
      String enumValue = getEnumValue();
      String label = getLabel();
      Object value = Enum.valueOf(c, enumValue);
      return new SelectItem(value, label == null ? enumValue : label);
   }
}
