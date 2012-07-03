package org.jboss.seam.test.integration;

import javax.ejb.Local;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.JndiName;
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
   
   // JBSEAM-4993
   // The test starts two threads, one evaluates #{factoryLock.test.testOtherFactory()} and the other #{factoryLock.testString} 200ms later
   @Test
   public void factoryLock() 
       throws Exception 
   {
      exceptionOccured = false;
      Thread thread1 = new TestThread() {
         @Override
         public void runTest() throws Exception
         {
            FactoryLockTest.this.invokeMethod("foo", "#{factoryLock.test.testOtherFactory()}");
         }
      };

      Thread thread2 = new TestThread() {
         @Override
         public void runTest() throws Exception
         {
            Thread.sleep(200);
            FactoryLockTest.this.getValue("testString", "#{factoryLock.testString}");
         }
      };

      thread1.start();
      thread2.start();
   
      thread1.join();
      thread2.join();
      
      assert !exceptionOccured;
   }
   
   // This test is the same as factoryLock test, except it uses the same factory in both threads.
   @Test
   public void sameFactoryLock() 
       throws Exception 
   {
      exceptionOccured = false;
      Thread thread1 = new TestThread() {
         @Override
         public void runTest() throws Exception
         {
            FactoryLockTest.this.invokeMethod("testString", "#{factoryLock.test.testSameFactory()}");
         }
      };

      Thread thread2 = new TestThread() {
         @Override
         public void runTest() throws Exception
         {
            Thread.sleep(200);
            FactoryLockTest.this.getValue("testString", "#{factoryLock.testString}");
         }
      };

      thread1.start();
      thread2.start();
   
      thread1.join();
      thread2.join();
      
      assert !exceptionOccured;
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
   //@JndiName("java:global/test/FactoryLockTest$FactoryLockAction")
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
      
      @Factory(value="factoryLock.testString", scope=ScopeType.EVENT)
      public String getTestString() {
         return "testString";
      }
      @Remove
      public void remove() {}
   }
   
   @Name("factoryLock.testProducer")
   public static class TestProducer {
      @Factory(value="factoryLock.foo", scope=ScopeType.EVENT)
      public String getFoo() {
         return "foo";
      }
   }
}
