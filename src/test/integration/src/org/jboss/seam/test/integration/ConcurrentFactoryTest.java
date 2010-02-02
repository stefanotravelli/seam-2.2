package org.jboss.seam.test.integration;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.In;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.core.Init;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.jboss.seam.ScopeType.APPLICATION;

public class ConcurrentFactoryTest 
    extends SeamTest 
{
    @Override
    protected void startJbossEmbeddedIfNecessary() 
          throws org.jboss.deployers.spi.DeploymentException,
                 java.io.IOException 
    {
       // don't deploy   
    }

    @Test(threadPoolSize = 2, invocationCount = 2)
    public void concurrentFactoryCall() 
        throws Exception 
    {
        new ComponentTest() {
            @Override
            protected void testComponents() throws Exception {
                assert "slowly created String".equals(getValue("#{concurrentFactoryTest.component.injectedString}"));
            }
        }.run();
    }
    
    @AfterMethod
    @Override
    public void end()
    {
       if (session != null) {
          // Because we run in threads. Only the first thread that finishes ends the session.
          ServletLifecycle.endSession(session);
       }
       session = null;
    }
    
    @Name("concurrentFactoryTest.component")
    static public class Component {
       @In(value = "concurrentFactoryTest.slowlyCreatedString") String injectedString;
       
       public String getInjectedString() {
          return injectedString;
       }
    }
    
    @Name("concurrentFactoryTest.SlowFactory")
    static public class SlowFactory {
        @Factory(value = "concurrentFactoryTest.slowlyCreatedString", scope = APPLICATION, autoCreate = true)
        public String slowlyCreateString() {
            try
            {
               Thread.sleep(1000);
               return "slowly created String";
            }
            catch (InterruptedException e)
            {
               e.printStackTrace();
               return null;
            }
        }        
    }
    


}
