package org.jboss.seam.transaction;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Dummy component that lets us install the
 * EjbSynchronizations via the tag
 * transaction:ejb-transaction
 *
 * @see EjbSynchronizations
 * @author Gavin King
 * 
 */
@Name("org.jboss.seam.transaction.ejbTransaction")
@Scope(ScopeType.STATELESS)
@Install(precedence=BUILT_IN, value=false)
@BypassInterceptors
public class EjbTransaction {}
