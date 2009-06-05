package org.jboss.seam.mock;

import java.util.Arrays;
import java.util.Iterator;

import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;

public class MockLifecycleFactory extends LifecycleFactory
{
   
   private static Lifecycle lifecycle;
   
   public static void setLifecycle(Lifecycle lifecycle)
   {
      MockLifecycleFactory.lifecycle = lifecycle;
   }
   
   public static Lifecycle getLifecycle()
   {
      return MockLifecycleFactory.lifecycle;
   }

   @Override
   public void addLifecycle(String lifecycleId, Lifecycle lifecycle)
   {
      throw new IllegalArgumentException("Not supported");
   }

   @Override
   public Lifecycle getLifecycle(String lifecycleId)
   {
      return lifecycle;
   }

   @Override
   public Iterator<String> getLifecycleIds()
   {
      return Arrays.asList(DEFAULT_LIFECYCLE).iterator();
   }
   
}
