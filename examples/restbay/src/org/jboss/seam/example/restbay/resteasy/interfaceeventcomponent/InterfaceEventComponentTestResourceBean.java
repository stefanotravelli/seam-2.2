package org.jboss.seam.example.restbay.resteasy.interfaceeventcomponent;

import org.jboss.seam.ScopeType;
import org.jboss.seam.example.restbay.resteasy.TestResource;
import org.jboss.seam.example.restbay.resteasy.TestComponent;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.List;

/**
 * @author Christian Bauer
 */
@Name("interfaceEventComponentTestResource")
@Scope(ScopeType.EVENT)
public class InterfaceEventComponentTestResourceBean extends TestResource implements InterfaceEventComponentTestResource
{

   @In
   TestComponent testComponent;

   @Override
   public List<String[]> getCommaSeparated()
   {
      assert headers.getAcceptableMediaTypes().size() == 2;
      assert headers.getAcceptableMediaTypes().get(0).toString().equals("text/plain");
      assert headers.getAcceptableMediaTypes().get(1).toString().equals("text/csv");
      return testComponent.getCommaSeparated();
   }
   
}
