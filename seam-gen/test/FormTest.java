package @testPackage@;

import org.testng.annotations.Test;
import org.jboss.seam.mock.SeamTest;

public class @interfaceName@Test extends SeamTest {

	@Test
	public void test_@methodName@() throws Exception {
		new FacesRequest("/@pageName@.xhtml") {
			@Override
			protected void updateModelValues() throws Exception {				
				//set form input to model attributes
				setValue("#{@componentName@.value}", "seam");
			}
			@Override
			protected void invokeApplication() {
				//call action methods here
				invokeMethod("#{@componentName@.@methodName@}");
			}
			@Override
			protected void renderResponse() {
				//check model attributes if needed
				assert getValue("#{@componentName@.value}").equals("seam");
			}
		}.run();
	}
}
