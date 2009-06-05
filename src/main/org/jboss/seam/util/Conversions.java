package org.jboss.seam.util;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
@SuppressWarnings("serial")
public class Conversions
{
   private static final String EXPRESSION_MARKER = "#{";
   private static final char EXPRESSION_ESCAPE_CHAR = '\\';
   
   private static Map<Class, Converter> converters = new HashMap<Class, Converter>() {{
      put(String.class, new StringConverter());
      put(Boolean.class, new BooleanConverter());
      put(boolean.class, new BooleanConverter());
      put(Integer.class, new IntegerConverter());
      put(int.class, new IntegerConverter());
      put(Long.class, new LongConverter());
      put(long.class, new LongConverter());
      put(Float.class, new FloatConverter());
      put(float.class, new FloatConverter());
      put(Double.class, new DoubleConverter());
      put(double.class, new DoubleConverter());
      put(Character.class, new CharacterConverter());
      put(char.class, new CharacterConverter());
      put(String[].class, new StringArrayConverter());
      put(Set.class, new SetConverter());
      put(List.class, new ListConverter());
      put(Map.class, new MapConverter());
      put(Properties.class, new PropertiesConverter());
      //put(Date.class, new DateTimeConverter());
      //put(Short.class, new ShortConverter());
      //put(Byte.class, new ByteConverter());
      put(Enum.class, new EnumConverter());
      put(BigInteger.class, new BigIntegerConverter());
      put(BigDecimal.class, new BigDecimalConverter());
      put(Class.class, new ClassConverter());
   }};
   
   public static <Y> void putConverter(Class<Y> type, Converter<Y> converter)
   {
      converters.put(type, converter);
   }
   
   public static <Y> Converter<Y> getConverter(Class<Y> clazz)
   {
      Converter<Y> converter = converters.get(clazz);
      if (converter == null && clazz != null && clazz.isEnum())
      {
          converter = converters.get(Enum.class);
      }

      if (converter==null)
      {
          throw new IllegalArgumentException("No converter for type: " + clazz.getName());
      }
      return converter;
   }
   
   public static interface Converter<Z>
   {
      public Z toObject(PropertyValue value, Type type); 
   }
   
   public static class BooleanConverter implements Converter<Boolean>
   {
      public Boolean toObject(PropertyValue value, Type type)
      {
         return Boolean.valueOf( value.getSingleValue() );
      }
   }
   
   public static class IntegerConverter implements Converter<Integer>
   {
      public Integer toObject(PropertyValue value, Type type)
      {
         return Integer.valueOf( value.getSingleValue() );
      }
   }
   
   public static class LongConverter implements Converter<Long>
   {
      public Long toObject(PropertyValue value, Type type)
      {
         return Long.valueOf( value.getSingleValue() );
      }
   }
   
   public static class FloatConverter implements Converter<Float>
   {
      public Float toObject(PropertyValue value, Type type)
      {
         return Float.valueOf( value.getSingleValue() );
      }
   }
   
   public static class DoubleConverter implements Converter<Double>
   {
      public Double toObject(PropertyValue value, Type type)
      {
         return Double.valueOf( value.getSingleValue() );
      }
   }
   
   public static class CharacterConverter implements Converter<Character>
   {
      public Character toObject(PropertyValue value, Type type)
      {
         return value.getSingleValue().charAt(0);
      }
   }
   
   public static class StringConverter implements Converter<String>
   {
      public String toObject(PropertyValue value, Type type)
      {
         return  value.getSingleValue() ;
      }
   }
   public static class BigDecimalConverter implements Converter<BigDecimal>
   {
      public BigDecimal toObject(PropertyValue value, Type type)
      {
         return new BigDecimal(value.getSingleValue());
      }
   }
   public static class BigIntegerConverter implements Converter<BigInteger>
   {
      public BigInteger toObject(PropertyValue value, Type type)
      {
         return new BigInteger(value.getSingleValue());
      }
   }

   public static class EnumConverter implements Converter<Enum<?>>
   {
      public Enum<?> toObject(PropertyValue value, Type type)
      {
         return Enum.valueOf((Class<Enum>) type, value.getSingleValue());
      }
   }

   public static class StringArrayConverter implements Converter<String[]>
   {
      public String[] toObject(PropertyValue values, Type type)
      {
         return values.getMultiValues();
      }
   }
   
   public static class ArrayConverter implements Converter
   {
      public Object toObject(PropertyValue values, Type type)
      {
         String[] strings = values.getMultiValues();
         Class elementType = ( (Class) type ).getComponentType();
         Object objects = Array.newInstance( elementType, strings.length );
         Converter elementConverter = converters.get(elementType);
         for (int i=0; i<strings.length; i++)
         {
            Object element = elementConverter.toObject( new FlatPropertyValue(strings[i]), elementType );
            Array.set( objects, i, element );
         }
         return objects;
      }
   }
   
   public static class SetConverter implements Converter<Set>
   {
      public Set toObject(PropertyValue values, Type type)
      {
         String[] strings = values.getMultiValues();
         Class elementType = Reflections.getCollectionElementType(type);
         Set set = new HashSet(strings.length);
         Converter elementConverter = converters.get(elementType);
         for (int i=0; i<strings.length; i++)
         {
            Object element = elementConverter.toObject( new FlatPropertyValue(strings[i]), elementType );
            set.add(element);
         }
         return set;
      }
   }
   
   public static class ListConverter implements Converter<List>
   {
      public List toObject(PropertyValue values, Type type)
      {
         String[] strings = values.getMultiValues();
         Class elementType = Reflections.getCollectionElementType(type);
         List list = new ArrayList(strings.length);
         Converter elementConverter = converters.get(elementType);
         for (int i=0; i<strings.length; i++)
         {
            list.add( getElementValue( elementType, elementConverter, strings[i] ) );
         }
         return list;
      }

   }
   
