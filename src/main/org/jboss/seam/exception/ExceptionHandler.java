package org.jboss.seam.exception;

import org.jboss.seam.faces.Navigator;

/**
 * An element of the chain that knows how to handle a 
 * specific exception type.
 * 
 * @author Gavin King
 *
 */
public abstract class ExceptionHandler extends Navigator
{
   public enum LogLevel { fatal, error, warn, info, debug, trace }
   
   private boolean logEnabled;
   private LogLevel logLevel;
   
   public abstract void handle(Exception e) throws Exception;
   public abstract boolean isHandler(Exception e);
   
   public boolean isLogEnabled()
   {
      return logEnabled;
   }
   
   public void setLogEnabled(boolean logEnabled)
   {
      this.logEnabled = logEnabled;
   }
   
   public LogLevel getLogLevel()
   {
      return logLevel;
   }
   
   public void setLogLevel(LogLevel logLevel)
   {
      this.logLevel = logLevel;
   }   
   
}