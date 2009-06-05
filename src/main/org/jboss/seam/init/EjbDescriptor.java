package org.jboss.seam.init;

import org.jboss.seam.ComponentType;

/**
 * Meta-data about an EJB, discovered from a deployment
 * descriptor.
 * 
 * @author Norman Richards
 *
 */
public class EjbDescriptor 
{
     private String ejbName;
     private String ejbClassName;
     private ComponentType beanType;
     
     public ComponentType getBeanType() 
     {
         return beanType;
     }
     
     public void setBeanType(ComponentType beanType) 
     {
         this.beanType = beanType;
     }
     
     public String getEjbClassName() 
     {
         return ejbClassName;
     }
     
     public void setEjbClassName(String ejbClass) 
     {
         this.ejbClassName = ejbClass;
     }
     
     public String getEjbName() 
     {
         return ejbName;
     }
     
     public void setEjbName(String name) 
     {
         this.ejbName = name;
     }
}