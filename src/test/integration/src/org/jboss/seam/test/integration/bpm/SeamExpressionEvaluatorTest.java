package org.jboss.seam.test.integration.bpm;

import org.jboss.seam.mock.SeamTest;
import org.jbpm.jpdl.el.ELException;
import org.testng.annotations.Test;

/**
 * @author Pete Muir
 *
 */
public class SeamExpressionEvaluatorTest extends SeamTest
{

   // Test for JBSEAM-1937
   @Test
   public void testValueExpression() throws Exception
   {
      new FacesRequest()
      {

         @Override
         protected void invokeApplication() throws Exception
         {
            invokeAction("#{seamExpressionEvaluatorTestController.createProcess2}");
         }
          
      }.run();
   }
   
   // Test for JBSEAM-3250
   @Test
   public void testUnqualifiedValueExpression() throws Exception
   {
      new FacesRequest()
      {

         @Override
         protected void invokeApplication() throws Exception
         {
            invokeAction("#{seamExpressionEvaluatorTestController.createProcess4}");
         }
          
      }.run();
   }
   
   // Test for JBSEAM-2152
   @Test
   public void testMissingMethod() throws Exception
   {
      new FacesRequest()
      {

         @Override
         protected void invokeApplication() throws Exception
         {
            try
            {
               invokeAction("#{seamExpressionEvaluatorTestController.createProcess3}");
            }
            catch (Exception e)
            {
               if (!(isRootCause(e, ELException.class) || isRootCause(e, javax.el.ELException.class)))
               {
                  e.printStackTrace();
                  assert false;
               }
            }
         }
          
      }.run();
   }
   
   private static boolean isRootCause(Throwable t, Class clazz)
   {
      for (Throwable cause = t.getCause(); cause != null && cause != cause.getCause(); cause = cause.getCause())
      {
         if (clazz.isAssignableFrom(cause.getClass()))
         {
            return true;
         }
      }
      return false;
   }
   
}
