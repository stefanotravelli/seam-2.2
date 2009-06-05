package org.jboss.seam.mock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.el.CompositeELResolver;
import javax.el.ExpressionFactory;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.convert.BigDecimalConverter;
import javax.faces.convert.BigIntegerConverter;
import javax.faces.convert.BooleanConverter;
import javax.faces.convert.ByteConverter;
import javax.faces.convert.CharacterConverter;
import javax.faces.convert.Converter;
import javax.faces.convert.DoubleConverter;
import javax.faces.convert.FloatConverter;
import javax.faces.convert.IntegerConverter;
import javax.faces.convert.LongConverter;
import javax.faces.convert.ShortConverter;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionListener;
import javax.faces.validator.Validator;

import org.jboss.seam.el.EL;
import org.jboss.seam.el.SeamExpressionFactory;
import org.jboss.seam.jsf.SeamNavigationHandler;
import org.jboss.seam.jsf.SeamStateManager;
import org.jboss.seam.jsf.SeamViewHandler;
import org.jboss.seam.jsf.UnifiedELMethodBinding;
import org.jboss.seam.jsf.UnifiedELValueBinding;
import org.jboss.seam.util.Reflections;

@SuppressWarnings("deprecation")
public class MockApplication extends Application
{
   
   private javax.el.CompositeELResolver elResolver;
   private javax.el.CompositeELResolver additionalResolvers;
   private Collection locales;
   
   public MockApplication()
   {
     elResolver = new CompositeELResolver();
     additionalResolvers = new CompositeELResolver();
     elResolver.add(additionalResolvers);
     elResolver.add(EL.EL_RESOLVER); 
   }
   
   @Override
   public Object evaluateExpressionGet(FacesContext context, String expression, Class type) throws javax.el.ELException 
   {
      return getExpressionFactory().createValueExpression(context.getELContext(), expression, type).getValue(context.getELContext());
   }
   
   @Override
   public void addELContextListener(javax.el.ELContextListener elcl) 
   {
      throw new UnsupportedOperationException();
   }
   
   @Override
   public void addELResolver(javax.el.ELResolver r) 
   {
      additionalResolvers.add(r);
   }
   
   @Override
   public UIComponent createComponent(javax.el.ValueExpression ve, FacesContext context, String id) throws FacesException 
   {
      throw new UnsupportedOperationException();
   }
   
   @Override
   public javax.el.ELContextListener[] getELContextListeners() 
   {
      throw new UnsupportedOperationException();
   }
   
   
   
   @Override
   public javax.el.ELResolver getELResolver() 
   {
      return elResolver;
   }
   
   @Override
   public java.util.ResourceBundle getResourceBundle(FacesContext context, String string) 
   {
      throw new UnsupportedOperationException();
   }
   
   @Override
   public void removeELContextListener(javax.el.ELContextListener elcl) 
   {
      throw new UnsupportedOperationException();
   }
   
   @Override
   public ActionListener getActionListener()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setActionListener(ActionListener listener)
   {
      throw new UnsupportedOperationException();
   }

   private Locale defaultLocale = Locale.ENGLISH;

   @Override
   public Locale getDefaultLocale()
   {
      return defaultLocale;
   }

   @Override
   public void setDefaultLocale(Locale locale)
   {
      defaultLocale = locale;
   }

   @Override
   public String getDefaultRenderKitId()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setDefaultRenderKitId(String rk)
   {
      throw new UnsupportedOperationException();
   }

   private String msgBundleName;

   @Override
   public String getMessageBundle()
   {
      return msgBundleName;
   }

   @Override
   public void setMessageBundle(String bundleName)
   {
      this.msgBundleName = bundleName;
   }

   private NavigationHandler navigationHandler = new SeamNavigationHandler( new MockNavigationHandler() );

   @Override
   public NavigationHandler getNavigationHandler()
   {
      return navigationHandler;
   }

   @Override
   public void setNavigationHandler(NavigationHandler navigationHandler)
   {
      this.navigationHandler = navigationHandler;
   }

   @Override
   public PropertyResolver getPropertyResolver()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setPropertyResolver(PropertyResolver pr)
   {
      throw new UnsupportedOperationException();
   }

   private VariableResolver variableResolver = /*new SeamVariableResolver(*/ new VariableResolver() { 
      @Override
      public Object resolveVariable(FacesContext ctx, String name) throws EvaluationException
      {
         return null;
      }
   } /*)*/;

   @Override
   public VariableResolver getVariableResolver()
   {
      return variableResolver;
   }

   @Override
   public void setVariableResolver(VariableResolver variableResolver)
   {
      this.variableResolver = variableResolver;
   }

   private ViewHandler viewHandler = new SeamViewHandler( new MockViewHandler() );

   @Override
   public ViewHandler getViewHandler()
   {
      return viewHandler;
   }

   @Override
   public void setViewHandler(ViewHandler viewHandler)
   {
      this.viewHandler = viewHandler;
   }

