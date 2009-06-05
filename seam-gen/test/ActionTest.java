package @testPackage@;

import org.testng.annotations.Test;
import org.jboss.seam.mock.SeamTest;

public class @interfaceName@Test extends SeamTest {

	@Test
	public void test_@methodName@() throws Exception {
		new FacesRequest("/@pageName@.xhtml") {
			@Override
			protected void invokeApplication() {
				//call action methods here
				invokeMethod("#{@componentName@.@methodName@}");
			}
		}.run();
	}
}
