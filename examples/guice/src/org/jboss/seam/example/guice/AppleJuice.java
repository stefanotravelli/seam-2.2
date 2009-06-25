package org.jboss.seam.example.guice;

/**
 * @author Pawel Wrzeszcz (pwrzeszcz [at] jboss . org)
 */
public class AppleJuice implements Juice
{
   private static final String name = "Apple Juice";
   private static final int price = 10;

   public AppleJuice() {}

   public String getName()
   {
      return name;
   }

   public int getPrice()
   {
      return price;
   }

   @Override
   public String toString()
   {
      return name;
   }
}