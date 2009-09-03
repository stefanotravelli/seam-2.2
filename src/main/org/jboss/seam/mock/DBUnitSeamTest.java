package org.jboss.seam.mock;

import static org.jboss.seam.mock.DBUnitSeamTest.Database.HSQL;
import static org.jboss.seam.mock.DBUnitSeamTest.Database.MYSQL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Optional;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

/**
 * Utility for integration testing with Seam and DBUnit datasets.
 * <p>
 * Subclass this class instead of <tt>SeamTest</tt> if you need to insert or clean data in
 * your database before and after a test. You need to implement <tt>prepareDBUnitOperations()</tt> and
 * add instances of <tt>DataSetOperation</tt>s to the <tt>beforeTestOperations</tt> and
 * <tt>afterTestOperations</tt> lists. An example:
 * <pre>
 * public class MyTest extends DBUnitSeamTest {
 *
 *   protected void prepareDBUnitOperations() {
 *       beforeTestOperations.add(
 *          new DataSetOperation("my/datasets/BaseData.xml")
 *       );
 *       beforeTestOperations.add(
 *           new DataSetOperation("my/datasets/AdditionalData.xml", DatabaseOperation.INSERT)
 *       );
 *   }
 * ... // Various test methods with @Test annotation
 * }
 * </pre>
 * <p>
 * Note that <tt>DataSetOperation</tt> defaults to <tt>DatabaseOperation.CLEAN_INSERT</tt> if no
 * other operation is specified as a constructor argument. The above example cleans all tables defined
 * in <tt>BaseData.xml</tt>, then inserts all rows declared in <tt>BaseData.xml</tt>, then inserts
 * all the rows declared in <tt>AdditionalData.xml</tt>. This executes before each test method
 * is invoked. If you require extra cleanup after a test method executes, add operations to the
 * <tt>afterTestOperations</tt> list.
 * </p>
 * <p>
 * A test class obtains the database connection for loading and cleaning of datasets in one of the following ways:
 * </p>
 * <dl>
 * <li>A TestNG test parameter named <tt>datasourceJndiName</tt> is provided by the TestNG test runner, which
 * automatically calls <tt>setDatasourceJndiName()</tt> on the test class before a logical test runs.</li>
 * <p/>
 * <li>An instance of a test class is created manually and the <tt>setDatasourceJndiName()</tt> method is
 * called after creation and before a test runs.</li>
 * <p/>
 * <li>A subclass overrides the <tt>getConnection()</tt> method and returns a custom database connection.</li>
 * <p/>
 * </dl>
 * <p>
 * Binary files can be imported into the database from a binary directory, configured with the TestNG parameter
 * <tt>binaryDir</tt> or by calling <tt>setBinaryDir()</tt> before a test runs. The binary directory is a classpath
 * reference, e.g. <tt>my/org/test/package/binarydir</tt>. In your DBUnit XML flat dataset, declare the path of your file
 * as follows: <tt>&lt;MYTABLE MYCOLUMN="[BINARY_DIR]/mytestfile.png"/&gt;</tt>
 * </p>
 * <p>
 * Referential integrity checks (foreign keys) will be or have to be disabled on the database connection
 * used for DBUnit operations. This makes adding circular references in datasets easier (especially for nullable
 * foreign key columns). Referential integrity checks are enabled again after the connection has been used.
 * </p>
 * <p>
 * <b>IMPORTANT: The methods <tt>disableReferentialIntegrity()</tt>,
 * <tt>enableReferentialIntegrity()</tt>, and <tt>editConfig()</tt> are implemented for HSQL and MySQL. You need to
 * configure the DBMS you are using with the <tt>database</tt> TestNG parameter or by calling <tt>setDatabase()</tt>
 * before the the test run. If you want to run unit tests on any other DBMS, you need to override the
 * <tt>disableReferentialIntegrity()</tt> and <tt>enableReferentialIntegrity()</tt> methods and implement them
 * for your DBMS. Also note that by default, if no <tt>database</tt> TestNG parameter has been set or if the
 * <tt>setDatabase()</tt> method has not been called before test runs, HSQL DB will be used as the default.</b>
 * </p>
 * @author Christian Bauer
 */
public abstract class DBUnitSeamTest extends SeamTest
{

