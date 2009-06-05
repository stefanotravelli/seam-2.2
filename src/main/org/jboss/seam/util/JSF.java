package org.jboss.seam.util;

import java.lang.reflect.Method;

public class JSF
{
   public static final Class PHASE_ID;
   public static final Class FACES_EVENT;
   public static final Class DATA_MODEL;
   public static final Class VALIDATOR_EXCEPTION;
   public static final Class CONVERTER_EXCEPTION;
   public static final Method GET_WRAPPED_DATA;
   public static final Method SET_WRAPPED_DATA;
   public static final Method GET_ROW_COUNT;
   
   public static final Object RESTORE_VIEW;
   public static final Object UPDATE_MODEL_VALUES;
   public static final Object INVOKE_APPLICATION;
   public static final Object RENDER_RESPONSE;
   
   static class Dummy {}
   
   static
   {
      PHASE_ID = classForName("javax.faces.event.PhaseId");
      FACES_EVENT = classForName("javax.faces.event.FacesEvent");
      DATA_MODEL = classForName("javax.faces.model.DataModel");
      VALIDATOR_EXCEPTION = classForName("javax.faces.validator.ValidatorException");
      CONVERTER_EXCEPTION = classForName("javax.faces.convert.ConverterException");
      GET_WRAPPED_DATA = methodForName(DATA_MODEL, "getWrappedData");
      SET_WRAPPED_DATA = methodForName(DATA_MODEL, "setWrappedData", Object.class);
      GET_ROW_COUNT = methodForName(DATA_MODEL, "getRowCount");
      
      RESTORE_VIEW = constantValueForName(PHASE_ID, "RESTORE_VIEW");
      UPDATE_MODEL_VALUES = constantValueForName(PHASE_ID, "UPDATE_MODEL_VALUES");
      INVOKE_APPLICATION = constantValueForName(PHASE_ID, "INVOKE_APPLICATION");
      RENDER_RESPONSE = constantValueForName(PHASE_ID, "RENDER_RESPONSE");
   }

   private static Class classForName(String name)
   {
      try
      {
         return Reflections.classForName(name);
      }
      catch (ClassNotFoundException cnfe)
      {
         return Dummy.class;
      }
   }
   
   private static Method methodForName(Class clazz, String name, Class... paramTypes)
   {
      if (clazz.equals(Dummy.class)) return null;
      try
      {
         return clazz.getDeclaredMethod(name, paramTypes);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private static Object constantValueForName(Class clazz, String name)
   {
      if (clazz.equals(Dummy.class)) return null;
      try
      {
         return clazz.getField(name).get(null);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public static int getRowCount(Object dataModel)
   {
      return (Integer) Reflections.invokeAndWrap(GET_ROW_COUNT, dataModel);
   }
   
   public static Object getWrappedData(Object dataModel)
   {
      return Reflections.invokeAndWrap(GET_WRAPPED_DATA, dataModel);
   }
   
   public static Object setWrappedData(Object dataModel, Object data)
   {
      return Reflections.invokeAndWrap(SET_WRAPPED_DATA, dataModel, data);
   }
   
}
