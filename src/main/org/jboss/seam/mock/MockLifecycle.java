//$Id$
package org.jboss.seam.mock;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;

public class MockLifecycle extends Lifecycle
{
   
   public static final Lifecycle INSTANCE = new MockLifecycle();
   
   public MockLifecycle()
   {
      MockLifecycleFactory.setLifecycle(this);
      FactoryFinder.setFactory(FactoryFinder.LIFECYCLE_FACTORY, MockLifecycleFactory.class.getName());
   }

   @Override
   public void addPhaseListener(PhaseListener pl)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void execute(FacesContext ctx) throws FacesException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public PhaseListener[] getPhaseListeners()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void removePhaseListener(PhaseListener pl)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void render(FacesContext ctx) throws FacesException
   {
      throw new UnsupportedOperationException();
   }

}
