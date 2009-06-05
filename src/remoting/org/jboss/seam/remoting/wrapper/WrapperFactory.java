package org.jboss.seam.remoting.wrapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Shane Bryzak
 */
public class WrapperFactory
{
  /**
   * Singleton instance.
   */
  private static final WrapperFactory factory = new WrapperFactory();

  /**
   * A registry of wrapper types
   */
  private Map<String,Class> wrapperRegistry = new HashMap<String,Class>();

  private Map<Class,Class> classRegistry = new HashMap<Class,Class>();

  /**
   * Private constructor
   */
  private WrapperFactory()
  {
    // Register the defaults
    registerWrapper("str", StringWrapper.class);
    registerWrapper("bool", BooleanWrapper.class);
    registerWrapper("bean", BeanWrapper.class);
    registerWrapper("number", NumberWrapper.class);
    registerWrapper("null", NullWrapper.class);
    registerWrapper("bag", BagWrapper.class);
    registerWrapper("map", MapWrapper.class);
    registerWrapper("date", DateWrapper.class);

    // String types
    registerWrapperClass(String.class, StringWrapper.class);
    registerWrapperClass(StringBuilder.class, StringWrapper.class);
    registerWrapperClass(StringBuffer.class, StringWrapper.class);
    registerWrapperClass(Character.class, StringWrapper.class);

    // Big numbers are handled by StringWrapper
    registerWrapperClass(BigDecimal.class, StringWrapper.class);
    registerWrapperClass(BigInteger.class, StringWrapper.class);

    // Number types
    registerWrapperClass(Integer.class, NumberWrapper.class);
    registerWrapperClass(Long.class, NumberWrapper.class);
    registerWrapperClass(Short.class, NumberWrapper.class);
    registerWrapperClass(Double.class, NumberWrapper.class);
    registerWrapperClass(Float.class, NumberWrapper.class);
    registerWrapperClass(Byte.class, NumberWrapper.class);
  }

  public void registerWrapper(String type, Class wrapperClass)
  {
    wrapperRegistry.put(type, wrapperClass);
  }

  public void registerWrapperClass(Class cls, Class wrapperClass)
  {
    classRegistry.put(cls, wrapperClass);
  }

  public static WrapperFactory getInstance()
  {
    return factory;
  }

  public Wrapper createWrapper(String type)
  {
    Class wrapperClass = wrapperRegistry.get(type);

    if (wrapperClass != null)
    {
      try {
        Wrapper wrapper = (Wrapper) wrapperClass.newInstance();
        return wrapper;
      }
      catch (Exception ex) { }
    }

    throw new RuntimeException(String.format("Failed to create wrapper for type: %s",
                               type));
  }

  public Wrapper getWrapperForObject(Object obj)
  {
    if (obj == null)
      return new NullWrapper();

    Wrapper w = null;

    if (Map.class.isAssignableFrom(obj.getClass()))
      w = new MapWrapper();
    else if (obj.getClass().isArray() || Collection.class.isAssignableFrom(obj.getClass()))
      w = new BagWrapper();
    else if (obj.getClass().equals(Boolean.class) || obj.getClass().equals(Boolean.TYPE))
      w = new BooleanWrapper();
    else if (obj.getClass().isEnum())
      w = new StringWrapper();
    else if (Date.class.isAssignableFrom(obj.getClass()) || Calendar.class.isAssignableFrom(obj.getClass()))
      w = new DateWrapper();
    else if (classRegistry.containsKey(obj.getClass()))
    {
      try
      {
        w = (Wrapper) classRegistry.get(obj.getClass()).newInstance();
      }
      catch (Exception ex)
      {
        throw new RuntimeException("Failed to create wrapper instance.");
      }
    }
    else
      w = new BeanWrapper();

    w.setValue(obj);
    return w;
  }
}
