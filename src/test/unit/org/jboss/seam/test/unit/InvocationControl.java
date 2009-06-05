package org.jboss.seam.test.unit;

import java.util.concurrent.CountDownLatch;

public class InvocationControl
{
   private String name;
   private CountDownLatch start = new CountDownLatch(1);
   private CountDownLatch started = new CountDownLatch(1);
   private CountDownLatch finish = new CountDownLatch(1);
   private CountDownLatch finished = new CountDownLatch(1);
   
   public InvocationControl(String name) {
      this.name = name;
   }
   
   public String getName() {
      return this.name;
   }
   
   public void init() {
      await(start);
   }
   
   public void start() {
      start.countDown();
      await(started);
   }
   
   public void markStarted() {
      started.countDown();
      await(finish);
   }
   
   public void finish() {
      finish.countDown();
      await(finished);
   }
   
   public void markFinished() {
      finished.countDown();
   }
   
   private void await(CountDownLatch l) {
      try
      {
         l.await();
      }
      catch (InterruptedException e)
      {
      }
   }
}