package org.jboss.seam.example.mail.test.selenium;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

public class SeleniumMailTest extends SeamSeleniumTest
{
   public static final String HOME_PAGE = "/index.seam";
   public static final String HOME_PAGE_TITLE = "Seam Mail";
   public static final String ENVELOPE_SENDER = "peter@example.com|do-not-reply@jboss.com";

   public static final String FIRSTNAME_INPUT = "id=emailform:firstname";
   public static final String FIRSTNAME = "John";
   public static final String LASTNAME_INPUT = "id=emailform:lastname";
   public static final String LASTNAME = "Smith";
   public static final String ADDRESS_INPUT = "id=emailform:address";
   public static final String ADDRESS = "john.smith@localhost";
   public static final String SERVLET_NAME_INPUT = "id=name";
   public static final String SERVLET_ADDRESS_INPUT = "id=email";

   public static final String SEND_SIMPLE_BUTTON = "id=emailform:sendSimple";
   public static final String SEND_PLAIN_BUTTON = "id=emailform:sendPlain";
   public static final String SEND_HTML_BUTTON = "id=emailform:sendHtml";
   public static final String SEND_ATTACHMENT_BUTTON = "id=emailform:sendAttachment";
   public static final String SEND_ASYNCHRONOUS_BUTTON = "id=emailform:sendAsynchronous";
   public static final String SEND_TEMPLATE_BUTTON = "id=emailform:sendTemplate";
   public static final String SEND_SERVLET_BUTTON = "id=sendServlet";
   
   protected Wiser wiser;
   
   @BeforeMethod
   @Override
   public void setUp()
   {
      super.setUp();
      browser.open(CONTEXT_PATH + HOME_PAGE);
   }

   /**
    * We restart SMTP after each Method, because Wiser doesn't have mechanism to flush recieved emails.
    */
   @BeforeMethod
   public void startSMTP() 
   {
      wiser = new Wiser();
      wiser.setPort(2525);
      wiser.start();
   }

   @AfterMethod
   public void stopSMTP()
   {
      wiser.stop();
   }
   
   /**
    * Place holder - just verifies that example deploys
    */
   @Test
   public void homePageLoadTest()
   {
      assertEquals("Unexpected page title.", HOME_PAGE_TITLE, browser.getTitle());
   }
   
   @DataProvider(name = "sendMethods")
   public Object[][] mailTest() {
      return new Object[][] {
            {SEND_SIMPLE_BUTTON, new String[] {"Content-Type: text/html; charset=ISO-8859-1", "Content-Disposition: inline", "<p>Dear " + FIRSTNAME + ",</p>"}},
            {SEND_PLAIN_BUTTON, new String[] {"This is a plain text, email."}},
            {SEND_HTML_BUTTON, new String[] {"Subject: Seam Mail", "Content-Type: multipart/mixed;", "Content-Type: multipart/alternative;", "Content-Type: text/plain; charset=ISO-8859-1", "This is the alternative text body for mail readers that don't support html", "Content-Type: text/html; charset=ISO-8859-1", "<p>This is an example <i>HTML</i> email sent by Seam.</p>"}},
            {SEND_ATTACHMENT_BUTTON, new String[] {"Content-Type: multipart/mixed;","Content-Type: application/octet-stream; name=jboss.jpg", "/9j/4AAQSkZJRgABA"/*jpeg start*/, "Content-Type: application/octet-stream; name=numbers.csv", "3,Three,treis,trois", "Content-Type: image/png; name=" + FIRSTNAME + "_" + LASTNAME + ".jpg", "iVBORw0KGgo" /*png start*/ }},
            {SEND_ASYNCHRONOUS_BUTTON, new String[] {"Content-Type: multipart/mixed;", "Content-Type: text/html; charset=ISO-8859-1", "Content-Disposition: inline", "<p>Dear " + FIRSTNAME + ",</p>"}},
            {SEND_TEMPLATE_BUTTON, new String[] {"Subject: Templating with Seam Mail", "Content-Type: multipart/mixed;", "Content-Type: multipart/alternative;", "Content-Type: text/plain; charset=ISO-8859-1", "Sorry, your mail reader doesn't support html.", "Content-Type: text/html; charset=ISO-8859-1", "<p>Here's a dataTable</p><table>", "<td>Saturday</td>"}},
            {SEND_SERVLET_BUTTON, new String[] {"Content-Type: multipart/mixed;", "Content-Disposition: inline", "Dear John Smith,", "This is a plain text, email."}}
      };
   }
   
   /**
    * Sends a mail and verifies it was delivered
    */
   @Test(dataProvider = "sendMethods")
   public void mailTest(String buttonToClick, String[] expectedMessageContents) {
      fillInInputs();
      sendEmail(buttonToClick);
      checkDelivered(expectedMessageContents);
   }

   /**
    * Fills in html text inputs.
    */
   private void fillInInputs()
   {
      browser.type(FIRSTNAME_INPUT, FIRSTNAME);
      browser.type(LASTNAME_INPUT, LASTNAME);
      browser.type(ADDRESS_INPUT, ADDRESS);
      browser.type(SERVLET_NAME_INPUT, FIRSTNAME + " " + LASTNAME);
      browser.type(SERVLET_ADDRESS_INPUT, ADDRESS);
   }
   
   /**
    * Sends an email by clicking on specified button. If the send method is asynchronous, 
    * waits for the associated action to take place. It assures that the email was sent
    * by verifying appropriate message.
    * @param buttonToClick
    */
   private void sendEmail(String buttonToClick)
   {
      browser.clickAndWait(buttonToClick);
      if (buttonToClick.equals(SEND_ASYNCHRONOUS_BUTTON)) {
         assertTrue(browser.isTextPresent("Seam Email")); // asynchronous email send produces no message, so we just check that we didn't end up on a debug page
         try
         {
            Thread.sleep(4000);
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
      } else {
         assertTrue("Expected message about successfuly sent mail. See also JBSEAM-3769.", browser.isTextPresent("Email sent successfully"));
      }
   }
   
   /**
    * Checks that the expected email was delivered.
    * @param expectedMessageContents
    */
   private void checkDelivered(String[] expectedMessageContents)
   {
      assertFalse("Expected a message", wiser.getMessages().isEmpty());
      WiserMessage message = wiser.getMessages().get(0); // although "send plain text" example sends 3 mails (To:, CC:, Bcc:) Wiser cannot distinquish between them so we just check the first mail.
      assertEquals(ADDRESS, message.getEnvelopeReceiver());
      assertTrue("Envelope sender (" + message.getEnvelopeSender() + ") doesn't match expected one (" + ENVELOPE_SENDER + ")", message.getEnvelopeSender().matches(ENVELOPE_SENDER));
      
      for (String expectedMessageContent: expectedMessageContents) {
         assertTrue("Didn't find expected text (" + expectedMessageContent + ") in the received email.", new String(message.getData()).contains(expectedMessageContent));
      }
   }

}
