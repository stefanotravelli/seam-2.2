package org.jboss.seam.exception;

import static org.jboss.seam.annotations.Install.BUILT_IN;
import static org.jboss.seam.exception.ExceptionHandler.LogLevel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Init;
import org.jboss.seam.core.ResourceLoader;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.util.Reflections;
import org.jboss.seam.util.Resources;
import org.jboss.seam.util.Strings;
import org.jboss.seam.util.XML;

/**
 *  Manages the exception handler chain
 * 
 * @author Gavin King
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Install(precedence=BUILT_IN, classDependencies="javax.faces.context.FacesContext")
@Name("org.jboss.seam.exception.exceptions")
public class Exceptions
{
   private static final LogProvider log = Logging.getLogProvider(Exceptions.class);
   
   private List<ExceptionHandler> exceptionHandlers = new ArrayList<ExceptionHandler>();
   
   public void handle(Exception e) throws Exception
   {
      if ( Contexts.isConversationContextActive() )
      {
         Contexts.getConversationContext().set("org.jboss.seam.caughtException", e);
      }
      
      //build a list of the nested exceptions
      List<Exception> causes = new ArrayList<Exception>();
      for (Exception cause=e; cause!=null; cause=org.jboss.seam.util.Exceptions.getCause(cause))
      {
         causes.add(cause);
      }
      //try to match each handler in turn
      for (ExceptionHandler eh: exceptionHandlers)
      {
         //Try to handle most-nested exception before least-nested
         for (int i=causes.size()-1; i>=0; i--)
         {
            Exception cause = causes.get(i);
            if ( eh.isHandler(cause) )
            {
               if ( Contexts.isConversationContextActive() )
               {
                  Contexts.getConversationContext().set("org.jboss.seam.handledException", cause);
               }
               eh.handle(cause);
               
               if (eh.isLogEnabled() && eh.getLogLevel() != null)
               {
                  switch (eh.getLogLevel())
                  {
                     case fatal: 
                        log.fatal("handled and logged exception", e);
                        break;
                     case error:
                        log.error("handled and logged exception", e);
                        break;
                     case warn:
                        log.warn("handled and logged exception", e);
                        break;
                     case info:
                        log.info("handled and logged exception", e);
                        break;
                     case debug: 
                        log.debug("handled and logged exception", e);
                        break;
                     case trace:
                        log.trace("handled and logged exception", e);
                  }
               }
               
               Events.instance().raiseEvent("org.jboss.seam.exceptionHandled." + cause.getClass().getName(), cause);
               Events.instance().raiseEvent("org.jboss.seam.exceptionHandled", cause);
               return;
            }
         }
      }
      
      //finally, rethrow it, since no handler was found
      Events.instance().raiseEvent("org.jboss.seam.exceptionNotHandled", e);
      throw e;
   }
   
   @Create
   public void initialize() throws Exception 
   {
      List<ExceptionHandler> deferredHandlers = new ArrayList<ExceptionHandler>();
      
      deferredHandlers.add(parse("/WEB-INF/exceptions.xml")); // deprecated
      
      for (String pageFile: Pages.instance().getResources()) 
      {
          deferredHandlers.add(parse(pageFile));
      }
                    
      addHandler(new AnnotationRedirectHandler());
      addHandler(new AnnotationErrorHandler());
      
      if (Init.instance().isDebugPageAvailable()) 
      {
         addHandler(new DebugPageHandler());
      }
            
      for (ExceptionHandler handler: deferredHandlers) 
      {
          addHandler(handler);
      }
   }

   private void addHandler(ExceptionHandler handler)
   {
      if (handler!=null) exceptionHandlers.add(handler);
   }
   
   private ExceptionHandler parse(String fileName) throws DocumentException, ClassNotFoundException
   {
      ExceptionHandler anyhandler = null;
      InputStream stream = ResourceLoader.instance().getResourceAsStream(fileName);
      if (stream!=null)
      {
          log.debug("reading exception mappings from " + fileName);

          List<Element> elements = null;
          try {
              elements = XML.getRootElement(stream).elements("exception");
          } finally {
              Resources.closeStream(stream);
          }
      
         for (final Element exception: elements)
         {
            String className = exception.attributeValue("class");
            boolean logEnabled = exception.attributeValue("log") != null ? 
                  Boolean.valueOf(exception.attributeValue("log")) : true;
            
            LogLevel logLevel = LogLevel.error;
            try {
               String levelValue = exception.attributeValue("log-level");
               if (levelValue == null) {
                   levelValue = exception.attributeValue("logLevel");
               }
               
               if (levelValue != null) {
                   logLevel = LogLevel.valueOf(levelValue.toLowerCase());
               }
            } catch (IllegalArgumentException ex) { 
               StringBuilder sb = new StringBuilder();
               sb.append("Exception handler");
               if (className != null) sb.append(" for class " + className);
               sb.append(" is configured with an invalid log-level.  Acceptable " +
                         "values are: fatal,error,warn,info,debug,trace. " +
                         "A default level of 'error' has been configured instead.");               
               log.warn(sb.toString());
            }
            
            if (className==null)
            {
               anyhandler = createHandler(exception, Exception.class);
               anyhandler.setLogEnabled(logEnabled);
               anyhandler.setLogLevel(logLevel);
            }
            else
            {
                ExceptionHandler handler = null;

                try { 
                    handler = createHandler(exception, 
                          Reflections.classForName(className));
                    
                    if (handler != null) {
                        handler.setLogEnabled(logEnabled);
                        handler.setLogLevel(logLevel);   
                    }
                } catch (ClassNotFoundException e) {
                    log.error("Can't find exception class for exception handler", e);
                }
               if (handler!=null) exceptionHandlers.add(handler);
            }
            
            
         }
      }
      return anyhandler;
   }

   private ExceptionHandler createHandler(Element exception, final Class clazz)
   {
      final boolean endConversation = exception.elementIterator("end-conversation").hasNext();
      
      Element redirect = exception.element("redirect");
      if (redirect!=null)
      {
         String viewId = redirect.attributeValue("view-id");
         Element messageElement = redirect.element("message");
         final String message = messageElement==null ? null : messageElement.getTextTrim();
         String severityName = messageElement==null ? null : messageElement.attributeValue("severity");
         Severity severity = severityName==null ? 
                  FacesMessage.SEVERITY_INFO : 
                  Pages.getFacesMessageValuesMap().get( severityName.toUpperCase() );
         return new ConfigRedirectHandler(viewId == null ? null : Expressions.instance().createValueExpression(
               viewId, String.class), clazz, endConversation, message, severity);
      }
      
      Element error = exception.element("http-error");
      if (error!=null)
      {
         String errorCode = error.attributeValue("error-code");
         final int code = Strings.isEmpty(errorCode) ? 
               500 : Integer.parseInt(errorCode);
         Element messageElement = error.element("message");
         final String message = messageElement==null ? null : messageElement.getTextTrim();
         return new ConfigErrorHandler(message, endConversation, clazz, code);
      }
      
      return null;
   }
   
   /**
    * @return the exception handler list, which supports addition and removal
    *         of handlers
    */
   public List<ExceptionHandler> getHandlers()
   {
      return exceptionHandlers;
   }

   public static Exceptions instance()
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("No active application context");
      }
      return (Exceptions) Component.getInstance(Exceptions.class, ScopeType.APPLICATION);
   }

}