   public enum Database
   {
      HSQL, MYSQL
   }

   private LogProvider log = Logging.getLogProvider(DBUnitSeamTest.class);

   protected String datasourceJndiName;
   protected String binaryDir;
   protected Database database = HSQL;
   protected boolean replaceNull = true;
   protected List<DataSetOperation> beforeTestOperations = new ArrayList<DataSetOperation>();
   protected List<DataSetOperation> afterTestOperations = new ArrayList<DataSetOperation>();

   private boolean prepared = false;

   @BeforeClass
   @Parameters("datasourceJndiName")
   public void setDatasourceJndiName(@Optional String datasourceJndiName)
   {
      if (datasourceJndiName == null) return;
      log.debug("Setting datasource name: " + datasourceJndiName);
      this.datasourceJndiName = datasourceJndiName;
   }

   public String getDatasourceJndiName()
   {
      return datasourceJndiName;
   }

   @BeforeClass
   @Parameters("binaryDir")
   public void setBinaryDir(@Optional String binaryDir)
   {
      if (binaryDir == null) return;
      log.debug("Setting binary directory: " + binaryDir);
      this.binaryDir = binaryDir;
   }

   public String getBinaryDir()
   {
      return binaryDir;
   }

   @BeforeClass
   @Parameters("database")
   public void setDatabase(@Optional String database)
   {
      if (database == null) return;
      log.debug("Setting database: " + database);
      this.database = Database.valueOf(database.toUpperCase());
   }

   // We don't have a getDatabase() getter because subclasses might use a different Enum!

   @BeforeClass
   @Parameters("replaceNull")
   public void setReplaceNull(@Optional Boolean replaceNull)
   {
      if (replaceNull == null) return;
      log.debug("Setting replace null: " + replaceNull);
      this.replaceNull = replaceNull;
   }

   public Boolean isReplaceNull()
   {
      return replaceNull;
   }

   @BeforeMethod
   public void prepareDataBeforeTest()
   {
      // This is not pretty but we unfortunately can not have dependencies between @BeforeClass methods.
      // This was a basic design mistake and we can't change it now because we need to be backwards
      // compatible. We can only "prepare" the datasets once all @BeforeClass have been executed.
      if (!prepared) {
         log.debug("Before test method runs, preparing datasets");
         prepareDBUnitOperations();
         for (DataSetOperation beforeTestOperation : beforeTestOperations)
         {
            beforeTestOperation.prepare(this);
         }
         for (DataSetOperation afterTestOperation : afterTestOperations)
         {
            afterTestOperation.prepare(this);
         }
         prepared = true;
      }

      executeOperations(beforeTestOperations);
   }

   @AfterMethod
   public void cleanDataAfterTest()
   {
      executeOperations(afterTestOperations);
   }

   private void executeOperations(List<DataSetOperation> list)
   {
      log.debug("Executing DataSetOperations: " + list.size());
      IDatabaseConnection con = null;
      try
      {
         con = getConnection();
         disableReferentialIntegrity(con);
         for (DataSetOperation op : list)
         {
            prepareExecution(con, op);
            op.execute(con);
            afterExecution(con, op);
         }
         enableReferentialIntegrity(con);
      }
      finally
      {
         if (con != null)
         {
            try
            {
               con.close();
            }
            catch (Exception ex)
            {
               ex.printStackTrace(System.err);
            }
         }
      }
   }

   protected static class DataSetOperation
   {

      private LogProvider log = Logging.getLogProvider(DataSetOperation.class);

      String dataSetLocation;
      ReplacementDataSet dataSet;
      DatabaseOperation operation;

      protected DataSetOperation()
      {
         // Support subclassing
      }

      /**
       * Defaults to <tt>DatabaseOperation.CLEAN_INSERT</tt>
       *
       * @param dataSetLocation location of DBUnit dataset
       */
      public DataSetOperation(String dataSetLocation)
      {
         this(dataSetLocation, DatabaseOperation.CLEAN_INSERT);
      }

      /**
       * Defaults to <tt>DatabaseOperation.CLEAN_INSERT</tt>
       *
       * @param dataSetLocation location of DBUnit dataset
       * @param dtdLocation optional (can be null) location of XML file DTD on classpath
       */
      public DataSetOperation(String dataSetLocation, String dtdLocation)
      {
         this(dataSetLocation, dtdLocation, DatabaseOperation.CLEAN_INSERT);
      }

