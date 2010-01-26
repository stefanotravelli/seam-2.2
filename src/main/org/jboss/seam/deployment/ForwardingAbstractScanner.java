package org.jboss.seam.deployment;

import java.io.File;

import javax.servlet.ServletContext;


public abstract class ForwardingAbstractScanner extends AbstractScanner
{

   @Override
   public DeploymentStrategy getDeploymentStrategy()
   {
      return delegate().getDeploymentStrategy();
   }

   @Override
   public long getTimestamp()
   {
      return delegate().getTimestamp();
   }

   public void scanDirectories(File[] directories)
   {
      delegate().scanDirectories(directories);
   }

   public void scanResources(String[] resources)
   {
      delegate().scanResources(resources);
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
   }
   
   @Override
   protected boolean handle(String name)
   {
      return delegate().handle(name);
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }
   
   protected abstract AbstractScanner delegate();
   
   @Deprecated
   public ForwardingAbstractScanner()
   {
      
   }
   
   
   public ForwardingAbstractScanner(ServletContext servletContext)
   {
      super(servletContext);
   }
   
}
