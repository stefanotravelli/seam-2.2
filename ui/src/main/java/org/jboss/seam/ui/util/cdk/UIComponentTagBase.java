/**
 * 
 */
package org.jboss.seam.ui.util.cdk;

import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIGraphic;
import javax.faces.component.UIParameter;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.ValueHolder;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

/**
 * @author Maksim Kaszynski
 * 
 */
public abstract class UIComponentTagBase extends UIComponentELTagBase {

   protected ExpressionFactory getExpressionFactory() {
      return getFacesContext().getApplication().getExpressionFactory();
   }

   protected void setProperty(UIComponent component, String propName, ValueExpression valueExpression) {
      if (valueExpression != null) {
         if (valueExpression.isLiteralText()) {
            component.getAttributes().put(propName,valueExpression.getValue(getELContext()));
         } else {
            component.setValueExpression(propName, valueExpression);
         }
      }
   }
   
   protected void setProperty(UIComponent component, Class<?> type,
         String propName, String value) {
      if (value != null) {
         ValueExpression valueExpression = getExpressionFactory()
               .createValueExpression(getELContext(), value, type);
         setProperty(component, propName, valueExpression);
      }
   }

   protected void setIntegerProperty(UIComponent component, String propName,
         ValueExpression value) {
      setProperty(component, propName, value);
   }

   protected void setLongProperty(UIComponent component, String propName,
         ValueExpression value) {
      setProperty(component, propName, value);
   }

   protected void setFloatProperty(UIComponent component, String propName,
         ValueExpression value) {
      setProperty(component, propName, value);
   }

   protected void setDoubleProperty(UIComponent component, String propName,
         ValueExpression value) {
      setProperty(component, propName, value);
   }

   protected void setStringProperty(UIComponent component, String propName,
         ValueExpression value) {
      setProperty(component, propName, value);
   }

   protected void setBooleanProperty(UIComponent component, String propName,
         ValueExpression value) {
      setProperty(component, propName, value);
   }

   protected void setIntegerProperty(UIComponent component, String propName,
         String value) {
      setProperty(component, Integer.class, propName, value);
   }

   protected void setLongProperty(UIComponent component, String propName,
         String value) {
      setProperty(component, Long.class, propName, value);
   }

   protected void setFloatProperty(UIComponent component, String propName,
         String value) {
      setProperty(component, Float.class, propName, value);
   }

   protected void setDoubleProperty(UIComponent component, String propName,
         String value) {
      setProperty(component, Double.class, propName, value);
   }

   protected void setStringProperty(UIComponent component, String propName,
         String value) {
      setProperty(component, String.class, propName, value);
   }

   protected void setBooleanProperty(UIComponent component, String propName,
         String value) {
      setProperty(component, Boolean.class, propName, value);
   }
   protected void setValueProperty(UIComponent component, String value) {
      if (value != null) {
         ValueExpression expression = getExpressionFactory()
               .createValueExpression(getELContext(), value, Object.class);

         setValueProperty(component, expression);
      }
   }
   protected void setValueProperty(UIComponent component, ValueExpression expression) {
      if (expression != null) {
         String value = expression.getExpressionString();
         if (!expression.isLiteralText()) {
            component.setValueExpression("value", expression);
         } else if (component instanceof UICommand) {
            ((UICommand) component).setValue(value);
         } else if (component instanceof UIParameter) {
            ((UIParameter) component).setValue(value);
         } else if (component instanceof UISelectBoolean) {
            ((UISelectBoolean) component).setValue(Boolean.valueOf(value));
         } else if (component instanceof UIGraphic) {
            ((UIGraphic) component).setValue(value);
         }
         // Since many input components are ValueHolders the special
         // components
         // must come first, ValueHolder is the last resort.
         else if (component instanceof ValueHolder) {
            ((ValueHolder) component).setValue(value);
         } else {
            component.getAttributes().put("value", value);
         }
      }
   }

   public boolean isValueReference(String s) {
      return !getExpressionFactory().createValueExpression(s, Object.class).isLiteralText();
   }
   
    protected void setActionProperty(UIComponent component, String action) {
        if (action != null) {
         MethodExpression expression = 
            getExpressionFactory().createMethodExpression(getELContext(), action, String.class, new Class[] {});
         setActionProperty(component, expression);
        }
    }

    protected void setActionListenerProperty(UIComponent component, String actionListener){
        if (actionListener != null) {
         MethodExpression expression = 
            getExpressionFactory().createMethodExpression(getELContext(), actionListener, String.class, new Class[] {ActionEvent.class});
         setActionListenerProperty(component, expression);
        }
    }

    protected void setValueChangedListenerProperty(UIComponent component, String valueChangedListener) {
        if (valueChangedListener != null) {
         MethodExpression expression = 
            getExpressionFactory().createMethodExpression(getELContext(), valueChangedListener, String.class, new Class[] {ValueChangeEvent.class});
         setValueChangeListenerProperty(component, expression);
        }
    }

}
