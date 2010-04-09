package org.jboss.seam.example.mail.test;

import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;

import java.io.InputStream;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.document.ByteArrayDocumentData;
import org.jboss.seam.document.DocumentData;
import org.jboss.seam.example.mail.Person;
import org.jboss.seam.mail.MailSession;
import org.jboss.seam.mail.ui.UIAttachment;
import org.jboss.seam.mail.ui.UIMessage;
import org.jboss.seam.mock.MockTransport;
import org.jboss.seam.mock.SeamTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Pete Muir
 *
 */
public class MailTest extends SeamTest
{
    
    @Test
    public void testSimple() throws Exception
    {
        
        new FacesRequest()
        {

            @Override
            protected void updateModelValues() throws Exception
            {
                setValue("#{person.firstname}", "Pete");
                setValue("#{person.lastname}", "Muir");
                setValue("#{person.address}", "test@example.com");
            }
            
            @Override
            protected void invokeApplication() throws Exception
            {
                MimeMessage renderedMessage = getRenderedMailMessage("/simple.xhtml");
             
                Assert.assertTrue(MailSession.instance().getTransport() instanceof MockTransport);
                
                // Test the headers
                
                Assert.assertNotNull(renderedMessage);
                Assert.assertEquals(renderedMessage.getAllRecipients().length, 1);
                Assert.assertTrue(renderedMessage.getAllRecipients()[0] instanceof InternetAddress);
                InternetAddress to = (InternetAddress) renderedMessage.getAllRecipients()[0];
                Assert.assertEquals(to.getAddress(), "test@example.com");
                Assert.assertEquals(to.getPersonal(), "Pete Muir");
                Assert.assertEquals(renderedMessage.getFrom().length, 1);
                Assert.assertTrue(renderedMessage.getFrom()[0] instanceof InternetAddress);
                InternetAddress from = (InternetAddress) renderedMessage.getFrom()[0];
                Assert.assertEquals(from.getAddress(), "peter@example.com");
                Assert.assertEquals(from.getPersonal(), "Peter");
                Assert.assertEquals(renderedMessage.getSubject(), "Try out Seam!");
                Assert.assertNull(renderedMessage.getHeader("Precedence"));
                Assert.assertNull(renderedMessage.getHeader("X-Priority"));
                Assert.assertNull(renderedMessage.getHeader("Priority"));
                Assert.assertNull(renderedMessage.getHeader("Importance"));
                Assert.assertNull(renderedMessage.getHeader("Disposition-Notification-To"));
                
                // Check the body
                
                Assert.assertNotNull(renderedMessage.getContent());
                Assert.assertTrue(renderedMessage.getContent() instanceof MimeMultipart);
                MimeMultipart body = (MimeMultipart) renderedMessage.getContent();
                Assert.assertEquals(body.getCount(), 1);
                Assert.assertNotNull(body.getBodyPart(0));
                Assert.assertTrue(body.getBodyPart(0) instanceof MimeBodyPart);
                MimeBodyPart bodyPart = (MimeBodyPart) body.getBodyPart(0);
                Assert.assertNotNull(bodyPart.getContent());
                Assert.assertEquals(bodyPart.getDisposition(), "inline");
                Assert.assertTrue(bodyPart.isMimeType("text/html"));                
            }            
        }.run();
       
    }    
        
