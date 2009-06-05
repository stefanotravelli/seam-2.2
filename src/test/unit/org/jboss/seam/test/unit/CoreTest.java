//$Id$
package org.jboss.seam.test.unit;

import java.util.HashMap;

import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.mock.MockServletContext;
import org.testng.annotations.BeforeSuite;

public class CoreTest
{
	/**
	 * The purpose of this test (any why it wraps the suite) is to trigger
	 * a component scan to ensure that it can execute without errors. That
	 * allows the other tests to execute knowing that the environment is sane.
	 */
	@BeforeSuite
	public void triggerComponentScan()
	{
      Lifecycle.beginApplication(new HashMap<String, Object>());
      new Initialization(new MockServletContext()).create().init();
      Lifecycle.endApplication();
	}
}
