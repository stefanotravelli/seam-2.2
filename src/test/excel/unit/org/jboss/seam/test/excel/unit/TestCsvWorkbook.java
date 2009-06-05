package org.jboss.seam.test.excel.unit;

import org.jboss.seam.excel.csv.CsvExcelWorkbook;
import org.jboss.seam.excel.ui.UICell;
import org.jboss.seam.excel.ui.UIWorkbook;
import org.jboss.seam.excel.ui.UIWorksheet;
import org.testng.annotations.Test;

@Test
public class TestCsvWorkbook
{

   @Test(expectedExceptions = { RuntimeException.class })
   public void testOneSheetOnly()
   {
      CsvExcelWorkbook wb = new CsvExcelWorkbook();
      UIWorkbook uiWorkbook = new UIWorkbook();
      wb.createWorkbook(uiWorkbook);

      UIWorksheet sheet = new UIWorksheet();
      sheet.setName("sheet");
      wb.createOrSelectWorksheet(sheet);

      UIWorksheet sheet2 = new UIWorksheet();
      sheet2.setName("sheet2");
      wb.createOrSelectWorksheet(sheet2);

   }

   @Test
   public void testSimpleAdd()
   {
      CsvExcelWorkbook wb = new CsvExcelWorkbook();
      UIWorkbook uiWorkbook = new UIWorkbook();
      wb.createWorkbook(uiWorkbook);

      UIWorksheet sheet = new UIWorksheet();
      sheet.setName("sheet");
      wb.createOrSelectWorksheet(sheet);

      for (int i = 0; i < 2; i++)
      {
         for (int j = 0; j < 2; j++)
         {
            UICell cell = new UICell();
            cell.setValue(i + "_" + j);
            wb.addItem(cell);
         }
         wb.nextColumn();
      }

      byte[] correct = new String("\"0_0\",\"1_0\"\n\"0_1\",\"1_1\"\n").getBytes();
      byte[] created = wb.getBytes();

      for (int i = 0; i < created.length; i++)
      {
         assert correct[i] == created[i];
      }

   }

   public void testAddExplicit()
   {
      CsvExcelWorkbook wb = new CsvExcelWorkbook();
      UIWorkbook uiWorkbook = new UIWorkbook();
      wb.createWorkbook(uiWorkbook);

      UIWorksheet sheet = new UIWorksheet();
      sheet.setName("sheet");
      wb.createOrSelectWorksheet(sheet);

      UICell cell = new UICell();
      cell.setValue("A1");
      cell.setColumn(0);
      cell.setRow(0);
      wb.addItem(cell);
      cell.setValue("C2");
      cell.setColumn(2);
      cell.setRow(1);
      wb.addItem(cell);

      byte[] correct = new String("\"A1\",\"\",\"\"\n\"\",\"\",\"C2\"\n").getBytes();
      byte[] created = wb.getBytes();

      System.out.println(new String(created));
      
      for (int i = 0; i < created.length; i++)
      {
         assert correct[i] == created[i];
      }

   }

   @Test
   public void testOverlapAdd()
   {
      CsvExcelWorkbook wb = new CsvExcelWorkbook();
      UIWorkbook uiWorkbook = new UIWorkbook();
      wb.createWorkbook(uiWorkbook);

      UIWorksheet sheet = new UIWorksheet();
      sheet.setName("sheet");
      wb.createOrSelectWorksheet(sheet);

      for (int i = 0; i < 2; i++)
      {
         for (int j = 0; j < 2; j++)
         {
            UICell cell = new UICell();
            cell.setValue(i + "_" + j);
            wb.addItem(cell);
         }
         wb.nextColumn();
      }

      sheet.setStartColumn(1);
      sheet.setStartRow(1);
      wb.createOrSelectWorksheet(sheet);

      for (int i = 0; i < 2; i++)
      {
         for (int j = 0; j < 2; j++)
         {
            UICell cell = new UICell();
            cell.setValue(i + "_" + j);
            wb.addItem(cell);
         }
         wb.nextColumn();
      }

      byte[] correct = new String("\"0_0\",\"1_0\",\"\"\n\"0_1\",\"0_0\",\"1_0\"\n\"\",\"0_1\",\"1_1\"\n").getBytes();
      byte[] created = wb.getBytes();

      for (int i = 0; i < created.length; i++)
      {
         assert correct[i] == created[i];
      }

   }

}
