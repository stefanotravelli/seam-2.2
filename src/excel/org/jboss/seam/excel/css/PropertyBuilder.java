package org.jboss.seam.excel.css;

/**
 * Interface for parsing a style string array into a stylemap
 * 
 * @author karlsnic
 */
public interface PropertyBuilder
{
   public StyleMap parseProperty(String key, String[] values);
}