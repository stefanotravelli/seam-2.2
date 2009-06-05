package org.jboss.seam.framework;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;

/**
 * The current time, as an instance of java.util.Date.
 * 
 * @author Gavin King
 *
 */
@Name("org.jboss.seam.framework.currentTime")
@Install(precedence=BUILT_IN)
@Scope(ScopeType.STATELESS)
@AutoCreate
public class CurrentTime
{
   @Unwrap 
   public Date getCurrentTime()
   {
      return new java.sql.Time( System.currentTimeMillis() );
   }
}
