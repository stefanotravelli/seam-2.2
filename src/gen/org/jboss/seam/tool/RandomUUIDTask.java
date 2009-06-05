package org.jboss.seam.tool;

import java.util.UUID;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Creates a random UUID using {@link java.util.UUID#randomUUID()} and
 * assigns it to the property.
 */
public class RandomUUIDTask extends Task
{
   private String property;

   @Override
   public void execute() throws BuildException
   {
      getProject().setProperty(property, generateRandomUUID());
   }

   protected String generateRandomUUID()
   {
      return UUID.randomUUID().toString();
   }

   public void setProperty(String property)
   {
      this.property = property;
   }
}
