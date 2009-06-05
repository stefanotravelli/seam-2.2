package org.jboss.seam.mock;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

public abstract class DBUnitSeamTest extends AbstractDBUnitSeamTest
{

   @Override
   @BeforeClass
   @Parameters("datasourceJndiName")
   public void setDatasourceJndiName(String datasourceJndiName) 
   {
      super.setDatasourceJndiName(datasourceJndiName);
   }

   @Override
   @BeforeClass
   @Parameters("binaryDir")
   public void setBinaryDir(String binaryDir) 
   {
       super.setBinaryDir(binaryDir);
   }

   @Override
   @BeforeClass
   @Parameters("database")
   public void setDatabase(String database) 
   {
       super.setDatabase(database);
   }

   @Override
   @BeforeClass
   public void setupClass() throws Exception 
   {
       super.setupClass();
   }
   
   @Override
   @AfterClass
   protected void cleanupClass() throws Exception
   {
       super.cleanupClass();
   }
   
   @Override
   @BeforeSuite
   protected void startSeam() throws Exception
   {
       super.startSeam();
   }
   
   @Override
   @AfterSuite
   protected void stopSeam() throws Exception
   {
       super.stopSeam();
   } 
   

   @BeforeMethod
   @Override
   public void begin() 
   {
       super.begin();
   }

   @AfterMethod
   @Override
   public void end() 
   {
       super.end();
   }
   
}
