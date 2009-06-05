package org.jboss.seam.example.restbay.resteasy.interfacestatelesscomponent;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.example.restbay.resteasy.SubResource;
import org.jboss.seam.example.restbay.resteasy.TestComponent;
import org.jboss.seam.example.restbay.resteasy.TestForm;
import org.jboss.seam.example.restbay.resteasy.TestResource;

import javax.ws.rs.core.MultivaluedMap;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Christian Bauer
 */
@Name("interfaceStatelessComponentTestResource")
@Scope(ScopeType.EVENT)
public class InterfaceStatelessComponentTestResourceBean extends TestResource implements InterfaceStatelessComponentTestResource
{

   protected final String INSTANCE_CODE_KEY = "interfaceStatelessComponentTestResource.instanceCode";

   // We want to verify that this is really stateless (Seam doesn't pool POJO instances, so that should work)
   public InterfaceStatelessComponentTestResourceBean()
   {
      Integer instanceCode;
      if ((instanceCode = (Integer)Contexts.getApplicationContext().get(INSTANCE_CODE_KEY)) != null)
      {
         assert !instanceCode.equals(hashCode());
      }
      Contexts.getApplicationContext().set(INSTANCE_CODE_KEY, hashCode());
   }

   protected void assertStateless()
   {
      assert !Contexts.getApplicationContext().get(INSTANCE_CODE_KEY).equals(hashCode());
   }

   @In
   TestComponent testComponent;

   @Override
   public String echoUri()
   {
      assertStateless();
      return super.echoUri();
   }

   @Override
   public String echoQueryParam(String bar)
   {
      assertStateless();
      return super.echoQueryParam(bar);
   }

   @Override
   public String echoHeaderParam(String bar)
   {
      assertStateless();
      return super.echoHeaderParam(bar);
   }

   @Override
   public String echoCookieParam(String bar)
   {
      assertStateless();
      return super.echoCookieParam(bar);
   }

   @Override
   public String echoTwoParams(String one, String two)
   {
      assertStateless();
      return super.echoTwoParams(one, two);
   }

   @Override
   public String echoEncoded(String val)
   {
      assertStateless();
      return super.echoEncoded(val);
   }

   @Override
   public String echoFormParams(MultivaluedMap<String, String> formMap)
   {
      assertStateless();
      return super.echoFormParams(formMap);
   }

   @Override
   public String echoFormParams2(String[] foo)
   {
      assertStateless();
      return super.echoFormParams2(foo);
   }

   @Override
   public String echoFormParams3(TestForm form)
   {
      assertStateless();
      return super.echoFormParams3(form);
   }

   @Override
   public SubResource getBar(String baz)
   {
      assertStateless();
      return super.getBar(baz);
   }

   @Override
   public long convertPathParam(GregorianCalendar isoDate)
   {
      assertStateless();
      return super.convertPathParam(isoDate);
   }

   @Override
   public String throwException()
   {
      assertStateless();
      return super.throwException();
   }

   @Override
   public List<String[]> getCommaSeparated()
   {
      assertStateless();
      assert headers.getAcceptableMediaTypes().size() == 2;
      assert headers.getAcceptableMediaTypes().get(0).toString().equals("text/plain");
      assert headers.getAcceptableMediaTypes().get(1).toString().equals("text/csv");
      return testComponent.getCommaSeparated();
   }

   @Override
   public String[] getCommaSeparatedStrings()
   {
      assertStateless();
      return super.getCommaSeparatedStrings();
   }

   @Override
   public Integer[] getCommaSeparatedIntegers()
   {
      assertStateless();
      return super.getCommaSeparatedIntegers();
   }
}