      /**
       * @param dataSetLocation location of DBUnit dataset
       * @param operation operation to execute
       */
      public DataSetOperation(String dataSetLocation, DatabaseOperation operation)
      {
         this(dataSetLocation, null, operation);
      }

      public DataSetOperation(String dataSetLocation, String dtdLocation, DatabaseOperation operation)
      {
         if (dataSetLocation == null)
         {
            this.operation = operation;
            return;
         }

         // Load the base dataset file
         InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(dataSetLocation);
         try
         {
            InputStream dtdInput = null;
            if (dtdLocation != null)
            {
               dtdInput = Thread.currentThread().getContextClassLoader().getResourceAsStream(dtdLocation);
            }
            if (dtdInput == null)
            {
               this.dataSet = new ReplacementDataSet(new FlatXmlDataSet(input));
            }
            else
            {
               this.dataSet = new ReplacementDataSet(new FlatXmlDataSet(input, dtdInput));
            }
         }
         catch (Exception ex)
         {
            throw new RuntimeException(ex);
         }
         this.operation = operation;
         this.dataSetLocation = dataSetLocation;
      }

      public IDataSet getDataSet()
      {
         return dataSet;
      }

      public DatabaseOperation getOperation()
      {
         return operation;
      }

      public void prepare(DBUnitSeamTest test)
      {
         if (dataSet == null) return;
         log.debug("Preparing DataSetOperation replacement values");

         if (test.isReplaceNull())
         {
            log.debug("Replacing [NULL] placeholder with real null value");
            dataSet.addReplacementObject("[NULL]", null);
         }
         if (test.getBinaryDir() != null)
         {
            log.debug("Replacing [BINARY_DIR] placeholder with path: " + test.getBinaryDirFullpath().toString());
            dataSet.addReplacementSubstring("[BINARY_DIR]", test.getBinaryDirFullpath().toString());
         }
      }

      public void execute(IDatabaseConnection connection)
      {
         if (dataSet == null || operation == null) return;
         try
         {
            log.debug("Executing: " + this);
            this.operation.execute(connection, dataSet);
         }
         catch (Exception ex)
         {
            throw new RuntimeException(ex);
         }
      }

      @Override
      public String toString()
      {
         return getClass().getName() + " with dataset location: " + dataSetLocation;
      }
   }

   // Subclasses can/have to override the following methods

