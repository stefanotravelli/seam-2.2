/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.test.unit;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.mock.MockServletContext;
import org.jboss.seam.remoting.Call;
import org.jboss.seam.remoting.CallContext;
import org.jboss.seam.remoting.InterfaceGenerator;
import org.jboss.seam.remoting.MarshalUtils;
import org.jboss.seam.remoting.client.ParserUtils;
import org.jboss.seam.remoting.wrapper.BagWrapper;
import org.jboss.seam.remoting.wrapper.BaseWrapper;
import org.jboss.seam.remoting.wrapper.BeanWrapper;
import org.jboss.seam.remoting.wrapper.BooleanWrapper;
import org.jboss.seam.remoting.wrapper.ConversionException;
import org.jboss.seam.remoting.wrapper.ConversionScore;
import org.jboss.seam.remoting.wrapper.DateWrapper;
import org.jboss.seam.remoting.wrapper.MapWrapper;
import org.jboss.seam.remoting.wrapper.NullWrapper;
import org.jboss.seam.remoting.wrapper.NumberWrapper;
import org.jboss.seam.remoting.wrapper.StringWrapper;
import org.jboss.seam.remoting.wrapper.WrapperFactory;
import org.testng.annotations.Test;

/**
 * Unit tests for Seam Remoting
 * 
 * @author Shane Bryzak
 */
public class RemotingTest
{
   private class InvalidClass
   {
   }

   public Element createElement(String name, String value)
         throws UnsupportedEncodingException
   {
      Element e = DocumentFactory.getInstance().createElement(name);
      if (value != null)
         e.addText(URLEncoder.encode(value, StringWrapper.DEFAULT_ENCODING));
      return e;
   }

   private enum TestEnum
   {
      red, green, blue
   }

   @Test
   public void testBooleanWrapper() throws Exception
   {
      BooleanWrapper wrapper = new BooleanWrapper();
      wrapper.setElement(createElement("bool", Boolean.toString(true)));

      assert (Boolean) wrapper.convert(Boolean.class);
      assert (Boolean) wrapper.convert(Boolean.TYPE);

      try
      {
         // Try an invalid conversion
         wrapper.convert(InvalidClass.class);
         assert false;
      }
      catch (ConversionException ex)
      {
      }

      // test the marshal() method
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      wrapper.marshal(out);
      byte[] expected = ("<bool>" + Boolean.toString(true) + "</bool>")
            .getBytes();
      assertEquals(expected, out.toByteArray());

      // test the conversionScore() method
      assert ConversionScore.exact == wrapper.conversionScore(Boolean.class);
      assert ConversionScore.exact == wrapper.conversionScore(Boolean.TYPE);
      assert ConversionScore.compatible == wrapper
            .conversionScore(Object.class);
      assert ConversionScore.nomatch == wrapper
            .conversionScore(InvalidClass.class);
   }

   @Test
   public void testDateWrapper() throws Exception
   {
      DateWrapper wrapper = new DateWrapper();
      String value = "20061231123045150";
      wrapper.setElement(createElement("date", value));

      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.YEAR, 2006);
      cal.set(Calendar.MONTH, Calendar.DECEMBER);
      cal.set(Calendar.DATE, 31);
      cal.set(Calendar.HOUR_OF_DAY, 12);
      cal.set(Calendar.MINUTE, 30);
      cal.set(Calendar.SECOND, 45);
      cal.set(Calendar.MILLISECOND, 150);

      assertEquals(cal.getTime(), wrapper.convert(Date.class));

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      wrapper.marshal(out);

      byte[] expected = ("<date>" + value + "</date>").getBytes();
      assertEquals(expected, out.toByteArray());

      try
      {
         // this should throw an exception
         wrapper.convert(InvalidClass.class);
         assert false;
      }
      catch (ConversionException ex)
      {
      }

      try
      {
         // this should throw an exception
         wrapper.setElement(createElement("date", "foobar"));
         wrapper.convert(Date.class);
         assert false;
      }
      catch (ConversionException ex)
      {
      }