    @Test
    public void testAttachment() throws Exception
    {
        
        new FacesRequest()
        {

            @Override
            protected void updateModelValues() throws Exception
            {
                setValue("#{person.firstname}", "Gavin");
                setValue("#{person.lastname}", "King");
                setValue("#{person.address}", "gavin@king.com");
            }
            
            @Override
            protected void invokeApplication() throws Exception
            { 
                MimeMessage renderedMessage = getRenderedMailMessage("/attachment.xhtml");
                
                // Test the headers
                
                InternetAddress to = (InternetAddress) renderedMessage.getAllRecipients()[0];
                Assert.assertEquals(to.getAddress(), "gavin@king.com");
                Assert.assertEquals(to.getPersonal(), "Gavin King");
                InternetAddress from = (InternetAddress) renderedMessage.getFrom()[0];
                Assert.assertEquals(from.getAddress(), "do-not-reply@jboss.com");
                Assert.assertEquals(from.getPersonal(), "Seam");
                Assert.assertEquals(renderedMessage.getSubject(), "Try out Seam!");
                MimeMultipart body = (MimeMultipart) renderedMessage.getContent();                
    
                Assert.assertEquals(body.getCount(), 4); //3 Attachments and 1 MimeMultipart                
                
                // The root multipart/related
                Assert.assertNotNull(body.getBodyPart(0));
                Assert.assertTrue(body.getBodyPart(0) instanceof MimeBodyPart);
                MimeBodyPart bodyPart = (MimeBodyPart) body.getBodyPart(0);
                Assert.assertNotNull(bodyPart.getContent());
                Assert.assertTrue(bodyPart.getContent() instanceof MimeMultipart);
                Assert.assertTrue(bodyPart.isMimeType("multipart/related"));
                
                MimeMultipart attachments = (MimeMultipart) bodyPart.getContent();
                
                Assert.assertEquals(attachments.getCount(), 3); //2 Attachments and 1 MimeMultipart
                
                // Attachment 0 (the actual message)
                Assert.assertNotNull(attachments.getBodyPart(0));                
                Assert.assertTrue(attachments.getBodyPart(0) instanceof MimeBodyPart);
                bodyPart = (MimeBodyPart) attachments.getBodyPart(0);
                Assert.assertNotNull(bodyPart.getContent());
                Assert.assertNotNull(bodyPart.getContent());
                Assert.assertTrue(bodyPart.isMimeType("text/html"));
                Assert.assertEquals(bodyPart.getDisposition(), "inline");
                
                // Inline Attachment 1 // Attachment Jboss Logo
                Assert.assertNotNull(attachments.getBodyPart(1));                
                Assert.assertTrue(attachments.getBodyPart(1) instanceof MimeBodyPart);
                bodyPart = (MimeBodyPart) attachments.getBodyPart(1);
                Assert.assertNotNull(bodyPart.getContent());
                Assert.assertTrue(bodyPart.getContent() instanceof InputStream);
                Assert.assertEquals(bodyPart.getFileName(), "jboss.jpg");
                Assert.assertTrue(bodyPart.isMimeType("image/jpeg"));
                Assert.assertEquals(bodyPart.getDisposition(), "inline");
                Assert.assertNotNull(bodyPart.getContentID());
                
                // Inline Attachment 1 // Attachment Gavin Pic
                Assert.assertNotNull(attachments.getBodyPart(2));                
                Assert.assertTrue(attachments.getBodyPart(2) instanceof MimeBodyPart);
                bodyPart = (MimeBodyPart) attachments.getBodyPart(2);
                Assert.assertNotNull(bodyPart.getContent());
                Assert.assertTrue(bodyPart.getContent() instanceof InputStream);
                Assert.assertEquals(bodyPart.getFileName(), "Gavin_King.jpg");
                Assert.assertTrue(bodyPart.isMimeType("image/png"));
                Assert.assertEquals(bodyPart.getDisposition(), "inline");
                Assert.assertNotNull(bodyPart.getContentID());
                
                // Root Attachment 0
                Assert.assertNotNull(body.getBodyPart(1));                
                Assert.assertTrue(body.getBodyPart(1) instanceof MimeBodyPart);
                bodyPart = (MimeBodyPart) body.getBodyPart(1);
                Assert.assertNotNull(bodyPart.getContent());
                Assert.assertTrue( bodyPart.getContent() instanceof InputStream);
                Assert.assertEquals(bodyPart.getFileName(), "numbers.csv");
                Assert.assertTrue(bodyPart.isMimeType("content/unknown"));
                Assert.assertEquals(bodyPart.getDisposition(), "attachment");
                Assert.assertNull(bodyPart.getContentID());                
                
                // Root Attachment 1
                Assert.assertNotNull(body.getBodyPart(2));                
                Assert.assertTrue(body.getBodyPart(2) instanceof MimeBodyPart);
                bodyPart = (MimeBodyPart) body.getBodyPart(2);
                Assert.assertNotNull(bodyPart.getContent());
                Assert.assertTrue(bodyPart.getContent() instanceof String);
                Assert.assertEquals(bodyPart.getFileName(), "whyseam.pdf");
                Assert.assertEquals(bodyPart.getDisposition(), "attachment");
                Assert.assertNull(bodyPart.getContentID());
                
                // Root Attachment 3
                Assert.assertNotNull(body.getBodyPart(3));                
                Assert.assertTrue(body.getBodyPart(3) instanceof MimeBodyPart);
                bodyPart = (MimeBodyPart) body.getBodyPart(3);
                Assert.assertNotNull(bodyPart.getContent());
                Assert.assertEquals(bodyPart.getFileName(), "excel.xls");
                Assert.assertTrue(bodyPart.isMimeType("application/vnd.ms-excel"));
                Assert.assertEquals(bodyPart.getDisposition(), "attachment");
                Assert.assertNull(bodyPart.getContentID());                
                
            }            
        }.run();
       
    }
    
