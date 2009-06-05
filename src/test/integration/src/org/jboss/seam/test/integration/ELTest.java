package org.jboss.seam.test.integration;

import javax.el.ELException;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

import org.jboss.seam.jsf.UnifiedELMethodBinding;
import org.jboss.seam.jsf.UnifiedELValueBinding;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

/**
 * 
 * @author Pete Muir
 *
 */
public class ELTest extends SeamTest
{
   
   @Override
   protected void startJbossEmbeddedIfNecessary() 
   throws org.jboss.deployers.spi.DeploymentException ,java.io.IOException {}
   
   @Test
   public void testUnifiedELMethodBinding() throws Exception
   {
      new FacesRequest() 
      {
         @SuppressWarnings("deprecation")
        @Override
         protected void invokeApplication() throws Exception
         {
            MethodBinding methodBinding = new UnifiedELMethodBinding("#{action.go}", new Class[0]);
            
            assert "#{action.go}".equals(methodBinding.getExpressionString());
            
            assert String.class.equals(methodBinding.getType(getFacesContext()));
            
            Object result = methodBinding.invoke(getFacesContext(), new Object[0]);
            
            assert result instanceof String;
            assert "success".equals(result);
         }
      }.run();
   }
   
   @Test
   public void testUnifiedELMethodBindingWithNull() throws Exception
   {
      new FacesRequest() 
      {
         @SuppressWarnings("deprecation")
         @Override
         protected void invokeApplication() throws Exception
         {

            MethodBinding methodBinding = new UnifiedELMethodBinding("#{action.go}", null);
            
            assert String.class.equals(methodBinding.getType(getFacesContext()));
            
            Object result = methodBinding.invoke(getFacesContext(), null);
            
            assert result instanceof String;
            assert "success".equals(result);
         }
      }.run();
   }
   
   @Test
   public void testEmptyUnifiedELMethodBinding() throws Exception
   {
      new FacesRequest() 
      {
         @SuppressWarnings("deprecation")
        @Override
         protected void invokeApplication() throws Exception
         {

            MethodBinding methodBinding = new UnifiedELMethodBinding();
            boolean failed = false;
            try
            {
               methodBinding.invoke(getFacesContext(), null);
            }
            catch (ELException e) {
               failed = true;
            }
            assert failed;
         }
      }.run();
   }
   
   @Test
   public void testUnifiedELValueBinding() throws Exception
   {
      new FacesRequest()
      {
         @SuppressWarnings("deprecation")
         @Override
         protected void invokeApplication() throws Exception
         {
            ValueBinding valueBinding = new UnifiedELValueBinding("#{person.name}");
            
            assert "#{person.name}".equals(valueBinding.getExpressionString());
            
            assert !valueBinding.isReadOnly(getFacesContext());
            
            assert String.class.equals(valueBinding.getType(getFacesContext()));
            
            valueBinding.setValue(getFacesContext(), "Pete");
            
            assert "Pete".equals(valueBinding.getValue(getFacesContext()));
         }
      }.run();
   }
   
   @Test
   public void testEmptyUnifiedELValueBinding() throws Exception
   {
      new FacesRequest() 
      {
         @SuppressWarnings("deprecation")
         @Override
         protected void invokeApplication() throws Exception
         {

            ValueBinding valueBinding = new UnifiedELValueBinding();
            boolean failed = false;
            try
            {
               valueBinding.setValue(getFacesContext(), "Pete");
               valueBinding.getValue(getFacesContext());
            }
            catch (ELException e) {
               failed = true;
            }
            assert failed;
         }
      }.run();
   }

}
