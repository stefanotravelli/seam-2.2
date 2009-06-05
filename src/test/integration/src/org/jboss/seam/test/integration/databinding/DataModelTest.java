package org.jboss.seam.test.integration.databinding;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.jboss.seam.faces.DataModels;
import org.jboss.seam.jsf.ArrayDataModel;
import org.jboss.seam.jsf.MapDataModel;
import org.jboss.seam.jsf.SetDataModel;
import org.jboss.seam.mock.SeamTest;
import org.jboss.seam.test.integration.Foo;
import org.jboss.seam.test.integration.Person;
import org.testng.annotations.Test;

public class DataModelTest extends SeamTest
{
   
   @Test
   public void testDataModels() throws Exception
   {
    
      new FacesRequest()
      {
         @Override
         protected void invokeApplication() throws Exception
         {
            DataModels dataModels = DataModels.instance();
            
            assert dataModels.getDataModel(new ArrayList()) instanceof ListDataModel;
            assert dataModels.getDataModel(new HashMap()) instanceof MapDataModel;
            assert dataModels.getDataModel(new HashSet()) instanceof SetDataModel;
            assert dataModels.getDataModel(new Object[0]) instanceof ArrayDataModel;
            // TODO assert dataModels.getDataModel(query) instanceof ListDataModel;
            
            boolean failed = false;
            try
            {
               dataModels.getDataModel(new Foo());
            }
            catch (IllegalArgumentException e)
            {
               failed = true;
            }
            assert failed;
         }
      }.run();
   }
   
   @Test
   public void testArrayDataModelSerialization() throws Exception
   {
      String[] array = {"Seam", "Hibernate"};
      javax.faces.model.ArrayDataModel arrayDataModel = new ArrayDataModel(array);
      arrayDataModel.setRowIndex(1);
      
      Object object = null;
      try
      {
         object = serialize(arrayDataModel);
      }
      catch (NotSerializableException e) 
      {
         assert false;
      }
      assert object instanceof javax.faces.model.ArrayDataModel;
      
      javax.faces.model.ArrayDataModel serializedArrayDataModel = (javax.faces.model.ArrayDataModel) object;
      
      assert serializedArrayDataModel.getRowIndex() == 1;
      
      String[] serializedArray = (String[]) serializedArrayDataModel.getWrappedData();
      
      assert array[0].equals(serializedArray[0]);
      
      assert array[1].equals(serializedArray[1]);
   }
   
