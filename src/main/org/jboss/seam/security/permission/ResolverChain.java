package org.jboss.seam.security.permission;

import static org.jboss.seam.ScopeType.SESSION;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * A chain of permission resolvers.  Specific permission checks are generally mapped to a 
 * particular ResolverChain for resolution. 
 * 
 * @author Shane Bryzak
 */
@Scope(SESSION)
@BypassInterceptors
public class ResolverChain implements Serializable
{
   private List<PermissionResolver> resolvers = new ArrayList<PermissionResolver>();
   
   public List<PermissionResolver> getResolvers()
   {
      return resolvers;
   }
   
   public void setResolvers(List<PermissionResolver> resolvers)
   {
      this.resolvers = resolvers;
   }   
}
