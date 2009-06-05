package org.jboss.seam.test.integration;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Import;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

public class ImportTest
    extends SeamTest
{
    
    @Test
    public void testImport() 
        throws Exception 
    {
        
        new FacesRequest() {
            @Override
            protected void invokeApplication()
                throws Exception
            {
                assert getValue("#{importTest.otherValue}").equals("foobar2");                 
            }        
        }.run();
    }
   
    
    @Name("importTest")
    @Import("importTest.ns2")
    public static class Importer {
        @In
        String otherValue;

        public String getOtherValue() {
            return otherValue;
        }

        @Factory(value="importTest.ns2.otherValue", autoCreate=true)
        public String createOtherValue() {
            return "foobar2";
        }
    }

}