   // Utility to serialize an object
   private Object serialize(Object object) throws Exception
   {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(object);  
      ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bis);
      return ois.readObject();
   }
   
   @Test
   public void testListDataModelSerialization() throws Exception
   {
      
      List<String> list = Arrays.asList("Seam", "Hibernate");
      javax.faces.model.ListDataModel listDataModel = new org.jboss.seam.jsf.ListDataModel(list);
      listDataModel.setRowIndex(1);
      
      Object object = null;
      try
      {
         object = serialize(listDataModel);
      }
      catch (NotSerializableException e) 
      {
         assert false;
      }
      
      assert object instanceof javax.faces.model.ListDataModel;
      javax.faces.model.ListDataModel serializedListDataModel = (javax.faces.model.ListDataModel) object;
      List<String> serializedList = (List<String>) serializedListDataModel.getWrappedData();
      
      assert serializedListDataModel.getRowIndex() == 1;
      assert list.get(0).equals(serializedList.get(0)); 
      assert list.get(1).equals(serializedList.get(1));
   }
   
   @Test
   public void testMapDataModel() throws IOException, ClassNotFoundException
   {
      Map<String, Person> map = new HashMap<String, Person>();
      map.put("0", new Person("Gavin"));
      map.put("1", new Person("Tom"));
      
      javax.faces.model.DataModel mapDataModel = new MapDataModel();
      
      assert mapDataModel.getRowCount() == -1;
      assert mapDataModel.getRowData() == null;
      assert !mapDataModel.isRowAvailable();
      
      mapDataModel = new MapDataModel(map);
      
      assert mapDataModel.getWrappedData() instanceof Map;
      
      assert map.get("0").equals(((Map) mapDataModel.getWrappedData()).get("0"));
      assert map.get("1").equals(((Map) mapDataModel.getWrappedData()).get("1"));
      
      mapDataModel.setRowIndex(10);
      
      assert !mapDataModel.isRowAvailable();
      
      boolean failed = false;
      try
      {
         mapDataModel.getRowData();
      }
      catch (IllegalArgumentException e) 
      {
         failed = true;
      }
      
      assert failed;
      
      mapDataModel.setRowIndex(1);
      
      assert mapDataModel.isRowAvailable();
      assert mapDataModel.getRowIndex() == 1;
      assert mapDataModel.getRowCount() == 2;
      
      // JBSEAM-1660
      try 
      {
         mapDataModel.setWrappedData(null);
      }
      catch (NullPointerException e) 
      {
         // Spec allows passing null
         assert false;
      }
   }
   
   /**
    * JBSEAM-1659
    */ 
   @Test
   public void testMapDataModelSerialization() throws Exception
   {
      
      Map<String, Person> map = new HashMap<String, Person>();
      map.put("0", new Person("Gavin"));
      map.put("1", new Person("Tom"));
      
      javax.faces.model.DataModel mapDataModel = new MapDataModel(map);    
      mapDataModel.setRowIndex(1);

      Object object = null;
      try
      {
         object = serialize(mapDataModel);
      }
      catch (NotSerializableException e) 
      {
         assert false;
      }
      
      
      assert object instanceof javax.faces.model.DataModel;
      javax.faces.model.DataModel serializedMapDataModel = (javax.faces.model.DataModel) object;
      Map<String, Person> serializedMap = (Map<String, Person>) serializedMapDataModel.getWrappedData();
      
      assert serializedMapDataModel.getRowIndex() == 1;
      assert map.get("0").equals(serializedMap.get("0")); 
      assert map.get("1").equals(serializedMap.get("1"));
   }
   
   @Test
   public void testSetDataModel() throws IOException, ClassNotFoundException
   {
      Person gavin = new Person("Gavin");
      Person tom = new Person("Tom");
      
      Set<Person> set = new HashSet<Person>();
      set.add(gavin);
      set.add(tom);
      
      javax.faces.model.DataModel setDataModel = new SetDataModel();
      
      assert setDataModel.getRowCount() == -1;
      assert setDataModel.getRowData() == null;
      assert !setDataModel.isRowAvailable();
      
      setDataModel = new SetDataModel(set);
      
      assert setDataModel.getWrappedData() instanceof Set;
      
      assert set.contains(gavin);
      assert set.contains(tom);
      
      setDataModel.setRowIndex(10);
      
      assert !setDataModel.isRowAvailable();
      
      boolean failed = false;
      try
      {
         setDataModel.getRowData();
      }
      catch (IllegalArgumentException e) 
      {
         failed = true;
      }
      
      assert failed;
      
      setDataModel.setRowIndex(1);
      
      assert setDataModel.isRowAvailable();
      assert setDataModel.getRowIndex() == 1;
      assert setDataModel.getRowCount() == 2;
      
      // JBSEAM-1660
      try 
      {
         setDataModel.setWrappedData(null);
      }
      catch (NullPointerException e) 
      {
         // Spec allows passing null
         assert false;
      }
   }
   
   @Test
   public void testSetDataModelSerialization() throws Exception
   {
      
      Person gavin = new Person("Gavin");
      Person tom = new Person("Tom");
      
      Set<Person> set = new HashSet<Person>();
      set.add(gavin);
      set.add(tom);
      
      javax.faces.model.DataModel setDataModel = new SetDataModel(set);    
      setDataModel.setRowIndex(1);

      Object object = null;
      try
      {
         object = serialize(setDataModel);
      }
      catch (NotSerializableException e) 
      {
         assert false;
      }
      
      
      assert object instanceof javax.faces.model.DataModel;
      javax.faces.model.DataModel serializedSetDataModel = (javax.faces.model.DataModel) object;
      Set<Person> serializedSet = (Set<Person>) serializedSetDataModel.getWrappedData();
      
      assert serializedSetDataModel.getRowIndex() == 1;
      assert serializedSet.contains(gavin);
      assert serializedSet.contains(tom);
   }
   
   @Test
   public void testDataModelOutjection() throws Exception
   {
      new FacesRequest()
      {
         
         @Override
         protected void renderResponse() throws Exception
         {
            Object people = getValue("#{peopleList}");
            assert people instanceof DataModel;
            DataModel dataModel = (DataModel) people;
            assert dataModel.getRowCount() == 4;
            dataModel.setRowIndex(1);
         }     
         
      }.run();
      
   }

}
