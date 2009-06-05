package org.jboss.seam.deployment;

import java.util.Set;

public interface ClassDeploymentHandler extends DeploymentHandler {
    
    public ClassDeploymentMetadata getMetadata();
    
    public Set<ClassDescriptor> getClasses();
    
    public void setClasses(Set<ClassDescriptor> classes);

}
