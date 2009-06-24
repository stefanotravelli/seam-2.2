package org.jboss.seam.drools;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.drools.decisiontable.InputType;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * Compiles Drools DecisionTable.
 * 
 * @author Tihomir Surdilovic
 *
 */

@Name("org.jboss.seam.drools.spreadsheetComponent")
@BypassInterceptors
@Scope(APPLICATION)
@Install(precedence = BUILT_IN, classDependencies = "org.drools.decisiontable.SpreadsheetCompiler")
public class SpreadsheetCompiler
{
    private static final LogProvider log = Logging.getLogProvider(SpreadsheetCompiler.class);  
   
    public InputStreamReader compile(InputStream stream) {
	org.drools.decisiontable.SpreadsheetCompiler compiler = new org.drools.decisiontable.SpreadsheetCompiler();
	String drl = compiler.compile(stream, InputType.XLS);

	byte currentXMLBytes[] = drl.getBytes();
	InputStreamReader source = new InputStreamReader(new ByteArrayInputStream(currentXMLBytes));
	return source;
    }
   
    public static SpreadsheetCompiler instance()
    {
	if (!Contexts.isApplicationContextActive()) {
	    return new SpreadsheetCompiler();
	} else {
	    return (SpreadsheetCompiler) Component.getInstance(SpreadsheetCompiler.class, ScopeType.APPLICATION);
	}
    }
}