      // test conversionScore() method
      assert ConversionScore.exact == wrapper.conversionScore(Date.class);
      assert ConversionScore.exact == wrapper
            .conversionScore(java.sql.Date.class);
      assert ConversionScore.compatible == wrapper
            .conversionScore(Object.class);
      assert ConversionScore.nomatch == wrapper
            .conversionScore(InvalidClass.class);
   }

   @Test
   public void testStringWrapper() throws Exception
   {
      String value = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            + "!@#$%^&*()_+-=`~[]{}|;:\"',./<>?\\ ";
      StringWrapper wrapper = new StringWrapper();
      wrapper.setElement(createElement("str", value));

      assert value.equals(wrapper.convert(String.class));
      assert value
            .equals(((StringBuilder) wrapper.convert(StringBuilder.class))
                  .toString());
      assert value.equals(((StringBuffer) wrapper.convert(StringBuffer.class))
            .toString());

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      wrapper.marshal(out);
      byte[] expected = ("<str>"
            + URLEncoder.encode(value, StringWrapper.DEFAULT_ENCODING) + "</str>")
            .replace("+", "%20").getBytes();

      assertEquals(expected, out.toByteArray());

      value = "123";
      wrapper.setElement(createElement("str", value));

      assert Integer.valueOf(value).equals(wrapper.convert(Integer.class));
      assert Integer.valueOf(value).equals(wrapper.convert(Integer.TYPE));
      assert Long.valueOf(value).equals(wrapper.convert(Long.class));
      assert Long.valueOf(value).equals(wrapper.convert(Long.TYPE));
      assert Short.valueOf(value).equals(wrapper.convert(Short.class));
      assert Short.valueOf(value).equals(wrapper.convert(Short.TYPE));
      assert Byte.valueOf(value).equals(wrapper.convert(Byte.class));
      assert Byte.valueOf(value).equals(wrapper.convert(Byte.TYPE));

      value = "123.4567";
      wrapper.setElement(createElement("str", value));

      assert Double.valueOf(value).equals(wrapper.convert(Double.class));
      assert Double.valueOf(value).equals(wrapper.convert(Double.TYPE));
      assert Float.valueOf(value).equals(wrapper.convert(Float.class));
      assert Float.valueOf(value).equals(wrapper.convert(Float.TYPE));

      wrapper.setElement(createElement("str", Boolean.toString(true)));
      assert (Boolean) wrapper.convert(Boolean.class);
      assert (Boolean) wrapper.convert(Boolean.TYPE);

      wrapper.setElement(createElement("str", Boolean.toString(false)));
      assert !(Boolean) wrapper.convert(Boolean.class);
      assert !(Boolean) wrapper.convert(Boolean.TYPE);

      try
      {
         // Attempt an invalid conversion
         wrapper.convert(Integer.class);
         assert false;
      }
      catch (ConversionException ex)
      {
      }

      value = "A";
      wrapper.setElement(createElement("str", value));

      assert Character.valueOf(value.charAt(0)).equals(
            wrapper.convert(Character.class));
      assert Character.valueOf(value.charAt(0)).equals(
            wrapper.convert(Character.TYPE));

      value = "green";
      wrapper.setElement(createElement("str", value));

      assert TestEnum.valueOf(value).equals(wrapper.convert(TestEnum.class));

      try
      {
         wrapper.setElement(createElement("str", "foo"));
         // Attempt an invalid conversion
         wrapper.convert(InvalidClass.class);
         assert false;
      }
      catch (ConversionException ex)
      {
      }

      // Test conversionScore() method
      wrapper = new StringWrapper();

      assert ConversionScore.exact == wrapper.conversionScore(String.class);
      assert ConversionScore.exact == wrapper
            .conversionScore(StringBuffer.class);
      assert ConversionScore.exact == wrapper
            .conversionScore(StringBuffer.class);
      assert ConversionScore.compatible == wrapper
            .conversionScore(Integer.class);
      assert ConversionScore.compatible == wrapper
            .conversionScore(Integer.TYPE);
      assert ConversionScore.compatible == wrapper.conversionScore(Long.class);
      assert ConversionScore.compatible == wrapper.conversionScore(Long.TYPE);
      assert ConversionScore.compatible == wrapper.conversionScore(Short.class);
      assert ConversionScore.compatible == wrapper.conversionScore(Short.TYPE);
      assert ConversionScore.compatible == wrapper
            .conversionScore(Boolean.class);
      assert ConversionScore.compatible == wrapper
            .conversionScore(Boolean.TYPE);
      assert ConversionScore.compatible == wrapper.conversionScore(Float.class);
      assert ConversionScore.compatible == wrapper.conversionScore(Float.TYPE);
      assert ConversionScore.compatible == wrapper
            .conversionScore(Character.class);
      assert ConversionScore.compatible == wrapper
            .conversionScore(Character.TYPE);
      assert ConversionScore.compatible == wrapper.conversionScore(Byte.class);
      assert ConversionScore.compatible == wrapper.conversionScore(Byte.TYPE);

      wrapper.setElement(createElement("str", "green"));
      assert ConversionScore.compatible == wrapper
            .conversionScore(TestEnum.class);
      wrapper.setElement(createElement("str", "foo"));
      assert ConversionScore.nomatch == wrapper.conversionScore(TestEnum.class);

      assert ConversionScore.nomatch == wrapper
            .conversionScore(InvalidClass.class);
   }

   @Test
   public void testNumberWrapper() throws Exception
   {
      String value = "123";

      NumberWrapper wrapper = new NumberWrapper();
      wrapper.setElement(createElement("number", value));

      assert new Short(value).equals(wrapper.convert(Short.class));
      assert wrapper.convert(Short.TYPE).equals(Short.parseShort(value));

      assert new Integer(value).equals(wrapper.convert(Integer.class));
      assert wrapper.convert(Integer.TYPE).equals(Integer.parseInt(value));

      assert new Long(value).equals(wrapper.convert(Long.class));
      assert wrapper.convert(Long.TYPE).equals(Long.parseLong(value));

      assert new Byte(value).equals(wrapper.convert(Byte.class));
      assert wrapper.convert(Byte.TYPE).equals(Byte.parseByte(value));

      assert value.equals(wrapper.convert(String.class));

      value = "123.456";
      wrapper.setElement(createElement("number", value));

      assert new Double(value).equals(wrapper.convert(Double.class));
      assert Double.valueOf(value).equals(wrapper.convert(Double.TYPE));

      assert new Float(value).equals(wrapper.convert(Float.class));
      assert Float.valueOf(value).equals(wrapper.convert(Float.TYPE));

      value = "";
      wrapper.setElement(createElement("number", value));

      assert null == wrapper.convert(Short.class);
      assert null == wrapper.convert(Integer.class);
      assert null == wrapper.convert(Long.class);
      assert null == wrapper.convert(Float.class);
      assert null == wrapper.convert(Double.class);
      assert null == wrapper.convert(Byte.class);

      try
      {
         // Attempt an invalid conversion
         wrapper.convert(InvalidClass.class);
         assert false;
      }
      catch (ConversionException ex)
      {
      }

      assert ConversionScore.exact == wrapper.conversionScore(Integer.class);
      assert ConversionScore.exact == wrapper.conversionScore(Integer.TYPE);

      assert ConversionScore.compatible == wrapper
            .conversionScore(String.class);
      assert ConversionScore.compatible == wrapper
            .conversionScore(Object.class);

      assert ConversionScore.nomatch == wrapper
            .conversionScore(InvalidClass.class);
   }

   /**
    * Dummy method used in testBagWrapper()
    * 
    * @return List
    */
   public List<String> dummy()
   {
      return null;
   }

   public InvalidList<String> dummyInvalid()
   {
      return null;
   }

   /**
    * Used in testBagWrapper()
    */
   @SuppressWarnings("serial")
   private class InvalidList<E> extends ArrayList<E>
   {
      @SuppressWarnings("unused")
      public InvalidList()
      {
         throw new InstantiationError();
      }
   }

   @Test
   public void testBagWrapper() throws Exception
   {
      BagWrapper wrapper = new BagWrapper();
      wrapper.setCallContext(new CallContext());

      String[] values = new String[2];
      values[0] = "foo";
      values[1] = "bar";

      Element e = createElement("bag", null);
      e.addElement("element").addElement("str").addText(values[0]);
      e.addElement("element").addElement("str").addText(values[1]);
      wrapper.setElement(e);

      // String Array
      String[] converted = (String[]) wrapper.convert(String[].class);
      assert values.length == converted.length;
      assertEquals(values[0], converted[0]);
      assertEquals(values[1], converted[1]);

      // List
      List convertedList = (List) wrapper.convert(List.class);
      assert values.length == convertedList.size();
      assertEquals(values[0], convertedList.get(0));
      assertEquals(values[1], convertedList.get(1));

      // List<String> (Generic type)

      // Isn't there an easier way of getting a generic type than this??
      Type t = RemotingTest.class.getMethod("dummy").getGenericReturnType();

      List<String> stringList = (List<String>) wrapper.convert(t);
      assert values.length == stringList.size();
      assertEquals(values[0], stringList.get(0));
      assertEquals(values[1], stringList.get(1));

      // Set
      Set s = (Set) wrapper.convert(Set.class);
      assert values.length == s.size();
      assert s.contains(values[0]);
      assert s.contains(values[1]);

      // Queue
      Queue q = (Queue) wrapper.convert(Queue.class);
      assert values.length == q.size();
      assert q.contains(values[0]);
      assert q.contains(values[1]);

      // Concrete class
      ArrayList l = (ArrayList) wrapper.convert(ArrayList.class);
      assert values.length == l.size();
      assertEquals(values[0], l.get(0));
      assertEquals(values[1], l.get(1));

      try
      {
         // This should throw a ConversionException
         wrapper.convert(InvalidList.class);
         assert false;
      }
      catch (ConversionException ex)
      {
      }

      t = RemotingTest.class.getMethod("dummyInvalid").getGenericReturnType();
      try
      {
         // This should throw a ConversionException also
         wrapper.convert(t);
         assert false;
      }
      catch (ConversionException ex)
      {
      }

      int[] intValues = new int[2];
      intValues[0] = 44444;
      intValues[1] = 55555;

      e = createElement("bag", null);
      e.addElement("element").addElement("number").addText("" + intValues[0]);
      e.addElement("element").addElement("number").addText("" + intValues[1]);
      wrapper.setElement(e);

      // int Array
      int[] convertedInts = (int[]) wrapper.convert(int[].class);
      assert intValues.length == convertedInts.length;
      assertEquals(intValues[0], convertedInts[0]);
      assertEquals(intValues[1], convertedInts[1]);

      // Test marshal()

      byte[] expected = ("<bag><element><number>" + intValues[0]
            + "</number></element>" + "<element><number>" + intValues[1] + "</number></element></bag>")
            .getBytes();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      wrapper.marshal(out);
      assertEquals(expected, out.toByteArray());

      List intList = new ArrayList();
      intList.add(intValues[0]);
      intList.add(intValues[1]);
      wrapper.setValue(intList);
      out.reset();
      wrapper.marshal(out);
      assertEquals(expected, out.toByteArray());

      try
      {
         // This should throw a RuntimeException
         wrapper.setValue(new InvalidClass());
         wrapper.marshal(out);
         assert false;
      }
      catch (RuntimeException ex)
      {
      }

      // test conversionScore() method
      assert ConversionScore.compatible == wrapper
            .conversionScore(String[].class);
      assert ConversionScore.compatible == wrapper
            .conversionScore(Object.class);
      assert ConversionScore.compatible == wrapper
            .conversionScore(Collection.class);
      assert ConversionScore.nomatch == wrapper
            .conversionScore(InvalidClass.class);
   }

   @Test
   public void testBeanWrapper()
   {
      // BeanWrapper wrapper = new BeanWrapper();

      /** @todo Write tests for BeanWrapper */
   }

   /**
    * Used in testMapWrapper()
    */
   public Map<String, String> dummyMap()
   {
      return null;
   }

   /**
    * Used in testMapWrapper()
    */
   @SuppressWarnings("serial")
   private class InvalidMap extends HashMap
   {
      @SuppressWarnings("unused")
      public InvalidMap()
      {
         throw new InstantiationError();
      }
   }

   @Test
   public void testMapWrapper() throws Exception
   {
      MapWrapper wrapper = new MapWrapper();
      wrapper.setCallContext(new CallContext());

      // Construct a map with two elements, foo:aaaaa and bar:zzzzz
      Element e = DocumentFactory.getInstance().createElement("map");
      Element child = e.addElement("element");
      child.addElement("k").addElement("str").addText("foo");
      child.addElement("v").addElement("str").addText("aaaaa");
      child = e.addElement("element");
      child.addElement("k").addElement("str").addText("bar");
      child.addElement("v").addElement("str").addText("zzzzz");

      wrapper.setElement(e);

      // Conversion tests
      Map m = (Map) wrapper.convert(Map.class);
      assert m.containsKey("foo");
      assert m.containsKey("bar");
      assert "aaaaa".equals(m.get("foo"));
      assert "zzzzz".equals(m.get("bar"));

      m = (Map) wrapper.convert(HashMap.class);
      assert m.containsKey("foo");
      assert m.containsKey("bar");
      assert "aaaaa".equals(m.get("foo"));
      assert "zzzzz".equals(m.get("bar"));

      Type t = RemotingTest.class.getMethod("dummyMap").getGenericReturnType();
      m = (Map) wrapper.convert(t);
      assert m.containsKey("foo");
      assert m.containsKey("bar");
      assert "aaaaa".equals(m.get("foo"));
      assert "zzzzz".equals(m.get("bar"));

      try
      {
         // This should throw a ConversionException
         wrapper.convert(InvalidClass.class);
         assert false;
      }
      catch (ConversionException ex)
      {
      }

      try
      {
         // This should throw a ConversionException also
         wrapper.convert(InvalidMap.class);
         assert false;
      }
      catch (ConversionException ex)
      {
      }

      // ensure when we marshal/unmarshal the values in the map remain the same      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      wrapper.marshal(out);
      
      SAXReader xmlReader = new SAXReader();
      Document doc = xmlReader.read( new ByteArrayInputStream(out.toByteArray()) );     
      
      Element root = doc.getRootElement();      
      
      MapWrapper other = new MapWrapper();
      other.setCallContext(new CallContext());
      other.setElement(root);
      
      Map otherMap = (Map) other.convert(Map.class);
      for (Object key : otherMap.keySet())
      {
         assert otherMap.get(key).equals(m.get(key));
      }      

      // test conversionScore() method
      assert ConversionScore.exact == wrapper.conversionScore(Map.class);
      assert ConversionScore.exact == wrapper.conversionScore(HashMap.class);
      assert ConversionScore.compatible == wrapper
            .conversionScore(Object.class);
      assert ConversionScore.nomatch == wrapper
            .conversionScore(InvalidClass.class);
   }

   @Test
   public void testNullWrapper() throws Exception
   {
      NullWrapper wrapper = new NullWrapper();
      assert wrapper.convert(null) == null;

      byte[] expected = "<null/>".getBytes();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      wrapper.marshal(out);
      assertEquals(expected, out.toByteArray());

      assert ConversionScore.compatible == wrapper.conversionScore(null);
      assert ConversionScore.compatible == wrapper
            .conversionScore(Object.class);
   }

   @Test
   public void testBaseWrapper()
   {
      BaseWrapper wrapper = new BaseWrapper()
      {
         public ConversionScore conversionScore(Class cls)
         {
            return ConversionScore.nomatch;
         }

         public void marshal(OutputStream out)
         {
         }

         public Object convert(Type type)
         {
            return null;
         }
      };

      Object value = new Object();
      wrapper.setValue(value);

      assert value.equals(wrapper.getValue());

      // For code-coverage completeness
      wrapper.unmarshal();
      wrapper.setCallContext(null);
      try
      {
         wrapper.serialize(null);
      }
      catch (IOException ex)
      {
      }
   }

   @Test
   public void testWrapperFactory()
   {
      try
      {
         // This should throw a RuntimeException
         WrapperFactory.getInstance().createWrapper("invalid");
         assert false;
      }
      catch (RuntimeException ex)
      {
      }

      assert WrapperFactory.getInstance().getWrapperForObject(null) instanceof NullWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(new HashMap()) instanceof MapWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(new String[2]) instanceof BagWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(new ArrayList()) instanceof BagWrapper;
      assert WrapperFactory.getInstance()
            .getWrapperForObject(new Boolean(true)) instanceof BooleanWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(true) instanceof BooleanWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(TestEnum.blue) instanceof StringWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(new Date()) instanceof DateWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(new Object()) instanceof BeanWrapper;

      // All the String types
      assert WrapperFactory.getInstance().getWrapperForObject("foo") instanceof StringWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(
            new StringBuffer("foo")) instanceof StringWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(
            new StringBuilder("foo")) instanceof StringWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(
            new Character('a')) instanceof StringWrapper;

      // All the number types
      assert WrapperFactory.getInstance().getWrapperForObject(111) instanceof NumberWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(111) instanceof NumberWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(111) instanceof NumberWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(123.456) instanceof NumberWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(123.456) instanceof NumberWrapper;
      assert WrapperFactory.getInstance().getWrapperForObject(111) instanceof NumberWrapper;

   }

   @Test
   public void testConversionScore()
   {
      // For code-coverage completeness
      assert 0 == ConversionScore.nomatch.getScore();
      assert 1 == ConversionScore.compatible.getScore();
      assert 2 == ConversionScore.exact.getScore();
      ConversionScore.valueOf("exact");
   }

   @Test
   public void testConstraints() throws Exception
   {
      // Initialize Seam
      MockServletContext servletContext = new MockServletContext();
      ServletLifecycle.beginApplication(servletContext);
      new Initialization(servletContext).create().init();

      try
      {
         Lifecycle.beginCall();

         // Create our object graph
         Widget result = new Widget();
         result.setValue("foo");
         result.setSecret("bar");

         ByteArrayOutputStream out = new ByteArrayOutputStream();
         
         // Constrain a single field of the result
         Call c = new Call(null, null, null);
         c.setConstraints(Arrays.asList(new String[] { "secret" }));
         c.setResult(result);
         MarshalUtils.marshalResult(c, out);

         SAXReader xmlReader = new SAXReader();
         Document doc = xmlReader.read(new StringReader(new String(out
               .toByteArray())));

         Widget widget = (Widget) ParserUtils.unmarshalResult(doc
               .getRootElement());

         // value field should equal "foo"
         assert "foo".equals(widget.getValue());

         // secret field should be null
         assert widget.getSecret() == null;

         // Now extend our object graph a little further
         result.setChild(new Widget());
         result.getChild().setValue("foo");
         result.getChild().setSecret("bar");

         // Reset our output stream so we can re-use it
         out.reset();

         // Now we're going to constrain result.child's secret field
         c.setConstraints(Arrays.asList(new String[] { "child.secret" }));
         MarshalUtils.marshalResult(c, out);

         doc = xmlReader.read(new StringReader(new String(out.toByteArray())));
         widget = (Widget) ParserUtils.unmarshalResult(doc.getRootElement());
         assert "foo".equals(widget.getValue());
         assert "bar".equals(widget.getSecret());
         assert "foo".equals(widget.getChild().getValue());
         assert widget.getChild().getSecret() == null;

         // Add a map to our result
         result.setWidgetMap(new HashMap<String, Widget>());
         Widget val = new Widget();
         val.setValue("foo");
         val.setSecret("bar");
         result.getWidgetMap().put("foo", val);

         // Reset our output stream again
         out.reset();

         // Constrain the "secret" field of the widgetMap map's values (sounds
         // confusing, I know...)
         c.setConstraints(Arrays.asList(new String[] { "widgetMap[value].secret" }));
         MarshalUtils.marshalResult(c, out);

         doc = xmlReader.read(new StringReader(new String(out.toByteArray())));
         widget = (Widget) ParserUtils.unmarshalResult(doc.getRootElement());
         val = widget.getWidgetMap().get("foo");
         assert val != null;
         assert "foo".equals(val.getValue());
         assert val.getSecret() == null;

         // Reset our output stream
         out.reset();

         // Add a list to our result
         result.setWidgetList(new ArrayList<Widget>());
         Widget item = new Widget();
         item.setValue("foo");
         item.setSecret("bar");
         result.getWidgetList().add(item);

         // Constraint the "secret" field of widgetList
         c.setConstraints(Arrays.asList(new String[] { "widgetList.secret" }));
         MarshalUtils.marshalResult(c, out);

         doc = xmlReader.read(new StringReader(new String(out.toByteArray())));
         widget = (Widget) ParserUtils.unmarshalResult(doc.getRootElement());
         item = widget.getWidgetList().get(0);
         assert item != null;
         assert "foo".equals(item.getValue());
         assert item.getSecret() == null;

         // Reset our output stream
         out.reset();

         // Now constrain all secrets
         c.setConstraints(Arrays.asList(new String[] { "[" + Widget.class.getName() + "].secret" }));
         MarshalUtils.marshalResult(c, out);

         doc = xmlReader.read(new StringReader(new String(out.toByteArray())));
         widget = (Widget) ParserUtils.unmarshalResult(doc.getRootElement());

         val = widget.getWidgetMap().get("foo");
         item = widget.getWidgetList().get(0);

         assert "foo".equals(widget.getValue());
         assert "foo".equals(widget.getChild().getValue());
         assert "foo".equals(val.getValue());
         assert "foo".equals(item.getValue());

         assert widget.getSecret() == null;
         assert widget.getChild().getSecret() == null;
         assert val.getSecret() == null;
         assert item.getSecret() == null;
      }
      finally
      {
         Lifecycle.endCall();
      }
   }

   class ProxyInterfaceGenerator extends InterfaceGenerator
   {
      @Override
      public String getFieldType(Type type)
      {
         return super.getFieldType(type);
      }
   }

   static class Dummy
   {
      enum TestEnum
      {
         foo, bar
      }

      public String getString()
      {
         return null;
      }

      public TestEnum getEnum()
      {
         return null;
      }

      public BigInteger getBigInteger()
      {
         return null;
      }

      public BigDecimal getBigDecimal()
      {
         return null;
      }

      public Boolean getBoolean()
      {
         return null;
      }

      public boolean getBool()
      {
         return false;
      }

      public Short getShort()
      {
         return null;
      }

      public short getSht()
      {
         return 0;
      }

      public Integer getInteger()
      {
         return null;
      }

      public int getInt()
      {
         return 0;
      }

      public Long getLong()
      {
         return null;
      }

      public long getLng()
      {
         return 0;
      }

      public Float getFloat()
      {
         return null;
      }

      public float getFlt()
      {
         return 0;
      }

      public Double getDouble()
      {
         return null;
      }

      public double getDbl()
      {
         return 0;
      }

      public Byte getByte()
      {
         return null;
      }

      public byte getByt()
      {
         return 0;
      }

      public Date getDate()
      {
         return null;
      }

      public int[] getIntArray()
      {
         return null;
      }

      public Map getMap()
      {
         return null;
      }

      public Collection getCollection()
      {
         return null;
      }

      public Map<String, String> getGenericMap()
      {
         return null;
      }

      public Collection<String> getGenericCollection()
      {
         return null;
      }
   }

   private Type getDummyReturnType(String methodName)
   {
      try
      {
         return Dummy.class.getMethod(methodName).getGenericReturnType();
      }
      catch (NoSuchMethodException ex)
      {
         return null;
      }
   }

   /**
    * Test that the correct remoting type is returned for various Java types
    */
   @Test
   public void testInterfaceGenerator()
   {
      ProxyInterfaceGenerator gen = new ProxyInterfaceGenerator();
      assert ("str".equals(gen.getFieldType(getDummyReturnType("getString"))));
      assert ("str".equals(gen.getFieldType(getDummyReturnType("getEnum"))));
      assert ("str".equals(gen
            .getFieldType(getDummyReturnType("getBigInteger"))));
      assert ("str".equals(gen
            .getFieldType(getDummyReturnType("getBigDecimal"))));
      assert ("bool".equals(gen.getFieldType(getDummyReturnType("getBoolean"))));
      assert ("bool".equals(gen.getFieldType(getDummyReturnType("getBool"))));
      assert ("number".equals(gen.getFieldType(getDummyReturnType("getShort"))));
      assert ("number".equals(gen.getFieldType(getDummyReturnType("getSht"))));
      assert ("number".equals(gen
            .getFieldType(getDummyReturnType("getInteger"))));
      assert ("number".equals(gen.getFieldType(getDummyReturnType("getInt"))));
      assert ("number".equals(gen.getFieldType(getDummyReturnType("getLong"))));
      assert ("number".equals(gen.getFieldType(getDummyReturnType("getLng"))));
      assert ("number".equals(gen.getFieldType(getDummyReturnType("getFloat"))));
      assert ("number".equals(gen.getFieldType(getDummyReturnType("getFlt"))));
      assert ("number"
            .equals(gen.getFieldType(getDummyReturnType("getDouble"))));
      assert ("number".equals(gen.getFieldType(getDummyReturnType("getDbl"))));
      assert ("number".equals(gen.getFieldType(getDummyReturnType("getByte"))));
      assert ("number".equals(gen.getFieldType(getDummyReturnType("getByt"))));
      assert ("date".equals(gen.getFieldType(getDummyReturnType("getDate"))));
      assert ("bag".equals(gen.getFieldType(getDummyReturnType("getIntArray"))));
      assert ("map".equals(gen.getFieldType(getDummyReturnType("getMap"))));
      assert ("bag".equals(gen
            .getFieldType(getDummyReturnType("getCollection"))));
      assert ("map".equals(gen
            .getFieldType(getDummyReturnType("getGenericMap"))));
      assert ("bag".equals(gen
            .getFieldType(getDummyReturnType("getGenericCollection"))));
   }
}
