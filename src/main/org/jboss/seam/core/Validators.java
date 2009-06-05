package org.jboss.seam.core;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.el.ValueExpression;

import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.jboss.seam.Component;
import org.jboss.seam.Instance;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.el.EL;

/**
 * Caches instances of Hibernate Validator ClassValidator
 * 
 * @author Gavin King
 * 
 */
@Name("org.jboss.seam.core.validators")
@BypassInterceptors
@Scope(ScopeType.APPLICATION)
@Install(precedence = BUILT_IN, classDependencies = "org.hibernate.validator.ClassValidator")
public class Validators
{

   // TODO: should use weak references here...
   private Map<Key, ClassValidator> classValidators = new ConcurrentHashMap<Key, ClassValidator>();

   class Key
   {
      private Class validatableClass;
      private java.util.Locale locale;

      public Key(Class validatableClass, java.util.Locale locale)
      {
         this.validatableClass = validatableClass;
         this.locale = locale;
      }

      @Override
      public boolean equals(Object other)
      {
         if (other == null || !(other instanceof Key)) {
             return false;             
         }
         
         Key key = (Key) other;
         return key.validatableClass.equals(validatableClass) && key.locale.equals(locale);
      }

      @Override
      public int hashCode()
      {
         return validatableClass.hashCode() + locale.hashCode();
      }
   }

   /**
    * Get the cached ClassValidator instance. If the argument is an instance of
    * a session bean Seam component instance, the returned validator will be
    * aware of constraints defined on the bean class. Therefore this method is
    * preferred to getValidator(Class) if the argument might be a session bean.
    * 
    * @param model the object to be validated
    */
   public <T> ClassValidator<T> getValidator(T model)
   {
      Class modelClass = model instanceof Instance ? ((Instance) model).getComponent().getBeanClass() : model.getClass();
      return getValidator((Class<T>) modelClass);
   }

   /**
    * Get the cached ClassValidator instance.
    * 
    * @param modelClass the class to be validated
    */
   @SuppressWarnings("unchecked")
   public <T> ClassValidator<T> getValidator(Class<T> modelClass)
   {
      java.util.ResourceBundle bundle = SeamResourceBundle.getBundle();
      Locale none = bundle == null ? new Locale("NONE") : bundle.getLocale();
      Key key = new Key(modelClass, none);
      ClassValidator result = classValidators.get(key);
      if (result == null)
      {
         result = createValidator(modelClass);
         classValidators.put(key, result);
      }
      return result;
   }

   /**
    * Create a new ClassValidator for the given class, using the current Seam
    * ResourceBundle.
    * 
    * @param modelClass the class to be validated
    */
   @SuppressWarnings("unchecked")
   protected <T> ClassValidator<T> createValidator(Class<T> modelClass)
   {
      java.util.ResourceBundle bundle = SeamResourceBundle.getBundle();

      return bundle == null ? new ClassValidator(modelClass) : new ClassValidator(modelClass, bundle);
   }

   /**
    * Validate that the given value can be assigned to the property given by the
    * value expression.
    * 
    * @param valueExpression a value expression, referring to a property
    * @param elContext the ELContext in which to evaluate the expression
    * @param value a value to be assigned to the property
    * @return a set of potential InvalidValues, from Hibernate Validator
    */
   public InvalidValue[] validate(ValueExpression valueExpression, ELContext elContext, Object value)
   {
      ValidatingResolver validatingResolver = new ValidatingResolver(elContext.getELResolver());
      ELContext decoratedContext = EL.createELContext(elContext, validatingResolver);
      valueExpression.setValue(decoratedContext, value);
      return validatingResolver.getInvalidValues();
   }

   class ValidatingResolver extends ELResolver
   {
      private ELResolver delegate;
      private InvalidValue[] invalidValues;

      public ValidatingResolver(ELResolver delegate)
      {
         this.delegate = delegate;
      }

      public InvalidValue[] getInvalidValues()
      {
         return invalidValues;
      }

      @Override
      public Class<?> getCommonPropertyType(ELContext context, Object value)
      {
         return delegate.getCommonPropertyType(context, value);
      }

      @Override
      public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object value)
      {
         return delegate.getFeatureDescriptors(context, value);
      }

      @Override
      public Class<?> getType(ELContext context, Object x, Object y) throws NullPointerException, PropertyNotFoundException, ELException
      {
         return delegate.getType(context, x, y);
      }

      @Override
      public Object getValue(ELContext context, Object base, Object property) throws NullPointerException, PropertyNotFoundException, ELException
      {
         return delegate.getValue(context, base, property);
      }

      @Override
      public boolean isReadOnly(ELContext context, Object base, Object property) throws NullPointerException, PropertyNotFoundException, ELException
      {
         return delegate.isReadOnly(context, base, property);
      }

      @Override
      public void setValue(ELContext context, Object base, Object property, Object value) throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException
      {
         if (base != null && property != null)
         {
            context.setPropertyResolved(true);
            invalidValues = getValidator(base).getPotentialInvalidValues(property.toString(), value);
         }

      }

   }

   public static Validators instance()
   {
      if (!Contexts.isApplicationContextActive())
      {
         throw new IllegalStateException("No active application scope");
      }
      return (Validators) Component.getInstance(Validators.class, ScopeType.APPLICATION);
   }

}
