package org.jboss.seam.wiki.core.dao;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;

@Name("clientAddressFactory")
public class ClientAddressFactory
{
   @Factory(value = "clientAddress", scope = ScopeType.EVENT, autoCreate = true)
   public String getClientAddress()
   {
      Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
      if (request instanceof HttpServletRequest)
      {
         return ((HttpServletRequest) request).getRemoteAddr();
      }
      
      return null;
   }
}