   private static Object getElementValue(Class elementType, Converter elementConverter, String string)
   {
      PropertyValue propertyValue = new FlatPropertyValue(string);
      if ( propertyValue.isExpression() )
      {
         throw new IllegalArgumentException("No expressions allowed here");
      }
      if (elementConverter==null)
      {
         throw new IllegalArgumentException("No converter for element type: " + elementType.getName());
      }
      return elementConverter.toObject(propertyValue, elementType);
   }
   
   public static class MapConverter implements Converter<Map>
   {
      public Map toObject(PropertyValue values, Type type)
      {
         Map<String, String> keyedValues = values.getKeyedValues();
         Class elementType = Reflections.getCollectionElementType(type);
         Map map = new HashMap( keyedValues.size() );
         Converter elementConverter = converters.get(elementType);
         for ( Map.Entry<String, String> me: keyedValues.entrySet() )
         {
            map.put( me.getKey(), getElementValue( elementType, elementConverter, me.getValue() ) );
         }
         return map;
      }
   }
   
   public static class PropertiesConverter implements Converter<Properties>
   {
      public Properties toObject(PropertyValue values, Type type)
      {
         Map<String, String> keyedValues = values.getKeyedValues();
         Properties map = new Properties();
         Converter elementConverter = converters.get(String.class);
         for ( Map.Entry<String, String> me: keyedValues.entrySet() )
         {
            String key = me.getKey();
            Object element = elementConverter.toObject( new FlatPropertyValue( me.getValue() ), String.class );
            map.put(key, element);
         }
         return map;
      }
   }
   
   public static class ClassConverter implements Converter<Class>
   {
      public Class toObject(PropertyValue value, Type type)
      {
         try
         {
            return Reflections.classForName( value.getSingleValue() );
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new IllegalArgumentException(cnfe);
         }
      }
   }
  
   public static interface PropertyValue extends Serializable
   {
      Map<String, String> getKeyedValues();
      String[] getMultiValues();
      String getSingleValue();
      boolean isExpression();
      boolean isMultiValued();
      boolean isAssociativeValued();
      Class getType();
   }
   
   public static class FlatPropertyValue implements PropertyValue
   {
      
      private String string;
      public FlatPropertyValue(String string)
      {
         if (string==null)
         {
            throw new IllegalArgumentException("null value");
         }
         this.string = string;
      }
      public String[] getMultiValues()
      {
         return Strings.split(string, ", \r\n\f\t");
      }
      public String getSingleValue()
      {
         return string;
      }
      
      public boolean isExpression()
      {
         boolean containsExpr = false;
         int idx = string.indexOf(EXPRESSION_MARKER);
         if (idx == 0) {
             containsExpr = true;
         }
         else {
             while (idx != -1) {
                 if (string.charAt(idx - 1) == EXPRESSION_ESCAPE_CHAR) {
                     idx = string.indexOf(EXPRESSION_MARKER, idx + 2);
                 }
                 else {
                     containsExpr = true;
                     break;
                 }
             }
         }
         return containsExpr;
      }
      
      public boolean isMultiValued()
      {
         return false;
      }
      
      public boolean isAssociativeValued()
      {
         return false;
      }
      
      public Map<String, String> getKeyedValues()
      {
         throw new UnsupportedOperationException("not a keyed property value");
      }
      
      @Override
      public String toString()
      {
         return string;
      }
      
      public Class getType()
      {
         return null;
      }
      
   }
   
   public static class MultiPropertyValue implements PropertyValue
   {     
      private String[] strings;
      
      private Class type;
      
      public MultiPropertyValue(String[] strings, Class type)
      {
         if (strings==null) throw new IllegalArgumentException();
         this.strings = strings;
         this.type = type;
      }
      
      public String[] getMultiValues()
      {
         return strings;
      }
      
      public String getSingleValue()
      {
         throw new UnsupportedOperationException("not a flat property value");
      }
      
      public Map<String, String> getKeyedValues()
      {
         throw new UnsupportedOperationException("not a keyed property value");
      }
      
      public boolean isMultiValued()
      {
         return true;
      }
      
      public boolean isAssociativeValued()
      {
         return false;
      }
      
      public boolean isExpression()
      {
         return false;
      }
      
      @Override
      public String toString()
      {
         return Strings.toString( ", ", (Object[]) strings );
      }
      
      public Class getType()
      {
         return type;
      }
   }
   
   public static class AssociativePropertyValue implements PropertyValue
   {      
      private Map<String, String> keyedValues;
      
      private Class type;
      
      public AssociativePropertyValue(Map<String, String> keyedValues, Class type)
      {
         if (keyedValues==null) throw new IllegalArgumentException();
         this.keyedValues = keyedValues;
         this.type = type;
      }
      
      public String[] getMultiValues()
      {
         throw new UnsupportedOperationException("not a multi-valued property value");
      }
      
      public String getSingleValue()
      {
         throw new UnsupportedOperationException("not a flat property value");
      }
      
      public Map<String, String> getKeyedValues()
      {
         return keyedValues;
      }
      
      public boolean isExpression()
      {
         return false;
      }
      
      public boolean isMultiValued()
      {
         return false;
      }
      
      public boolean isAssociativeValued()
      {
         return true;
      }
      
      @Override
      public String toString()
      {
         return keyedValues.toString();
      }
      
      
      public Class getType()
      {
         return type;
      }
      
   }
   
}
