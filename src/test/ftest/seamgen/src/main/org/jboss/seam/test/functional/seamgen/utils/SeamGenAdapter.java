/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.test.functional.seamgen.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import org.jboss.seam.test.functional.seamgen.SeamGenTest;

/**
 * This class wraps seam-gen ant script in order to be easily usable from Java.
 * Methods of this class corespond to seam-gen targets. Note that deploy and
 * undeploy methods are using explode variants of the targets if icefaces
 * variable is set to true which it is by default.
 * 
 * @author Jozef Hartinger
 * 
 */
public class SeamGenAdapter
{

   protected String buildfile;
   protected boolean explode = true;
   protected PrintStream out, err;
   protected String antExecutable;

   public SeamGenAdapter(String antExecutable, String buildfile)
   {
      this(antExecutable, buildfile, System.out, System.err);
   }

   public SeamGenAdapter(String antExecutable, String buildfile, PrintStream out, PrintStream err)
   {
      this.antExecutable = antExecutable;
      this.buildfile = buildfile;
      this.err = err;
      this.out = out;
   }

   private String getAntCommand(String task)
   {
      return antExecutable + " -f " + buildfile + " " + task;
   }

   protected void executeAntTarget(String task)
   {
      executeAntTarget(task, null);
   }

   protected void executeAntTarget(String task, String[] properties)
   {
      try
      {
         OutputStreamFeeder feeder = null;
         String antCommand = getAntCommand(task);
         out.print(antCommand);
         Process process = Runtime.getRuntime().exec((antCommand));
         if (properties != null && properties.length > 0)
         {
            feeder = new OutputStreamFeeder(process.getOutputStream(), out, properties);
            feeder.start();
         }
         // Associate the stdout InputStreamEater with the properties feeder to 
         // have the feeder type in a line from the properties whenever the eater 
         // encounters an input challenge.
         (new InputStreamEater(process.getInputStream(), out, feeder)).start();
         (new InputStreamEater(process.getErrorStream(), err, null)).start();
         process.waitFor();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException(e.toString());
      }
   }

   public void createProject()
   {
      executeAntTarget("create-project");
   }

   public void deleteProject()
   {
      executeAntTarget("delete-project");
   }

   public void newAction(String[] properties)
   {
      executeAntTarget("new-action", properties);
   }

   public void newForm(String[] properties)
   {
      executeAntTarget("new-form", properties);
   }

   public void generateEntities()
   {
      executeAntTarget("generate");
   }

   /**
    * Deploy the application. Using either explode or deploy target, depending
    * on explode property.
    */
   public void deploy()
   {
      if (explode)
      {
         executeAntTarget("explode");
      }
      else
      {
         executeAntTarget("deploy");
      }
   }

   /**
    * Undeploy the application. Using either unexplode or undeploy target,
    * depending on explode property.
    */
   public void undeploy()
   {
      if (explode)
      {
         executeAntTarget("unexplode");
      }
      else
      {
         executeAntTarget("undeploy");
      }
   }

   public void hotDeploy()
   {
      if (explode)
      {
         executeAntTarget("explode");
      }
      else
      {
         throw new IllegalStateException("Unable to hot deploy non-exploded archive");
      }
   }

   public void restart()
   {
      executeAntTarget("restart");
   }

   public void addIdentityManagement()
   {
      executeAntTarget("add-identity-management");
   }

   public boolean isExplode()
   {
      return explode;
   }

   /**
    * Set "deploy" or "explode" variant of deployment. "Explode" is used by
    * default.
    */
   public void setExplode(boolean explode)
   {
      this.explode = explode;
   }

   /**
    * EatInputStreamData class is used for handling InputStream (stdout, stderr)
    * of an ant sub-process. When it encounters an input challenge, it notifies
    * the associated {@link OutputStreamFeeder} to provide the input.
    * 
    */
   class InputStreamEater extends Thread
   {
      private static final String INPUT_CHALLENGE = "[input] Enter";
      private BufferedReader stream;
      private OutputStreamFeeder feederToNotify;
      private PrintStream out;

      public InputStreamEater(InputStream stream, PrintStream out, OutputStreamFeeder feederToNotify)
      {
         this.stream = new BufferedReader(new InputStreamReader(stream));
         this.out = out;
         this.feederToNotify = feederToNotify;
         setDaemon(true);
      }

      @Override
      public void run()
      {
         try
         {
            String line;
            while ((line = stream.readLine()) != null)
            {
               out.println(line);
               if (feederToNotify != null && line.contains(INPUT_CHALLENGE))
               {
                  // notify OutputStreamFeeder to send an input
                  feederToNotify.feed();
               }
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

   }

   /**
    * OutputStreamFeeder class is used for feeding OutputStream (stdin) of an
    * ant sub-process with appropriate inputs. It waits for a notification from
    * the associated {@link InputStreamEater} object.
    * 
    */
   class OutputStreamFeeder extends Thread
   {

      PrintStream stream, out;
      String[] food;

      public OutputStreamFeeder(OutputStream stream, PrintStream out, String[] food)
      {
         this.stream = new PrintStream(stream);
         this.food = food;
         this.out = out;
         setDaemon(true);
      }

      @Override
      public synchronized void run()
      {
         try
         {
            for (int i = 0; i < food.length; i++)
            {
               // wait for a notification from EatInputStreamData
               wait();
               stream.println(food[i]);
               stream.flush();
               out.println("Typed: " + food[i]);
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      public synchronized void feed()
      {
         this.notify();
      }

   }
}
