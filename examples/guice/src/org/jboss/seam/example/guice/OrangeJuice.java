package org.jboss.seam.example.guice;

/**
 * @author Pawel Wrzeszcz (pwrzeszcz [at] jboss . org)
 */
public class OrangeJuice implements Juice
{
   private static final String name = "Orange Juice";
   private static final int price = 12;

   public OrangeJuice() {}

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
      return name + " (" + price + " cents)";
   }
}
