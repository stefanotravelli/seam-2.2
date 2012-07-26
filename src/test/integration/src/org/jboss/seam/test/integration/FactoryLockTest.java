package org.jboss.seam.test.integration;

import javax.ejb.Local;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.mock.SeamTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FactoryLockTest extends SeamTest
{
   private volatile boolean exceptionOccured = false;   
   
   private abstract class TestThread extends Thread {
      public abstract void runTest() throws Exception;
      
      @Override
      public void run()
      {
         try
         {
            runTest();
         }
         catch (Throwable e)
         {
            e.printStackTrace();
            FactoryLockTest.this.exceptionOccured = true;
         }
      }
   }
   
   private void multiThreadedTest(Thread... threads) throws InterruptedException {
      exceptionOccured = false;

      for (Thread thread : threads) {
         thread.start();
      }

      for (Thread thread : threads) {
         thread.join();
      }

      assert !exceptionOccured;
   }

   
   // JBSEAM-4993
   // The test starts two threads, one evaluates #{factoryLock.test.testOtherFactory()} and the other #{factoryLock.testString} 200ms later
   @Test
   public void factoryLock() 
       throws Exception 
   {
      multiThreadedTest(new TestThread() {
         @Override
         public void runTest() throws Exception
         {
            FactoryLockTest.this.invokeMethod("foo", "#{factoryLock.test.testOtherFactory()}");
         }
      },
      
      new TestThread() {
         @Override
         public void runTest() throws Exception
         {
            Thread.sleep(200);
            FactoryLockTest.this.getValue("testString", "#{factoryLock.testString}");
         }
      });
   }
   
   // This test is the same as factoryLock test, except it uses the same factory in both threads.
   @Test
   public void sameFactoryLock() 
       throws Exception 
   {
      multiThreadedTest(new TestThread() {
         @Override
         public void runTest() throws Exception
         {
            FactoryLockTest.this.invokeMethod("testString", "#{factoryLock.test.testSameFactory()}");
         }
      },

      new TestThread() {
         @Override
         public void runTest() throws Exception
         {
            Thread.sleep(200);
            FactoryLockTest.this.getValue("testString", "#{factoryLock.testString}");
         }
      });
   }
   
   // Test the behavior of two components using factories of each other.
   // Skip the test, as it causes deadlock.
   @Test(enabled=false)
   public void interleavingFactories()
         throws Exception
   {
      multiThreadedTest(new TestThread() {
         @Override
         public void runTest() throws Exception
         {
            FactoryLockTest.this.getValue("knit(purl)", "#{factoryLock.knitPurl}");
         }
      },

      new TestThread() {
         @Override
         public void runTest() throws Exception
         {
            Thread.sleep(200);
            FactoryLockTest.this.getValue("purl(knit)", "#{factoryLock.purlKnit}");
         }
      });
   }
   
   private void invokeMethod(final String expected, final String el) throws Exception {
      new ComponentTest() {
         @Override
         protected void testComponents() throws Exception {
            Assert.assertEquals(expected, invokeMethod(el));
         }
     }.run();
   }
   
   private void getValue(final String expected, final String el) throws Exception {
      new ComponentTest() {
         @Override
         protected void testComponents() throws Exception {
            Assert.assertEquals(expected, getValue(el));
         }
     }.run();
   }

   @Local
   public static interface FactoryLockLocal
   {
      public String getTestString();
      public String testOtherFactory();
      public String testSameFactory();
      public void remove();
   }

   
   @Stateful
   @Scope(ScopeType.SESSION)
   @Name("factoryLock.test")
   public static class FactoryLockAction implements FactoryLockLocal
   {
      public String testOtherFactory() {
         try
         {
            Thread.sleep(500);
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
         return (String)Component.getInstance("factoryLock.foo", true);
      }
      
      // gets instance produced by this component's factory 
      public String testSameFactory() {
         try
         {
            Thread.sleep(500);
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
         return (String)Component.getInstance("factoryLock.testString", true);
      }
      
      @Factory(value="factoryLock.testString", scope=ScopeType.SESSION)
      public String getTestString() {
         return "testString";
      }
      @Remove
      public void remove() {}
   }
   
   @Name("factoryLock.testProducer")
   public static class TestProducer {
      @Factory(value="factoryLock.foo", scope=ScopeType.SESSION)
      public String getFoo() {
         return "foo";
      }
   }
   
   @Scope(ScopeType.APPLICATION)
   @Name("factoryLock.knitFactory")
   public static class KnitFactory
   {
      @Factory(value="factoryLock.knitPurl", scope=ScopeType.SESSION)
      public String getDoubleKnit() {
         try
         {
            Thread.sleep(500);
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
         return "knit(" + (String)Component.getInstance("factoryLock.purl") + ")";
      }

      @Factory(value="factoryLock.knit", scope=ScopeType.SESSION)
      public String getKnit() {
         return "knit";
      }
   }

   @Scope(ScopeType.APPLICATION)
   @Name("factoryLock.purlFactory")
   public static class PurlFactory
   {
      @Factory(value="factoryLock.purlKnit", scope=ScopeType.SESSION)
      public String getDoublePurl() {
         return "purl(" + (String)Component.getInstance("factoryLock.knit") + ")";
      }

      @Factory(value="factoryLock.purl", scope=ScopeType.SESSION)
      public String getPurl() {
         return "purl";
      }
   }
}
