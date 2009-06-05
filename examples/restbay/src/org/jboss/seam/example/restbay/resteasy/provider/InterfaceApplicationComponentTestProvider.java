package org.jboss.seam.example.restbay.resteasy.provider;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * @author Christian Bauer
 */
@Provider
@Produces("text/plain")
public interface InterfaceApplicationComponentTestProvider extends MessageBodyWriter
{

}