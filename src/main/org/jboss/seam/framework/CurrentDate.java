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
 * The current date, as an instance of java.util.Date.
 * 
 * @author Gavin King
 *
 */
@Name("org.jboss.seam.framework.currentDate")
@Install(precedence=BUILT_IN)
@Scope(ScopeType.STATELESS)
@AutoCreate
public class CurrentDate
{
   @Unwrap 
   public Date getCurrentDate()
   {
      return new java.sql.Date( System.currentTimeMillis() );
   }
}
