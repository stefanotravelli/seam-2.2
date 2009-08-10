package org.jboss.seam.jmx;

import javax.management.ObjectName;

import org.jboss.seam.annotations.Unwrap;

public class Mbean
{
    String objectName;
    String agentId;
    String proxyClass;
    
    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getObjectName() {
        return objectName;
    }
   
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }
   
    public String getProxyClass() {
        return proxyClass;
    }
   
    public void setProxyClass(String proxyClass) {
        this.proxyClass = proxyClass;
    }
   
    @Unwrap 
    public Object createProxy() {
        try {
            Object o = MBeanProxy.get(Class.forName(proxyClass), new ObjectName(getObjectName()), getAgentId());
            return o;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
