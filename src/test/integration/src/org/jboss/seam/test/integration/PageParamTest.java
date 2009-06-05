package org.jboss.seam.test.integration;

import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;
import javax.faces.validator.ValidatorException;
import javax.faces.application.FacesMessage;
import java.util.List;

/**
 * @author Pete Muir
 * @author Dan Allen
 */
public class PageParamTest extends SeamTest
{

   @Test
   public void testPageParameter() throws Exception
   {
      new FacesRequest("/pageWithParameter.xhtml")
      {
         @Override
         protected void beforeRequest()
         {
            setParameter("personName", "pete");
         }
         
         @Override
         protected void invokeApplication() throws Exception
         {
            assert "pete".equals(getValue("#{person.name}"));
         }
      }.run();
      
      new FacesRequest("/pageWithParameter.xhtml")
      {
         @Override
         protected void beforeRequest()
         {
            setParameter("anotherPersonName", "pete");
         }
         
         @Override
         protected void invokeApplication() throws Exception
         {
            assert getValue("#{person.name}") == null;
         }
      }.run();
   }

   @Test
   public void testPageParameterFailsModelValidation() throws Exception
   {
      new FacesRequest("/pageWithParameter.xhtml")
      {
         @Override
         protected void beforeRequest()
         {
            setParameter("personName", "pe");
         }

         @Override
         protected void invokeApplication() throws Exception
         {
            List<FacesMessage> messages = (List<FacesMessage>) getValue("#{facesMessages.currentMessages}");
            assert messages.size() == 1;
            assert messages.get(0).getDetail().startsWith("'personName' parameter is invalid");
            assert getValue("#{person.name}") == null;
         }
      }.run();
   }

   @Test
   public void testPageParameterModelValidationDisabled() throws Exception
   {
      new FacesRequest("/pageWithValidateModelDisabledParameter.xhtml")
      {
         @Override
         protected void beforeRequest()
         {
            setParameter("personName", "pe");
         }

         @Override
         protected void invokeApplication() throws Exception
         {
            assert "pe".equals(getValue("#{person.name}"));
         }
      }.run();
   }
   
   @Test
   public void testRequiredPageParameter() throws Exception
   {
      new FacesRequest("/pageWithRequiredParameter.xhtml")
      {
         @Override
         protected void beforeRequest()
         {
            setParameter("personName", "pete");
         }
         
         @Override
         protected void invokeApplication() throws Exception
         {
            assert "pete".equals(getValue("#{person.name}"));
         }
      }.run();
      
   }
   
}