    /**
     * This test is needed since the PDF is not rendered in the attachment test.
     * If PDF rendering is supported in a test environment, then this test can be
     * removed.
     */
    @Test
    public void testPdfAttachment() throws Exception
    {
       new FacesRequest()
       {
          @Override
          protected void invokeApplication() throws Exception
          {
             UIAttachment attachment = new UIAttachment();
             attachment.setFileName("filename.pdf");
             UIMessage message = new UIMessage();
             attachment.setParent(message);
             message.setMailSession(MailSession.instance());
             DocumentData doc = new ByteArrayDocumentData("filename", new DocumentData.DocumentType("pdf", "application/pdf"), new byte[] {});
             attachment.setValue(doc);
             attachment.encodeEnd(FacesContext.getCurrentInstance());
             
             // verify we built the message
             Assert.assertEquals(message.getAttachments().size(), 1);
             MimeBodyPart bodyPart = message.getAttachments().get(0);
             Assert.assertEquals(bodyPart.getFileName(), "filename.pdf");
             Assert.assertEquals(bodyPart.getDisposition(),"attachment");
          }
       }.run();
    }
    
    @Test
    public void testHtml() throws Exception
    {
        
        new FacesRequest()
        {

            @Override
            protected void updateModelValues() throws Exception
            {
                setValue("#{person.firstname}", "Pete");
                setValue("#{person.lastname}", "Muir");
                setValue("#{person.address}", "test@example.com");
            }
            
            @Override
            protected void invokeApplication() throws Exception
            {   
                MimeMessage renderedMessage = getRenderedMailMessage("/html.xhtml");
             
                // Test the standard headers
                
                InternetAddress to = (InternetAddress) renderedMessage.getAllRecipients()[0];
                Assert.assertEquals(to.getAddress(), "test@example.com");
                Assert.assertEquals(to.getPersonal(), "Pete Muir");
                InternetAddress from = (InternetAddress) renderedMessage.getFrom()[0];
                Assert.assertEquals(from.getAddress(), "do-not-reply@jboss.com");
                Assert.assertEquals(from.getPersonal(), "Seam");
                Assert.assertEquals(renderedMessage.getSubject(), "Seam Mail");
                
                // Test the extra headers
                
                // Importance
                Assert.assertNotNull(renderedMessage.getHeader("X-Priority"));
                Assert.assertNotNull(renderedMessage.getHeader("Priority"));
                Assert.assertNotNull(renderedMessage.getHeader("Importance"));
                Assert.assertEquals(renderedMessage.getHeader("X-Priority").length, 1);
                Assert.assertEquals(renderedMessage.getHeader("Priority").length, 1);
                Assert.assertEquals(renderedMessage.getHeader("Importance").length, 1);
                Assert.assertEquals(renderedMessage.getHeader("X-Priority")[0], "5");
                Assert.assertEquals(renderedMessage.getHeader("Priority")[0], "Non-urgent");
                Assert.assertEquals(renderedMessage.getHeader("Importance")[0], "low");
                
                // read receipt
                Assert.assertNotNull(renderedMessage.getHeader("Disposition-Notification-To"));
                Assert.assertEquals(renderedMessage.getHeader("Disposition-Notification-To").length,  1);
                Assert.assertEquals(renderedMessage.getHeader("Disposition-Notification-To")[0], "Seam <do-not-reply@jboss.com>");
                
                // m:header
                Assert.assertNotNull(renderedMessage.getHeader("X-Sent-From"));
                Assert.assertEquals(renderedMessage.getHeader("X-Sent-From").length, 1);
                Assert.assertEquals(renderedMessage.getHeader("X-Sent-From")[0], "Seam");
                
                MimeMultipart body = (MimeMultipart) renderedMessage.getContent();
                
                // Check the alternative facet
                Assert.assertTrue(renderedMessage.getContentType().startsWith("multipart/mixed"));
                Assert.assertEquals(body.getCount(), 1);
                MimeBodyPart bodyPart = (MimeBodyPart) body.getBodyPart(0);
                Assert.assertTrue(bodyPart.getContentType().startsWith("multipart/alternative"));
                Assert.assertTrue(bodyPart.getContent() instanceof MimeMultipart);
                MimeMultipart bodyParts = (MimeMultipart) bodyPart.getContent();
                Assert.assertEquals(bodyParts.getCount(), 2);
                Assert.assertTrue(bodyParts.getBodyPart(0) instanceof MimeBodyPart);
                Assert.assertTrue(bodyParts.getBodyPart(1) instanceof MimeBodyPart);
                MimeBodyPart alternative = (MimeBodyPart) bodyParts.getBodyPart(0);
                MimeBodyPart html = (MimeBodyPart) bodyParts.getBodyPart(1);
                Assert.assertTrue(alternative.isMimeType("text/plain"));
                Assert.assertEquals(alternative.getDisposition(), "inline");
                Assert.assertTrue(html.isMimeType("text/html"));
                Assert.assertEquals(html.getDisposition(), "inline");

            }            
        }.run();
       
    }
    
    
    @Test
    public void testPlain() throws Exception
    {
        
        new FacesRequest()
        {

            @Override
            protected void updateModelValues() throws Exception
            {
                setValue("#{person.firstname}", "Pete");
                setValue("#{person.lastname}", "Muir");
                setValue("#{person.address}", "test@example.com");
            }
            
            @Override
            protected void invokeApplication() throws Exception
            {   
                MimeMessage renderedMessage = getRenderedMailMessage("/plain.xhtml");
             
                // Test the standard headers
                
                InternetAddress to = (InternetAddress) renderedMessage.getAllRecipients()[0];
                Assert.assertEquals(to.getAddress(), "test@example.com");
                Assert.assertEquals(to.getPersonal(), "Pete Muir");
                InternetAddress from = (InternetAddress) renderedMessage.getFrom()[0];
                Assert.assertEquals(from.getAddress(), "do-not-reply@jboss.com");
                Assert.assertEquals(from.getPersonal(), "Seam");
                Assert.assertEquals(renderedMessage.getReplyTo().length,  1);
                Assert.assertTrue(renderedMessage.getReplyTo()[0] instanceof InternetAddress);
                InternetAddress replyTo = (InternetAddress) renderedMessage.getReplyTo()[0];
                Assert.assertEquals(replyTo.getAddress(), "another.address@jboss.org");
                Assert.assertEquals(replyTo.getPersonal(), "JBoss");
                Assert.assertEquals(renderedMessage.getRecipients(CC).length, 1);
                Assert.assertTrue(renderedMessage.getRecipients(CC)[0] instanceof InternetAddress);
                InternetAddress cc = (InternetAddress) renderedMessage.getRecipients(CC)[0];
                Assert.assertEquals(cc.getAddress(), "test@example.com");
                Assert.assertEquals(cc.getPersonal(), "Pete Muir");
                Assert.assertEquals(renderedMessage.getRecipients(BCC).length, 1);
                Assert.assertTrue(renderedMessage.getRecipients(BCC)[0] instanceof InternetAddress);
                InternetAddress bcc = (InternetAddress) renderedMessage.getRecipients(CC)[0];
                Assert.assertEquals(bcc.getAddress(), "test@example.com");
                Assert.assertEquals(bcc.getPersonal(), "Pete Muir");
                Assert.assertEquals(renderedMessage.getHeader("Precedence")[0], "bulk");
                // Importance
                Assert.assertNotNull(renderedMessage.getHeader("X-Priority"));
                Assert.assertNotNull(renderedMessage.getHeader("Priority"));
                Assert.assertNotNull(renderedMessage.getHeader("Importance"));
                Assert.assertEquals(renderedMessage.getHeader("X-Priority").length, 1);
                Assert.assertEquals(renderedMessage.getHeader("Priority").length, 1);
                Assert.assertEquals(renderedMessage.getHeader("Importance").length, 1);
                Assert.assertEquals(renderedMessage.getHeader("X-Priority")[0], "1");
                Assert.assertEquals(renderedMessage.getHeader("Priority")[0], "Urgent");
                Assert.assertEquals(renderedMessage.getHeader("Importance")[0], "high");
                Assert.assertEquals(renderedMessage.getSubject(), "Plain text email sent by Seam");
                
                // Check the body
                
                Assert.assertNotNull(renderedMessage.getContent());
                MimeMultipart body = (MimeMultipart) renderedMessage.getContent();
                Assert.assertEquals(body.getCount(), 1);
                MimeBodyPart bodyPart = (MimeBodyPart) body.getBodyPart(0);
                Assert.assertNotNull(bodyPart.getContent());
                Assert.assertEquals(bodyPart.getDisposition(), "inline");
                Assert.assertTrue(bodyPart.isMimeType("text/plain"));
            }
        }.run();
    }
    
