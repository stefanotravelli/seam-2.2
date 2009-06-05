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

import java.io.File;
import java.io.PrintStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.InputRequest;

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

   private String buildfile;
   private DefaultLogger log;
   private boolean explode = true;

   public SeamGenAdapter(String buildfile)
   {
      this(buildfile, System.out, System.err);
   }

   public SeamGenAdapter(String buildfile, PrintStream out, PrintStream err)
   {
      this.buildfile = buildfile;
      log = new DefaultLogger();
      log.setOutputPrintStream(out);
      log.setErrorPrintStream(err);
      log.setMessageOutputLevel(Project.MSG_INFO);
   }

   public Project getAntCall()
   {
      Project ant = new Project();
      ant.init();
      ProjectHelper.configureProject(ant, new File(buildfile));
      // ant.addBuildListener(log);
      return ant;
   }

   public void createProject()
   {
      getAntCall().executeTarget("create-project");
   }

   public void deleteProject()
   {
      getAntCall().executeTarget("delete-project");
   }

   public void newAction(String[] properties)
   {
      Project project = getAntCall();
      project.setInputHandler(getInputHandler(properties));
      project.executeTarget("new-action");
   }

   public void newForm(String[] properties)
   {
      Project project = getAntCall();
      project.setInputHandler(getInputHandler(properties));
      project.executeTarget("new-form");
   }

   public void generateEntities()
   {
      getAntCall().executeTarget("generate-entities");
   }

   /**
    * Deploy the application. Using either explode or deploy target, depending
    * on explode property.
    */
   public void deploy()
   {
      if (explode)
      {
         getAntCall().executeTarget("explode");
      }
      else
      {
         getAntCall().executeTarget("deploy");
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
         getAntCall().executeTarget("unexplode");
      }
      else
      {
         getAntCall().executeTarget("undeploy");
      }
   }

   public void hotDeploy()
   {
      if (explode)
      {
         getAntCall().executeTarget("explode");
      }
      else
      {
         throw new IllegalStateException("Unable to hot deploy non-exploded archive");
      }
   }

   public void restart()
   {
      getAntCall().executeTarget("restart");
   }

   public void addIdentityManagement()
   {
      getAntCall().executeTarget("add-identity-management");
   }

   private InputHandler getInputHandler(final String[] properties)
   {
      return new InputHandler()
      {
         public void handleInput(InputRequest request) throws BuildException
         {
            if (request.getPrompt().contains("Enter the Seam component name"))
            {
               request.setInput(properties[0]);
            }
            else if (request.getPrompt().contains("Enter the local interface name"))
            {
               request.setInput(properties[1]);
            }
            else if (request.getPrompt().contains("Enter the bean class name"))
            {
               request.setInput(properties[2]);
            }
            else if (request.getPrompt().contains("Enter the action method name"))
            {
               request.setInput(properties[3]);
            }
            else if (request.getPrompt().contains("Enter the page name"))
            {
               request.setInput(properties[4]);
            }
            else
            {
               throw new RuntimeException("Unexpected prompt " + request.getPrompt());
            }
         }
      };
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

}
