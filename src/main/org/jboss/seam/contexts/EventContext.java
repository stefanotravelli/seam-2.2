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
 * Event context - spans a single request to
 * the server.
 * 
 * @author Gavin King
 */
public class EventContext extends BasicContext 
{
   
   public EventContext(Map<String, Object> map)
   {
      super(ScopeType.EVENT, map);
   }
  
}
