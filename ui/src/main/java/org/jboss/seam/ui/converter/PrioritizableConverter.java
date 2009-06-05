package org.jboss.seam.ui.converter;

import javax.el.ValueExpression;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * Helper class for ConverterChain
 *
 */
public class PrioritizableConverter implements Converter, Comparable<PrioritizableConverter>,
         StateHolder
{

   private ValueExpression valueExpression;

   private Converter delegate;

   private int priority;

   public PrioritizableConverter()
   {
   }

   public PrioritizableConverter(ValueExpression vb, int priority)
   {
      this.valueExpression = vb;
      this.priority = priority;
   }

   public PrioritizableConverter(Converter delegate, int priority)
   {
      this.delegate = delegate;
      this.priority = priority;
   }

   public Converter getDelegate()
   {
      if (valueExpression != null)
      {
         return (Converter) valueExpression.getValue(FacesContext.getCurrentInstance().getELContext());
      }
      else
      {
         return delegate;
      }
   }

   public int getPriority()
   {
      return priority;
   }

   public Object getAsObject(FacesContext context, UIComponent component, String value)
            throws ConverterException
   {
      return getDelegate().getAsObject(context, component, value);
   }

   public String getAsString(FacesContext context, UIComponent component, Object value)
            throws ConverterException
   {
      return getDelegate().getAsString(context, component, value);
   }

   public int compareTo(PrioritizableConverter o)
   {
      return this.getPriority() - o.getPriority();
   }

   private boolean _transient;

   public boolean isTransient()
   {
      return _transient;
   }

   public void restoreState(FacesContext context, Object state)
   {
      Object[] values = (Object[]) state;
      delegate = (Converter) UIComponentBase.restoreAttachedState(context, values[0]);
      priority = (Integer) values[1];
      valueExpression = (ValueExpression) values[2];
   }

   public Object saveState(FacesContext context)
   {
      Object[] values = new Object[3];
      values[0] = UIComponentBase.saveAttachedState(context, delegate);
      values[1] = priority;
      values[2] = valueExpression;
      return values;
   }

   public void setTransient(boolean newTransientValue)
   {
      this._transient = newTransientValue;

   }
}