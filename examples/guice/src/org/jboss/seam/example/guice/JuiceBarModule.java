package org.jboss.seam.example.guice;

import com.google.inject.Module;
import com.google.inject.Binder;
import com.google.inject.Scopes;

/**
 * @author Pawel Wrzeszcz (pwrzeszcz [at] jboss . org)
 */
public class JuiceBarModule implements Module
{
   public void configure(Binder binder)
   {
      binder.bind(Juice.class).to(AppleJuice.class); // Create a new instance every time.
       
      binder.bind(Juice.class).annotatedWith(Orange.class).to(OrangeJuice.class).in(Scopes.SINGLETON);
   }
}
