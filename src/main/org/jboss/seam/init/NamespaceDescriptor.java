package org.jboss.seam.init;

import java.util.HashSet;
import java.util.Set;

import org.jboss.seam.annotations.Namespace;

class NamespaceDescriptor
{
	private String namespace;
	private Set<String> packageNames = new HashSet<String>();
	private String componentPrefix;

	NamespaceDescriptor(Namespace namespaceAnnotation, Package pkg)
	{
		this.namespace       = namespaceAnnotation.value();
		this.componentPrefix = namespaceAnnotation.prefix();
		packageNames.add(pkg.getName());
	}
	
	NamespaceDescriptor(String namespace, String packageName) {
		this.namespace       = namespace;
		packageNames.add(packageName);
		this.componentPrefix = "";
	}

	public String getNamespace() {
		return namespace;
	}
	
	public String getComponentPrefix() {
		return componentPrefix;
	}
	
	public void addPackageName(String packageName)
	{
	   packageNames.add(packageName);
	}

	public Set<String> getPackageNames() {
		return packageNames;
	}

	@Override
	public String toString()
	{
		return "NamespaceDescriptor(" + namespace + ')';
	}
}