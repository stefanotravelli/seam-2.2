package org.jboss.seam.bpm;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.NamingException;

import org.dom4j.Element;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Init;
import org.jboss.seam.core.ResourceLoader;
import org.jboss.seam.deployment.DeploymentStrategy;
import org.jboss.seam.deployment.FileDescriptor;
import org.jboss.seam.deployment.StandardDeploymentStrategy;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Naming;
import org.jboss.seam.util.Resources;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.node.DbSubProcessResolver;
import org.jbpm.graph.node.ProcessState;
import org.jbpm.graph.node.SubProcessResolver;
import org.jbpm.instantiation.UserCodeInterceptorConfig;
import org.jbpm.jpdl.JpdlException;
import org.jbpm.jpdl.el.impl.JbpmExpressionEvaluator;
import org.jbpm.persistence.db.DbPersistenceServiceFactory;
import org.xml.sax.InputSource;

/**
 * A seam component that boostraps a JBPM SessionFactory
 * 
 * @author Gavin King
 * @author <a href="mailto:steve@hibernate.org">Steve Ebersole</a>
 * @author Norman Richards
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Startup
@Name("org.jboss.seam.bpm.jbpm")
@Install(value=false, precedence=BUILT_IN)
public class Jbpm 
{
   private static final LogProvider log = Logging.getLogProvider(Jbpm.class);
   
   private JbpmConfiguration jbpmConfiguration;
   private String jbpmConfigurationJndiName;
   private String[] processDefinitions;
   private String[] pageflowDefinitions;
   private Map<String, ProcessDefinition> pageflowProcessDefinitions = new HashMap<String, ProcessDefinition>();

   @Create
   public void startup() throws Exception
   {
      log.debug( "Starting jBPM" );
      ProcessState.setDefaultSubProcessResolver( new SeamSubProcessResolver() );
      installProcessDefinitions();
      installPageflowDefinitions();
      //JbpmExpressionEvaluator.setVariableResolver( new SeamVariableResolver() );
      //JbpmExpressionEvaluator.setFunctionMapper( new SeamFunctionMapper() );
      JbpmExpressionEvaluator.setExpressionEvaluator( new SeamExpressionEvaluator() );
      UserCodeInterceptorConfig.setUserCodeInterceptor( new SeamUserCodeInterceptor() );
   }

   @Destroy
   public void shutdown()
   {
      if (jbpmConfiguration!=null) 
      {
         jbpmConfiguration.close();
      }
   }
   
   public JbpmConfiguration getJbpmConfiguration()
   {
      if (jbpmConfiguration==null)
      {
         initJbpmConfiguration();
      }
      return jbpmConfiguration;
   }

   private void initJbpmConfiguration()
   {
      if (jbpmConfigurationJndiName==null)
      {
         jbpmConfiguration = JbpmConfiguration.getInstance();
      }
      else
      {
         try
         {
            jbpmConfiguration = (JbpmConfiguration) Naming.getInitialContext().lookup(jbpmConfigurationJndiName);
         }
         catch (NamingException ne)
         {
            throw new IllegalArgumentException("JbpmConfiguration not found in JNDI", ne);
         }
      }

      DbPersistenceServiceFactory dbpsf = (DbPersistenceServiceFactory) jbpmConfiguration.getServiceFactory("persistence");
      if (Naming.getInitialContextProperties()!=null)
      {
         // Prefix regular JNDI properties for Hibernate
         Hashtable<String, String> hash = Naming.getInitialContextProperties();
         Properties prefixed = new Properties();
         for (Map.Entry<String, String> entry: hash.entrySet() )
         {
            prefixed.setProperty( Environment.JNDI_PREFIX + "." + entry.getKey(), entry.getValue() );
         }
         
         try
         {
            dbpsf.getConfiguration().getProperties().putAll(prefixed);
         }
         catch (HibernateException he)
         {
            log.warn("could not set JNDI properties for jBPM persistence: " + he.getMessage());
         }
      }
   }

   public ProcessDefinition getPageflowProcessDefinition(String pageflowName)
   {
      return pageflowProcessDefinitions.get(pageflowName);
   }
   
   public boolean isPageflowProcessDefinition(String pageflowName)
   {
      return pageflowProcessDefinitions.containsKey(pageflowName);
   }
   
   public static ProcessDefinition getPageflowDefinitionFromResource(String resourceName)
   {
      InputStream resource = ResourceLoader.instance().getResourceAsStream(resourceName);
      if (resource==null)
      {
         throw new IllegalArgumentException("pageflow resource not found: " + resourceName);
      }
      try
      {
         return Jbpm.parseInputSource( new InputSource(resource) );
      }
      catch (JpdlException e)
      {
         throw new JpdlException("Unable to parse process definition " + resourceName, e);
      } finally {
          Resources.closeStream(resource);
      }
   }
   
   public ProcessDefinition getProcessDefinitionFromResource(String resourceName) 
   {
      InputStream resource = ResourceLoader.instance().getResourceAsStream(resourceName);
      if (resource==null)
      {
         throw new IllegalArgumentException("process definition resource not found: " + resourceName);
      }
      
      try {
          return ProcessDefinition.parseXmlInputStream(resource);
      } finally {
          Resources.closeStream(resource);
      }
   }

   public String[] getPageflowDefinitions() 
   {
      return pageflowDefinitions;
   }

   public void setPageflowDefinitions(String[] pageflowDefinitions) 
   {
      this.pageflowDefinitions = pageflowDefinitions;
   }
   
   public String[] getProcessDefinitions() 
   {
      return processDefinitions;
   }

   public void setProcessDefinitions(String[] processDefinitions) 
   {
      this.processDefinitions = processDefinitions;
   }
   
   /**
    * Dynamically deploy a page flow definition, if a pageflow with an 
    * identical name already exists, the pageflow is updated.
    * 
    * @return true if the pageflow definition has been updated
    */
   public boolean deployPageflowDefinition(ProcessDefinition pageflowDefinition) 
   {
      return pageflowProcessDefinitions.put( pageflowDefinition.getName(), pageflowDefinition )!=null;
   }
   
   /**
    * Read a pageflow definition
    * 
    * @param pageflowDefinition the pageflow as an XML string
    */
   public ProcessDefinition getPageflowDefinitionFromXml(String pageflowDefinition)
   {
      Reader reader = null;
      try {
          reader = new StringReader(pageflowDefinition);
          //stream = new ReaderInputStream(new StringReader(pageflowDefinition));
          return Jbpm.parseReaderSource(reader);
      } finally {
          Resources.closeReader(reader);
      }       
   }
   
   /**
    * Read a process definition
    * 
    * @param processDefinition the process as an XML string
    */
   public ProcessDefinition getProcessDefinitionFromXml(String processDefinition)
   {
       //InputStream stream = null;
      Reader reader = null;
       try {
           //stream = new ReaderInputStream(new StringReader(processDefinition));
           reader =  new StringReader(processDefinition);
           return ProcessDefinition.parseXmlReader(reader);
       } finally {
           Resources.closeReader(reader);
       }
   }
   
   /**
    * Remove a pageflow definition
    * 
    * @param pageflowName Name of the pageflow to remove
    * @return true if the pageflow definition has been removed
    */
   public boolean undeployPageflowDefinition(String pageflowName) 
   {     
      return pageflowProcessDefinitions.remove(pageflowName)!=null;
   }
   
   private void installPageflowDefinitions() {
      Set<String> mergedPageflowDefinitions = new LinkedHashSet<String>();
      if ( pageflowDefinitions!=null )
      {
         mergedPageflowDefinitions.addAll(Arrays.asList(pageflowDefinitions));
      }
      
      for (FileDescriptor fileDescriptor : ((PageflowDeploymentHandler) ((DeploymentStrategy) Contexts.getEventContext().get(StandardDeploymentStrategy.NAME)).getDeploymentHandlers().get(PageflowDeploymentHandler.NAME)).getResources())
      {
         mergedPageflowDefinitions.add(fileDescriptor.getName());
      }
      
      for (String pageflow: mergedPageflowDefinitions)
      {
         ProcessDefinition pd = getPageflowDefinitionFromResource(pageflow);
         pageflowProcessDefinitions.put( pd.getName(), pd );
      }
   }

   private void installProcessDefinitions()
   {
      if ( isProcessDeploymentEnabled() )
      {
         JbpmContext jbpmContext = getJbpmConfiguration().createJbpmContext();
         try
         {
            if (processDefinitions!=null)
            {
               for ( String definitionResource : processDefinitions )
               {
                  deployProcess(jbpmContext, definitionResource);
               }
            }
         }
         catch (RuntimeException e)
         {
            throw new RuntimeException("could not deploy a process definition", e);
         }
         finally
         {
            jbpmContext.close();
         }
      }
   }

   private void deployProcess(JbpmContext jbpmContext, String definitionResource)
   {
      ProcessDefinition processDefinition = ProcessDefinition.parseXmlResource(definitionResource);
      if (log.isDebugEnabled())
      {
         log.debug( "deploying process definition : " + processDefinition.getName() );
      }
      jbpmContext.deployProcessDefinition(processDefinition);
   }

   protected boolean isProcessDeploymentEnabled()
   {
      return processDefinitions!=null && processDefinitions.length>0;
   }
   
   public static Jbpm instance()
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("No application context active");
      }
      if ( !Init.instance().isJbpmInstalled() )
      {
         throw new IllegalStateException("jBPM support is not installed (use components.xml to install it)");
      }
      return (Jbpm) Component.getInstance(Jbpm.class, ScopeType.APPLICATION);
   }

   protected String getJbpmConfigurationJndiName()
   {
      return jbpmConfigurationJndiName;
   }

   protected void setJbpmConfigurationJndiName(String jbpmConfigurationJndiName)
   {
      this.jbpmConfigurationJndiName = jbpmConfigurationJndiName;
   }
   
   public static JbpmConfiguration pageflowConfiguration = JbpmConfiguration.parseResource("org/jboss/seam/bpm/jbpm.pageflow.cfg.xml");

   public static JbpmContext createPageflowContext() 
   {
      return pageflowConfiguration.createJbpmContext();
   }

   public static ProcessDefinition parseInputSource(InputSource inputSource) 
   {
      JbpmContext jbpmContext = createPageflowContext();
      try 
      {
         return new PageflowParser(inputSource).readProcessDefinition();
      }
      finally 
      {
         jbpmContext.close();
      }
   }
   
   public static ProcessDefinition parseReaderSource(Reader reader) 
   {
      JbpmContext jbpmContext = createPageflowContext();
      try 
      {
         return new PageflowParser(reader).readProcessDefinition();
      }
      finally 
      {
         jbpmContext.close();
      }
   }

   private static final DbSubProcessResolver DB_SUB_PROCESS_RESOLVER = new DbSubProcessResolver();
   class SeamSubProcessResolver implements SubProcessResolver
   {
      public ProcessDefinition findSubProcess(Element element)
      {
         String subProcessName = element.attributeValue("name");
         ProcessDefinition pageflow = pageflowProcessDefinitions.get(subProcessName);
         return pageflow==null ? DB_SUB_PROCESS_RESOLVER.findSubProcess(element) : pageflow;
      }
   }
   
}
