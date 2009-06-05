package org.jboss.seam.test.integration;

import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

/**
 * @author Pete Muir
 *
 */
public class JavaBeanEqualsTest extends SeamTest
{
   
   @Test
   // Test for JBSEAM-1257
   public void testReflexiveEquals() throws Exception
   {
      new ComponentTest()
      {

         @Override
         protected void testComponents() throws Exception
         {
            assert getInstance("beanA").equals(getInstance("beanA"));
            assert getValue("#{beanA.component}").equals(getValue("#{beanA.component}"));
         }
         
      }.run();
   }

}