   private StateManager stateManager = new SeamStateManager( new MockStateManager() );

   @Override
   public StateManager getStateManager()
   {
      return stateManager;
   }

   @Override
   public void setStateManager(StateManager stateManager)
   {
      this.stateManager = stateManager;
   }

   @Override
   public void addComponent(String name, String x)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public UIComponent createComponent(String name) throws FacesException
   {
      // Best guess component creation with a dummy component if it can't be found
      if (name.startsWith("org.jboss.seam.mail.ui") || name.startsWith("org.jboss.seam.excel.ui"))
      {
        try
        {
           return (UIComponent) Class.forName(name).newInstance();
        } 
        catch (Exception e)
        {
           throw new UnsupportedOperationException("Unable to create component " + name);
        }
      }
      else
      {
         // Oh well, can't simply create the component so put a dummy one in its place
         return new UIOutput();
      }
   }

   @Override
   public UIComponent createComponent(ValueBinding vb, FacesContext fc, String x)
            throws FacesException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Iterator getComponentTypes()
   {
      throw new UnsupportedOperationException();
   }

   private final Map<Class, Converter> converters = new HashMap<Class, Converter>();
   {
      converters.put(Integer.class, new IntegerConverter());
      converters.put(Long.class, new LongConverter());
      converters.put(Float.class, new FloatConverter());
      converters.put(Double.class, new DoubleConverter());
      converters.put(Boolean.class, new BooleanConverter());
      converters.put(Short.class, new ShortConverter());
      converters.put(Byte.class, new ByteConverter());
      converters.put(Character.class, new CharacterConverter());
      converters.put(BigDecimal.class, new BigDecimalConverter());
      converters.put(BigInteger.class, new BigIntegerConverter());
   }

   private final Map<String, Converter> convertersById = new HashMap<String, Converter>();
   {
      convertersById.put(IntegerConverter.CONVERTER_ID, new IntegerConverter());
      convertersById.put(LongConverter.CONVERTER_ID, new LongConverter());
      convertersById.put(FloatConverter.CONVERTER_ID, new FloatConverter());
      convertersById.put(DoubleConverter.CONVERTER_ID, new DoubleConverter());
      convertersById.put(BooleanConverter.CONVERTER_ID, new BooleanConverter());
      convertersById.put(ShortConverter.CONVERTER_ID, new ShortConverter());
      convertersById.put(ByteConverter.CONVERTER_ID, new ByteConverter());
      convertersById.put(CharacterConverter.CONVERTER_ID, new CharacterConverter());
      convertersById.put(BigDecimalConverter.CONVERTER_ID, new BigDecimalConverter());
      convertersById.put(BigIntegerConverter.CONVERTER_ID, new BigIntegerConverter());
   }

   @Override
   public void addConverter(String id, String converterClass)
   {
      convertersById.put(id, instantiateConverter(converterClass));
   }

   @Override
   public void addConverter(Class type, String converterClass)
   {
      converters.put(type, instantiateConverter(converterClass));
   }

   private Converter instantiateConverter(String converterClass)
   {
      try
      {
         return (Converter) Reflections.classForName(converterClass).newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   public Converter createConverter(String id)
   {
      return convertersById.get(id);
   }

   @Override
   public Converter createConverter(Class clazz)
   {
      return converters.get(clazz);
   }

   @Override
   public Iterator getConverterIds()
   {
      return convertersById.keySet().iterator();
   }

   @Override
   public Iterator getConverterTypes()
   {
      return converters.keySet().iterator();
   }

   @Override
   public MethodBinding createMethodBinding(String expression, Class[] params)
         throws ReferenceSyntaxException
   {
      return new UnifiedELMethodBinding(expression, params);

   }

   @Override
   public ValueBinding createValueBinding(String expression)
         throws ReferenceSyntaxException
   {
      return new UnifiedELValueBinding(expression);
   }

   @Override
   public Iterator getSupportedLocales()
   {
      if (locales == null)
      {
         return  Collections.singleton(defaultLocale).iterator();
      }
      else
      {
         return locales.iterator();
      }
   }

   @Override
   public void setSupportedLocales(Collection locales)
   {
      this.locales = locales;
   }

   private final Map<String, Validator> validatorsById = new HashMap<String, Validator>();
   
   @Override
   public void addValidator(String id, String validatorClass)
   {
      validatorsById.put(id, instantiateValidator(validatorClass));
   }

   private Validator instantiateValidator(String validatorClass)
   {
      try
      {
         return (Validator) Reflections.classForName(validatorClass).newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   @Override
   public Validator createValidator(String id) throws FacesException
   {
      return validatorsById.get(id);
   }

   @Override
   public Iterator getValidatorIds()
   {
      return validatorsById.keySet().iterator();
   }

   @Override
   public ExpressionFactory getExpressionFactory()
   {
      return SeamExpressionFactory.INSTANCE;
   }
   
}