   /**
    * Override this method if you want to provide your own DBUnit <tt>IDatabaseConnection</tt> instance.
    * <p/>
    * If you do not override this, default behavior is to use the * configured datasource name and
    * to obtain a connection with a JNDI lookup.
    *
    * @return a DBUnit database connection (wrapped)
    */
   protected IDatabaseConnection getConnection()
   {
      try
      {
         if (getDatasourceJndiName() == null)
         {
            throw new RuntimeException("Please set datasourceJndiName TestNG property");
         }

         DataSource datasource = ((DataSource) getInitialContext().lookup(getDatasourceJndiName()));

         // Get a JDBC connection from JNDI datasource
         Connection con = datasource.getConnection();
         IDatabaseConnection dbUnitCon = new DatabaseConnection(con);
         editConfig(dbUnitCon.getConfig());
         return dbUnitCon;
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   /**
    * Execute whatever statement is necessary to either defer or disable foreign
    * key constraint checking on the given database connection, which is used by
    * DBUnit to import datasets.
    *
    * @param con A DBUnit connection wrapper, which is used afterwards for dataset operations
    */
   protected void disableReferentialIntegrity(IDatabaseConnection con)
   {
      try
      {
         if (database.equals(HSQL))
         {
            con.getConnection().prepareStatement("set referential_integrity FALSE").execute(); // HSQL DB
         }
         else if (database.equals(MYSQL))
         {
            con.getConnection().prepareStatement("set foreign_key_checks=0").execute(); // MySQL > 4.1.1
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   /**
    * Execute whatever statement is necessary to enable integrity constraint checks after
    * dataset operations.
    *
    * @param con A DBUnit connection wrapper, before it is used by the application again
    */
   protected void enableReferentialIntegrity(IDatabaseConnection con)
   {
      try
      {
         if (database.equals(HSQL))
         {
            con.getConnection().prepareStatement("set referential_integrity TRUE").execute();  // HSQL DB
         }
         else if (database.equals(MYSQL))
         {
            con.getConnection().prepareStatement("set foreign_key_checks=1").execute(); // MySQL > 4.1.1
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }

   /**
    * Override this method if you require DBUnit configuration features or additional properties.
    * <p/>
    * Called after a connection has been obtaind and before the connection is used. Can be a
    * NOOP method if no additional settings are necessary for your DBUnit/DBMS setup.
    *
    * @param config A DBUnit <tt>DatabaseConfig</tt> object for setting properties and features
    */
   protected void editConfig(DatabaseConfig config)
   {
      if (database.equals(HSQL))
      {
         // DBUnit/HSQL bugfix
         // http://www.carbonfive.com/community/archives/2005/07/dbunit_hsql_and.html
         config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new DefaultDataTypeFactory()
         {
            @Override
            public DataType createDataType(int sqlType, String sqlTypeName)
                  throws DataTypeException
            {
               if (sqlType == Types.BOOLEAN)
               {
                  return DataType.BOOLEAN;
               }
               return super.createDataType(sqlType, sqlTypeName);
            }
         });
      }
   }

   /**
    * Callback for each operation before DBUnit executes the operation, useful if extra preparation of
    * data/tables is necessary, e.g. additional SQL commands on a per-operation (per table?) granularity
    * on the given database connection.
    *
    * @param con       A DBUnit connection wrapper
    * @param operation The operation to be executed, call <tt>getDataSet()</tt> to access the data.
    */
   protected void prepareExecution(IDatabaseConnection con, DataSetOperation operation)
   {
   }

   /**
    * Callback for each operation, useful if extra preparation of data/tables is necessary.
    *
    * @param con       A DBUnit connection wrapper
    * @param operation The operation that was executed, call <tt>getDataSet()</tt> to access the data.
    */
   protected void afterExecution(IDatabaseConnection con, DataSetOperation operation)
   {
   }

   /**
    * Resolves the binary dir location with the help of the classloader, we need the
    * absolute full path of that directory.
    *
    * @return URL full absolute path of the binary directory
    */
   protected URL getBinaryDirFullpath()
   {
      if (getBinaryDir() == null)
      {
         throw new RuntimeException("Please set binaryDir TestNG property to location of binary test files");
      }
      return getResourceURL(getBinaryDir());
   }

   protected URL getResourceURL(String resource)
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
      if (url == null)
      {
         throw new RuntimeException("Could not find resource with classloader: " + resource);
      }
      return url;
   }

   /**
    * Load a file and return it as a <tt>byte[]</tt>. Useful for comparison operations in an actual
    * unit test, e.g. to compare an imported database record against a known file state.
    *
    * @param filename the path of the file on the classpath, relative to configured <tt>binaryDir</tt> base path
    * @return the file content as bytes
    * @throws Exception when the file could not be found or read
    */
   protected byte[] getBinaryFile(String filename) throws Exception
   {
      if (getBinaryDir() == null)
      {
         throw new RuntimeException("Please set binaryDir TestNG property to location of binary test files");
      }
      File file = new File(getResourceURL(getBinaryDir() + "/" + filename).toURI());
      InputStream is = new FileInputStream(file);

      // Get the size of the file
      long length = file.length();

      if (length > Integer.MAX_VALUE)
      {
         // File is too large
      }

      // Create the byte array to hold the data
      byte[] bytes = new byte[(int) length];

      // Read in the bytes
      int offset = 0;
      int numRead;
      while (offset < bytes.length
            && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
      {
         offset += numRead;
      }

      // Ensure all the bytes have been read in
      if (offset < bytes.length)
      {
         throw new IOException("Could not completely read file " + file.getName());
      }

      // Close the input stream and return bytes
      is.close();
      return bytes;
   }

   /**
    * Implement this in a subclass.
    * <p/>
    * Use it to stack DBUnit <tt>DataSetOperation</tt>'s with
    * the <tt>beforeTestOperations</tt> and <tt>afterTestOperations</tt> lists.
    */
   protected abstract void prepareDBUnitOperations();


}