    @Test
    public void testAttachmentErrors() throws Exception
    {
        
        new FacesRequest()
        {

            @Override
            protected void updateModelValues() throws Exception
            {
                
            }
            
            @Override
            protected void invokeApplication() throws Exception
            {   
                Person person = (Person) getValue("#{person}");
                
                person.setFirstname("Pete");
                person.setLastname("Muir");
                person.setAddress("test@example.com");
                
                // Test for an unavailable attachment
                
                Contexts.getEventContext().set("attachment", "/foo.pdf");
                
            }
        }.run();
    }
    
    @Test
    public void testAddressValidation() throws Exception
    {
        
        new FacesRequest()
        {

            @Override
            protected void updateModelValues() throws Exception
            {
                
            }
            
            @Override
            protected void invokeApplication() throws Exception
            {   
                Person person = (Person) getValue("#{person}");
                
                person.setFirstname("Pete");
                person.setLastname("Muir");                
                boolean exceptionThrown = false;
                          
                person.setAddress("testexample.com");
                
                try
                {
                    getRenderedMailMessage("/org/jboss/seam/example/mail/test/errors2.xhtml");
                }
                catch (FacesException e)
                {
                    Assert.assertTrue(e.getCause() instanceof AddressException);
                    AddressException ae = (AddressException) e.getCause();
                    Assert.assertTrue(ae.getMessage().startsWith("Missing final '@domain'"));
                    exceptionThrown = true;
                }
                Assert.assertTrue(exceptionThrown);
             
            }
        }.run();
    }
    
