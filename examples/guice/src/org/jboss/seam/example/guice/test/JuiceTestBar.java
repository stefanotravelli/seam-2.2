package org.jboss.seam.example.guice.test;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.example.guice.JuiceBar;
import org.jboss.seam.example.guice.Juice;
import org.jboss.seam.ioc.guice.Guice;

/**
 * A component used by the disinjection test to obtain the values of the fields after the proxied call.
 *
 * @author Pawel Wrzeszcz (pawel . wrzeszcz [at] gmail . com)
 */
@Name("juiceTestBar")
@Guice
public class JuiceTestBar extends JuiceBar
{
   @BypassInterceptors
   public Juice getJuiceOfTheDayBypassInterceptors()
   {
      return juiceOfTheDay;
   }

   @BypassInterceptors
   public Juice getAnotherJuiceBypassInterceptors()
   {
      return anotherJuice;
   }
}
