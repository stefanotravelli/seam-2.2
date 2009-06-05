package org.jboss.seam.test.unit.component;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;

/**
 * A bunch of test actions to be used in unit tests.
 */
@Name("testActions")
public class TestActions
{
   private List<String> actionsCalled = new ArrayList<String>();
   
   public String nonNullActionA() {
      actionsCalled.add("nonNullActionA");
      return "outcomeA";
   }
   
   public String nonNullActionB() {
      actionsCalled.add("nonNullActionB");
      return "outcomeB";
   }
   
   public String nonNullActionC() {
      actionsCalled.add("nonNullActionC");
      return "outcomeC";
   }
   
   public void nullActionA() {
      actionsCalled.add("nullActionA");
   }
   
   public void nullActionB() {
      actionsCalled.add("nullActionB");
   }
   
   public void nullActionC() {
      actionsCalled.add("nullActionC");
   }
   
   public List<String> getActionsCalled() {
      return actionsCalled;
   }
   
   public static TestActions instance() {
      return (TestActions) Component.getInstance(TestActions.class);
   }
}
