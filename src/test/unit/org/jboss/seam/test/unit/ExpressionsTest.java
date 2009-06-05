package org.jboss.seam.test.unit;

import static org.testng.Assert.assertEquals;

import java.beans.FeatureDescriptor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.jboss.seam.contexts.FacesLifecycle;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.faces.FacesExpressions;
import org.jboss.seam.mock.MockApplication;
import org.jboss.seam.mock.MockExternalContext;
import org.jboss.seam.mock.MockFacesContext;
import org.jboss.seam.mock.MockHttpServletRequest;
import org.jboss.seam.mock.MockHttpSession;
import org.testng.annotations.Test;

public class ExpressionsTest
{
   /**
    * Validate that FacesExpressions reports that the FacesContext is activate and
    * returns the Faces-aware EL context so that a Faces-specific expression can
    * be resolved through Seam's built-in expression resolver.
    * @jira JBSEAM-3674
    */
   @Test
   public void testExpressionResolvedInFacesELContext()
   {
      Map<String, String> params = new HashMap<String, String>();
      params.put("foo", "bar");
      FacesContext facesContext = setupFacesContextToAccessRequestParams(params);
      String expr = "#{param.foo}";
      
      // the control
      assertEquals(facesContext.getApplication().evaluateExpressionGet(facesContext, expr, Object.class), "bar");
      
      // the test
      FacesLifecycle.setPhaseId(PhaseId.INVOKE_APPLICATION);
      Expressions expressions = new FacesExpressions();
      assert expressions.getELContext().getContext(FacesContext.class) != null;
      assertEquals(expressions.createValueExpression(expr).getValue(), "bar");
   }
   
   protected FacesContext setupFacesContextToAccessRequestParams(Map<String, String> params)
   {
      MockHttpServletRequest request = new MockHttpServletRequest(new MockHttpSession());
      if (params != null)
      {
         for (Map.Entry<String, String> param : params.entrySet())
         {
            request.getParameterMap().put(param.getKey(), new String[] { param.getValue() });
         }
      }
      ExternalContext extContext = new MockExternalContext(request);
      Application application = new MockApplication();
      application.addELResolver(new ImplicitObjectELResolver());
      FacesContext facesCtx = new MockFacesContext(extContext, application).setCurrent();
      assert FacesContext.getCurrentInstance() != null;
      return facesCtx;
   }
   
   /**
    * This resolver resolves select implicit objects that are available to the EL during a Faces request.
    * It must be implemented here since it is part of the JSF RI, not the API.
    */
   class ImplicitObjectELResolver extends ELResolver
   {
      private final String PARAM = "param";
      
      private final String[] IMPLICIT_OBJECT_NAMES = new String[] { PARAM };
      
      @Override
      public Class<?> getCommonPropertyType(ELContext ctx, Object base)
      {
         return null;
      }

      @Override
      public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext ctx, Object base)
      {
         return null;
      }

      @Override
      public Class<?> getType(ELContext ctx, Object base, Object prop)
      {
         return null;
      }

      @Override
      public Object getValue(ELContext elCtx, Object base, Object prop)
      {
         if (base != null) return null;
         
         if (prop == null) throw new PropertyNotFoundException("No such property " + prop);

         int idx = Arrays.binarySearch(IMPLICIT_OBJECT_NAMES, prop);
         if (idx < 0) return null;
         
         FacesContext facesCtx = (FacesContext) elCtx.getContext(FacesContext.class);
         ExternalContext extCtx = facesCtx.getExternalContext();
         
         if (prop.equals(PARAM))
         {
            elCtx.setPropertyResolved(true);
            return extCtx.getRequestParameterMap();
         }

         throw new IllegalStateException("Programming error: list of possible conditions is incomplete");
      }

      @Override
      public boolean isReadOnly(ELContext ctx, Object base, Object prop)
      {
         return true;
      }

      @Override
      public void setValue(ELContext ctx, Object base, Object prop, Object value) {}
   }
}
