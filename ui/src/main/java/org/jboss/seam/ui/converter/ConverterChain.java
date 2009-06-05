package org.jboss.seam.ui.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.el.ValueExpression;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * This class provides a chainable converter for JSF.
 * 
 * Any JSF converter can be placed at the end of the chain. A converter that is
 * placed higher up the chain should return ConverterChain.CONTINUE if
 * conversion has failed. If the all converters run return
 * ConverterChain.CONTINUE an unconverted value will be returned.
 * 
 * A converter can be placed in the chain with a priority, the order in which
 * converters with the same priority is run is not specified.
 * 
 */
public class ConverterChain implements Converter, StateHolder
{

   public static final String CONTINUE = "org.jboss.seam.ui.ConverterChain.continue";

   /**
    * This places the converter at the end of the chain. No garuntee is made
    * about the order converters which are placed on the queue with this
    * priority will be run
    */
   public static final int CHAIN_END = Integer.MAX_VALUE;

   /**
    * This places the converter at the head of the chain. No garuntee is made
    * about the order converters which are placed on the queue with this
    * priority will be run
    */
   public static final int CHAIN_START = 0;

   private List<PrioritizableConverter> converters;

   private boolean dirty;

   public ConverterChain()
   {
      // A Priority Queue would be nice but JSF has issues serializing that
      converters = new ArrayList<PrioritizableConverter>();
   }

   /**
    * Set up a ConverterChain for this component.
    * 
    * This replaces any existing converter with a ConverterChain with the
    * existing Converter at the end of the chain
    * 
    * @param component
    */
   public ConverterChain(UIComponent component)
   {
      this();
      if (component instanceof ValueHolder)
      {
         ValueHolder valueHolder = (ValueHolder) component;
         if (!(valueHolder.getConverter() instanceof ConverterChain)) 
         {
            ValueExpression converterValueExpression = component.getValueExpression("converter");
            if (converterValueExpression != null)
            {
               addConverterToChain(converterValueExpression);
            }
            else if (valueHolder.getConverter() != null)
            {
               addConverterToChain(valueHolder.getConverter());
            }
            else
            {
               ValueExpression valueExpression = component.getValueExpression("value");
               FacesContext facesContext = FacesContext.getCurrentInstance();
               if (valueExpression != null)
               {
                  Class<?> type = valueExpression.getType(facesContext.getELContext());
                  if (type != null)
                  {
                     Converter converter = facesContext.getApplication().createConverter(type);
                     if (converter != null)
                     {
                        addConverterToChain(converter);
                     }
                  }
               }
            }
            valueHolder.setConverter(this);
         }
      }
   }

   public Object getAsObject(FacesContext context, UIComponent component, String value)
            throws ConverterException
   {
      Object output = value;
      for (Converter converter : getConverters())
      {
         Object result = converter.getAsObject(context, component, value);
         if (!CONTINUE.equals(result))
         {
            output = result;
            break;
         }
      }
      return output;
   }

   public String getAsString(FacesContext context, UIComponent component, Object value)
            throws ConverterException
   {
      String output = value ==  null ? null : value.toString();
      for (Converter converter : getConverters())
      {
         String result = converter.getAsString(context, component, value);
         if (!CONTINUE.equals(result))
         {
            output = result;
            break;
         }
      }
      return output;
   }

   /**
    * Add a converter to the end of the chain
    */
   public boolean addConverterToChain(Converter c)
   {
      return addConverterToChain(c, CHAIN_END);
   }

   /**
    * Add a converter to the end of the chain
    */
   public boolean addConverterToChain(ValueExpression c)
   {
      return addConverterToChain(c, CHAIN_END);
   }

   /**
    * Add a converter to the chain with a defined priority
    */
   public boolean addConverterToChain(Converter c, int priority)
   {
      if (c != null)
      {
         dirty = true;
         return converters.add(new PrioritizableConverter(c, priority));
      }
      else
      {
         return false;
      }
   }

   /**
    * Add a converter to the chain with a defined priority
    */
   public boolean addConverterToChain(ValueExpression c, int priority)
   {
      if (c != null)
      {
         dirty = true;
         return converters.add(new PrioritizableConverter(c, priority));
      }
      else
      {
         return false;
      }
   }

   private boolean _transient;

   public boolean isTransient()
   {
      return _transient;
   }

   public void restoreState(FacesContext context, Object state)
   {
      Object[] values = (Object[]) state;
      converters = (List<PrioritizableConverter>) UIComponentBase.restoreAttachedState(context,
               values[0]);
      dirty = true;
   }

   public Object saveState(FacesContext context)
   {
      Object[] values = new Object[1];
      values[0] = UIComponentBase.saveAttachedState(context, converters);
      return values;
   }

   public void setTransient(boolean newTransientValue)
   {
      this._transient = newTransientValue;

   }

   private List<PrioritizableConverter> getConverters()
   {
      if (dirty)
      {
         Collections.sort(converters);
      }
      return converters;
   }
   
   public boolean containsConverterType(Converter converter) {
      // TODO Improve this
      for (Converter c : converters) {
         if (c.getClass().equals(converter.getClass())) {
            return true;
         }
      }
      return false;
   }
}