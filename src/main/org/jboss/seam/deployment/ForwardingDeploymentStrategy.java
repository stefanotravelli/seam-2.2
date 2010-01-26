package org.jboss.seam.deployment;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 * A decorator for DeploymentStrategy
 * 
 * @author Dan Allen
 * @author Pete Muir
 */
public abstract class ForwardingDeploymentStrategy extends DeploymentStrategy
{

   @Override
   public ClassLoader getClassLoader()
   {
      return delegate().getClassLoader();
   }
   
   @Override
   public ServletContext getServletContext()
   {
      return delegate().getServletContext();
   }

   @Override
   protected String getDeploymentHandlersKey()
   {
      return delegate().getDeploymentHandlersKey();
   }
   
   @Override
   public void scan()
   {
      delegate().scan();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
   }
   
   @Override
   public Map<String, DeploymentHandler> getDeploymentHandlers()
   {
      return delegate().getDeploymentHandlers();
   }
   
   @Override
   public List<File> getFiles()
   {
      return delegate().getFiles();
   }
   
   @Override
   protected Scanner getScanner()
   {
      return delegate().getScanner();
   }
   
   @Override
   public long getTimestamp()
   {
      return delegate().getTimestamp();
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
   @Override
   protected void postScan()
   {
      delegate().postScan();
   }
   
   @Override
   public void setFiles(List<File> files)
   {
      delegate().setFiles(files);
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }
   
   protected abstract DeploymentStrategy delegate();

}