    //JBSEAM-2109
    //@Test
    public void testReplyToErrors() throws Exception
    {
        
        new FacesRequest()
        {

            @Override
            protected void updateModelValues() throws Exception
            {
                
            }
            
            @Override
            protected void invokeApplication() throws Exception
            {   
                Person person = (Person) getValue("#{person}");
                
                person.setFirstname("Pete");
                person.setLastname("Muir");
                person.setAddress("test@example.com");
                
                boolean exceptionThrown = false;
                
                
                
                try
                {
                    getRenderedMailMessage("/org/jboss/seam/example/mail/test/errors3.xhtml");
                }
                catch (Exception e)
                {
                    Assert.assertTrue(e.getCause() instanceof AddressException);
                    AddressException ae = (AddressException) e.getCause();
                    System.out.println(ae.getMessage());
                    Assert.assertTrue(ae.getMessage().startsWith("Email cannot have more than one Reply-to address"));
                    exceptionThrown = true;
                }
                Assert.assertTrue(exceptionThrown);
             
            }
        }.run();
    }
    
    @Test
    public void testFromErrors() throws Exception
    {
        
        new FacesRequest()
        {

            @Override
            protected void updateModelValues() throws Exception
            {
                
            }
            
            @Override
            protected void invokeApplication() throws Exception
            {   
                Person person = (Person) getValue("#{person}");
                
                person.setFirstname("Pete");
                person.setLastname("Muir");
                person.setAddress("test@example.com");
                
                boolean exceptionThrown = false;
                
                try
                {
                    getRenderedMailMessage("/org/jboss/seam/example/mail/test/errors4.xhtml");
                }
                catch (FacesException e)
                {
                    Assert.assertTrue(e.getCause() instanceof AddressException);
                    AddressException ae = (AddressException) e.getCause();
                    Assert.assertTrue(ae.getMessage().startsWith("Email cannot have more than one from address"));
                    exceptionThrown = true;
                }
                Assert.assertTrue(exceptionThrown);
             
            }
        }.run();
    }
    
