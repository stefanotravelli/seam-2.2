package org.jboss.seam.excel.ui.validation;

public interface Validation
{
   public enum ValidationType
   {
      numeric, range, list
   }

   public abstract ValidationType getType();
}
