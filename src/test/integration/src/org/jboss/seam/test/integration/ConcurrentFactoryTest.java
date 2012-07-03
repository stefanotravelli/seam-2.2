package org.jboss.seam.test.integration;

import static org.jboss.seam.ScopeType.APPLICATION;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;


// JBSEAM-4669
public class ConcurrentFactoryTest 
    extends SeamTest 
{
    private volatile boolean exceptionOccured = false;
    static AtomicInteger testSequence = new AtomicInteger(0);

    private void concurrentFactoryCallTest() throws Exception {
       new ComponentTest() {
          @Override
          protected void testComponents() throws Exception {
             int myTestSequence;
             myTestSequence = testSequence.getAndIncrement();
             if (myTestSequence == 0) {
                assert "TestString".equals(getValue("#{concurrentFactoryTest.LockHoldingComponent.string}"));
             } else {
                try {
                   Thread.sleep(500);
                } catch (InterruptedException e) {
                   e.printStackTrace();
                }
                assert "TestString".equals(getValue("#{concurrentFactoryTest.dependentString}"));
             }
             System.out.println(myTestSequence);

          }
      }.run();
    }
    
    private class ConcurrentFactoryCallTestThread extends Thread
    {
        public void run() {
            try
            {
                ConcurrentFactoryTest.this.concurrentFactoryCallTest();
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                ConcurrentFactoryTest.this.exceptionOccured = true;
            }
        };
    }
   
    @Test(timeOut=10000)
    public void concurrentFactoryCall() 
        throws Exception 
    {
       Thread thread1 = new ConcurrentFactoryCallTestThread();
       Thread thread2 = new ConcurrentFactoryCallTestThread();

       thread1.start();
       thread2.start();
    
       thread1.join();
       thread2.join();
       
       assert !exceptionOccured;
    }
    
    @Name("concurrentFactoryTest.LockHoldingComponent")
    @Scope(APPLICATION)
    static public class LockHoldingComponent implements Serializable {
       @In(value = "concurrentFactoryTest.slowlyCreatedComponent", create = true) SlowlyCreatedComponent slowlyCreatedComponent;
       
       public String getString() {
          return (String) Expressions.instance().createValueExpression("#{concurrentFactoryTest.plainFactoryGeneratedString}").getValue();
       }
    }
    
    @Name("concurrentFactoryTest.slowlyCreatedComponent")
    static public class SlowlyCreatedComponent {
       @Create
       public void slowlyCreate() {
          try {
             Thread.sleep(1000);
          } catch (InterruptedException e) {
             e.printStackTrace();
          }
       }
    }
      
    @Name("concurrentFactoryTest.dependentFactory")
    static public class DependentFactory {
        @Factory(value = "concurrentFactoryTest.dependentString", scope = APPLICATION, autoCreate = true)
        public String createString() {
           return (String) Expressions.instance().createValueExpression("#{concurrentFactoryTest.LockHoldingComponent.string}").getValue();
        }
    }
    
    @Name("concurrentFactoryTest.plainFactory")
    static public class PlainFactory {
        @Factory(value = "concurrentFactoryTest.plainFactoryGeneratedString", scope = APPLICATION, autoCreate = true)
        public String createPlainString() {
           return "TestString";
        }
    }
    
    @AfterClass
    @Override
    public void end()
    {
       // don't attempt to endSession, as it will block in the deadlocked org.jboss.seam.Component.getInstanceFromFactory lock
    }
}
