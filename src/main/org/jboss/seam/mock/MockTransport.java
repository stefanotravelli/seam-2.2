package org.jboss.seam.mock;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import org.jboss.seam.contexts.Contexts;

/**
 * Provides a MockTransport for integration testing Seam Mail.
 * {@link MockTransport#getMailMessage()} returns the most recently rendered 
 * message sent using the MockTransport.
 * 
 * To enable the mock transport, set the mailSession.transport property to mock
 * in components.properties.
 * 
 * @see javax.mail.Transport
 *
 * @author Pete Muir
 *
 */
public class MockTransport extends Transport 
{

    private static final String VAR_NAME = "org.jboss.seam.mock.mailMessage";
    
    public MockTransport(Session session, URLName urlname) 
    {
        super(session, urlname);
    }

    @Override
    public void sendMessage(Message message, Address[] recipients)
            throws MessagingException 
    {
        Contexts.getApplicationContext().set(VAR_NAME, message);
    }
    
    /**
     * Get the most recently rendered message sent using the MockTransport.
     */
    public static void clearMailMessage()
    {
        Contexts.getApplicationContext().remove(VAR_NAME);
    }
    
    /**
     * Get the most recently rendered message sent using the MockTransport.
     */
    public static MimeMessage getMailMessage()
    {
        return (MimeMessage) Contexts.getApplicationContext().get(VAR_NAME);
    }
    
    @Override
    public void connect() throws MessagingException
    {
        // No-op
    }

}
