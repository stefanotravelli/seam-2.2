package org.jboss.seam.drools;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.drools.RuleBaseFactory;
import org.drools.compiler.DroolsError;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.compiler.RuleError;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.ResourceLoader;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * Manager component for a Drools RuleBase
 * 
 * @author Gavin King
 *
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
public class RuleBase
{
   private static final LogProvider log = Logging.getLogProvider(RuleBase.class);
   
   private String[] ruleFiles;
   private String dslFile;
   private org.drools.RuleBase ruleBase;
   
   @Create
   public void compileRuleBase() throws Exception
   {
      PackageBuilderConfiguration conf = new PackageBuilderConfiguration();
      PackageBuilder builder = new PackageBuilder(conf);
      
      if (ruleFiles!=null)
      {
         for (String ruleFile: ruleFiles)
         {
            log.debug("parsing rules: " + ruleFile);
            InputStream stream = ResourceLoader.instance().getResourceAsStream(ruleFile);
            if (stream==null)
            {
               throw new IllegalStateException("could not locate rule file: " + ruleFile);
            }
            // read in the source
            Reader drlReader = new InputStreamReader(stream);
                                  
            if (dslFile==null)
            {
               builder.addPackageFromDrl(drlReader);               
            }
            else
            {
               Reader dslReader = new InputStreamReader( ResourceLoader.instance().getResourceAsStream(dslFile) );
               builder.addPackageFromDrl(drlReader, dslReader);
            }
            
            if ( builder.hasErrors() )
            {
               log.error("errors parsing rules in: " + ruleFile);               
               for ( DroolsError error: builder.getErrors().getErrors() )
               {
                  if (error instanceof RuleError)
                  {
                     RuleError ruleError = (RuleError) error;
                     log.error( ruleError.getMessage() + " (" + ruleFile + ':' + ruleError.getLine() + ')' );                     
                  }
                  else
                  {
                     log.error( error.getMessage() + " (" + ruleFile + ')' );                     
                  }
               }
            }
         }
      }
      
      // add the package to a rulebase
      ruleBase = RuleBaseFactory.newRuleBase();
      ruleBase.addPackage( builder.getPackage() );
   }
   
   @Unwrap
   public org.drools.RuleBase getRuleBase()
   {
      return ruleBase;
   }
   
   public String[] getRuleFiles()
   {
      return ruleFiles;
   }
   
   public void setRuleFiles(String[] ruleFiles)
   {
      this.ruleFiles = ruleFiles;
   }
   
   public String getDslFile()
   {
      return dslFile;
   }
   
   public void setDslFile(String dslFile)
   {
      this.dslFile = dslFile;
   }
   
}
