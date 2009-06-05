package org.jboss.seam.test.integration;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.bpm.CreateProcess;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

public class BusinessProcessTest 
    extends SeamTest
{
    @Test
    public void noProcessDefinition() 
        throws Exception 
    {
        new FacesRequest() {
            @Override
            protected void invokeApplication() throws Exception {
                try {
                    invokeAction("#{bpmTest.startInvalid}");
                    assert false;
                } catch (Exception e) {
                    // expected
                }
            }

        }.run();
    }
    
    @Test
    public void noVariableStart() 
        throws Exception 
    {
        new FacesRequest() {
            @Override
            protected void invokeApplication() throws Exception {
                try {
                    invokeAction("#{bpmTest.startOne}");
                } catch (Exception e) {
                    assert false;
                }
            }

        }.run();
    }
    
    
    @Name("bpmTest")
    static public class ProcessComponent {
        @CreateProcess(definition="NoSuchProcess") 
        public void startInvalid() {
        }
        
        @CreateProcess(definition="TestProcess1") 
        public void startOne() {            
        }
    }
}
