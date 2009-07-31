package org.jboss.seam.async;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

@Scope(ScopeType.STATELESS)
@Name("org.jboss.seam.async.asynchronousExceptionHandler")
@Install(precedence=BUILT_IN)
@BypassInterceptors
public class AsynchronousExceptionHandler
{

   private LogProvider log = Logging.getLogProvider(AsynchronousExceptionHandler.class);
   
   public void handleException(Exception throwable)
   {
      log.error("Exception thrown whilst executing asynchronous call", throwable);
   }
   
   public static AsynchronousExceptionHandler instance()
   {
      return (AsynchronousExceptionHandler) Component.getInstance(AsynchronousExceptionHandler.class);
   }
   
}
