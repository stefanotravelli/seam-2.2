package org.jboss.seam.remoting;

import javax.servlet.ServletContext;

/**
 *
 * @author Shane Bryzak
 */
public abstract class BaseRequestHandler implements RequestHandler
{
  public void setServletContext(ServletContext context) {}
}
