package org.jboss.seam.wicket.ioc;

public interface InstrumentedComponent
{
   
   public WicketHandler getHandler();
   
   public InstrumentedComponent getEnclosingInstance();

}