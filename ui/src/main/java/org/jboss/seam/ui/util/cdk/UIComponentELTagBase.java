package org.jboss.seam.ui.util.cdk;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.ActionSource2;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.MethodExpressionActionListener;
import javax.faces.event.MethodExpressionValueChangeListener;
import javax.faces.validator.MethodExpressionValidator;
import javax.faces.webapp.UIComponentELTag;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * @author Maksim Kaszynski
 *
 */
public abstract class UIComponentELTagBase extends UIComponentELTag {
	
	private static final LogProvider log = Logging.getLogProvider(UIComponentELTagBase.class);
	
	protected void setActionListenerProperty(UIComponent component, MethodExpression actionListener) {
		if (actionListener != null) {
			if (component instanceof ActionSource2) {
				ActionSource2 actionSource2 = (ActionSource2) component;
				actionSource2.addActionListener(new MethodExpressionActionListener(actionListener));
			} else {
				throw new IllegalArgumentException(Messages.getMessage(Messages.NO_ACTION_SOURCE2_ERROR, component.getClientId(getFacesContext())));
			}
		}
	}
	
	protected void setActionProperty(UIComponent component, MethodExpression action) {
		if (action != null) {
			if (component instanceof ActionSource2) {
				ActionSource2 actionSource2 = (ActionSource2) component;
				actionSource2.setActionExpression(action);
			} else {
				throw new IllegalArgumentException(Messages.getMessage(Messages.NO_ACTION_SOURCE2_ERROR, component.getClientId(getFacesContext())));
			}
		}
	}

	protected void setConverterProperty(UIComponent component, ValueExpression converter) {
        if (converter != null) {
			if (component instanceof ValueHolder) {
				ValueHolder output = (ValueHolder) component;
		            if (!converter.isLiteralText()) {
		                component.setValueExpression("converter", converter);
		            } else {
		                Converter conv = FacesContext.getCurrentInstance().getApplication().createConverter(converter.getExpressionString());
		                output.setConverter(conv);
		            }
			} else {
				 throw new IllegalArgumentException(Messages.getMessage(Messages.NO_VALUE_HOLDER_ERROR, component.getClass().getName()));
			}
        }
	}
	
	protected void setValidatorProperty(UIComponent component, MethodExpression validator) {
		
		if (validator != null) {
			if (component instanceof EditableValueHolder) {
				EditableValueHolder input = (EditableValueHolder) component;
				input.addValidator(new MethodExpressionValidator(validator));
			} else {
	            throw new IllegalArgumentException(Messages.getMessage(Messages.NO_EDITABLE_VALUE_HOLDER_ERROR, component.getId()));
			}
		}
	}
	
	protected void setValueChangeListenerProperty(UIComponent component, MethodExpression valueChangeListener) {
		if (valueChangeListener != null) {
			if (component instanceof EditableValueHolder) {
				EditableValueHolder input = (EditableValueHolder) component;
				input.addValueChangeListener(new MethodExpressionValueChangeListener(valueChangeListener));
			} else {
	            throw new IllegalArgumentException(Messages.getMessage(Messages.NO_EDITABLE_VALUE_HOLDER_ERROR, component.getId()));
			}
		}
	}
}
