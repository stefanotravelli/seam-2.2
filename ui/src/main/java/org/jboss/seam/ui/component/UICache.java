/**
 * License Agreement.
 * 
 * Ajax4jsf 1.1 - Natural Ajax for Java Server Faces (JSF)
 * 
 * Copyright (C) 2007 Exadel, Inc.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 2.1 as published
 * by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.jboss.seam.ui.component;

import javax.faces.component.UIComponentBase;

import org.jboss.seam.cache.CacheProvider;


/**
 * JSF component class
 * 
 */
public abstract class UICache extends UIComponentBase
{
   
   public abstract boolean isEnabled();
   
   public abstract void setEnabled(boolean enabled);
   
   public abstract String getKey();
   
   public abstract void setKey(String key);
   
   public abstract String getRegion();
   
   public abstract void setRegion(String region);
   
   public abstract CacheProvider getCacheProvider();
   
   public abstract void setCacheProvider(CacheProvider cacheProvider);

}
