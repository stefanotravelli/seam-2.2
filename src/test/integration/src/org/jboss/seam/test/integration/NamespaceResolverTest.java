package org.jboss.seam.test.integration;

import org.jboss.seam.init.NamespacePackageResolver;
import org.jboss.seam.mock.SeamTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NamespaceResolverTest 
    extends SeamTest
{
	NamespacePackageResolver resolver = new NamespacePackageResolver();

	@Test
	public void testResolver() {
		
		test("java:foo", "foo");
		test("java:com.company.department",
		     "com.company.department");
		test("java:com.company.department.product", 
		     "com.company.department.product");
		test("http://www.company.com/department/product",
		     "com.company.department.product");
		test("https://my-company.com/department/product",
			 "com.my_company.department.product");
		test("http://ericjung:password@www.company.com:8080/foo/bar/baz#anchor?param1=332&param2=334",
			 "com.company.foo.bar.baz");
		test("http://cats.import.com",
             "com.import.cats");
		
		
		//testFail("http://bar#foo#com");
		
		testFail("java:");
		
		// need to think about this one
		//testFail("java:foo!bar");

		testFail("mailto:java-net@java.sun.com");
		testFail("news:comp.lang.java");
		testFail("urn:isbn:096139210x");
		
	}

	private void test(String namespace, String packageName) {
		Assert.assertEquals(resolver.resolve(namespace), packageName);
	}
	
	private void testFail(String namespace) {
		Assert.assertNull(resolver.resolve(namespace), namespace);
	}
}
