/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.contexts;

import java.util.Map;

import org.jboss.seam.ScopeType;

/**
 * Application context - state associated with a 
 * deployment to a particular node. State is typically
 * neither persistent across deployments, not 
 * replicated to other nodes.
 * 
 * @author Gavin King
 */
public class ApplicationContext extends BasicContext 
{
   
   public ApplicationContext(Map<String, Object> map)
   {
      super(ScopeType.APPLICATION, map);
   }
  
}