    @Test
    public void testSanitization() throws Exception
    {
        
        new FacesRequest()
        {

            @Override
            protected void updateModelValues() throws Exception
            {
                
            }
            
            @Override
            protected void invokeApplication() throws Exception
            { 
                Contexts.getEventContext().set("name", "Pete\nMuir");   
                MimeMessage renderedMessage = getRenderedMailMessage("/org/jboss/seam/example/mail/test/sanitization.xhtml");
                Assert.assertEquals(renderedMessage.getSubject(), "Try out Seam!");
                InternetAddress to = (InternetAddress) renderedMessage.getAllRecipients()[0];
                Assert.assertEquals(to.getAddress(), "peter@email.tld");
                Assert.assertEquals(to.getPersonal(), "Pete");
                Assert.assertEquals(renderedMessage.getFrom().length, 1);
                Assert.assertTrue(renderedMessage.getFrom()[0] instanceof InternetAddress);
                InternetAddress from = (InternetAddress) renderedMessage.getFrom()[0];
                Assert.assertEquals(from.getAddress(), "peter@example.com");
                Assert.assertEquals(from.getPersonal(), "Pete");
                Assert.assertNotNull(renderedMessage.getHeader("Pete"));
                Assert.assertEquals(renderedMessage.getHeader("Pete").length, 1);
                Assert.assertEquals(renderedMessage.getHeader("Pete")[0], "roll over");
            }
        }.run();
    }
    
    @Test
    public void testTemplating() throws Exception
    {
        
        new FacesRequest()
        {

            @Override
            protected void updateModelValues() throws Exception
            {
                setValue("#{person.firstname}", "Pete");
                setValue("#{person.lastname}", "Muir");
                setValue("#{person.address}", "test@example.com");
            }
            
            @Override
            protected void invokeApplication() throws Exception
            {   
                MimeMessage renderedMessage = getRenderedMailMessage("/templating.xhtml");
             
                // Test the standard headers
                
                InternetAddress to = (InternetAddress) renderedMessage.getAllRecipients()[0];
                Assert.assertEquals(to.getAddress(), "test@example.com");
                Assert.assertEquals(to.getPersonal(), "Pete Muir");
                InternetAddress from = (InternetAddress) renderedMessage.getFrom()[0];
                Assert.assertEquals(from.getAddress(), "do-not-reply@jboss.com");
                Assert.assertEquals(from.getPersonal(), "Seam");
                Assert.assertEquals(renderedMessage.getSubject(), "Templating with Seam Mail");
                Assert.assertNull(renderedMessage.getHeader("X-Priority"));
                Assert.assertNull(renderedMessage.getHeader("Priority"));
                Assert.assertNull(renderedMessage.getHeader("Importance"));
                
                // Check the body
                
                MimeMultipart body = (MimeMultipart) renderedMessage.getContent();
                
                // Check the alternative facet
                Assert.assertTrue(renderedMessage.getContentType().startsWith("multipart/mixed"));
                Assert.assertEquals(body.getCount(), 1);
                MimeBodyPart bodyPart = (MimeBodyPart) body.getBodyPart(0);
                Assert.assertTrue(bodyPart.getContentType().startsWith("multipart/alternative"));
                Assert.assertTrue(bodyPart.getContent() instanceof MimeMultipart);
                MimeMultipart bodyParts = (MimeMultipart) bodyPart.getContent();
                Assert.assertEquals(bodyParts.getCount(), 2);
                Assert.assertTrue(bodyParts.getBodyPart(0) instanceof MimeBodyPart);
                Assert.assertTrue(bodyParts.getBodyPart(1) instanceof MimeBodyPart);
                MimeBodyPart alternative = (MimeBodyPart) bodyParts.getBodyPart(0);
                MimeBodyPart html = (MimeBodyPart) bodyParts.getBodyPart(1);
                Assert.assertTrue(alternative.isMimeType("text/plain"));
                Assert.assertEquals(alternative.getDisposition(), "inline");
                Assert.assertTrue(html.isMimeType("text/html"));
                Assert.assertEquals(html.getDisposition(), "inline");
            }
        }.run();
    }
}
