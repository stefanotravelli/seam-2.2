package org.jboss.seam.excel;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.excel.csv.CsvExcelWorkbook;
import org.jboss.seam.excel.jxl.JXLExcelWorkbook;
import org.jboss.seam.util.Strings;

/**
 * Factory to get excel workbook implementation
 * 
 * @author Daniel Roth (danielc.roth@gmail.com)
 */
@Name("org.jboss.seam.excel.excelFactory")
@Scope(ScopeType.STATELESS)
@AutoCreate
@Install(precedence=Install.BUILT_IN)
@BypassInterceptors
public class ExcelFactory
{

   private static Map<String, Class<? extends ExcelWorkbook>> defaultImplementations;

   private Map<String, Class> implementations;

   static
   {
      defaultImplementations = new HashMap<String, Class<? extends ExcelWorkbook>>();
      defaultImplementations.put("csv", CsvExcelWorkbook.class);
      defaultImplementations.put("jxl", JXLExcelWorkbook.class);
   }

   public static ExcelFactory instance()
   {
      return (ExcelFactory) Component.getInstance(ExcelFactory.class);
   }

   public ExcelWorkbook getExcelWorkbook(String type)
   {

      Class<? extends ExcelWorkbook> clazz;

      ExcelWorkbook excelWorkbook;

      if (Strings.isEmpty(type))
      {
         type = "jxl";
      }

      if (implementations != null && implementations.get(type) != null)
      {
         clazz = implementations.get(type);
      }
      else
      {
         clazz = defaultImplementations.get(type);
      }

      if (clazz == null)
      {
         throw new IllegalArgumentException("Unable to create workbook of type " + type);
      }

      try
      {
         excelWorkbook = clazz.newInstance();
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException("The class provided could not be instanciated " + type, e);
      }

      return excelWorkbook;

   }

   public Map<String, Class> getImplementations()
   {
      return implementations;
   }

   public void setImplementations(Map<String, Class> implementations)
   {
      this.implementations = implementations;
   }

}