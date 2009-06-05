package org.jboss.seam.jsf;

import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;

/**
 * Factory for SeamApplication (how could you possibly
 * have figured that out without JavaDoc?)
 * 
 * @see SeamApplication
 * @author Gavin King
 *
 */
public class SeamApplicationFactory extends ApplicationFactory
{
   
   private final ApplicationFactory delegate;
   
   public SeamApplicationFactory(ApplicationFactory af)
   {
      delegate = af;
   }

   @Override
   public Application getApplication()
   {
      return new SeamApplication( delegate.getApplication() );
   }

   @Override
   public void setApplication(Application application)
   {
      delegate.setApplication(application);
   }

}
