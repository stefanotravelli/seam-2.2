package org.jboss.seam.test.unit;

import java.util.ArrayList;

import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.naming.NamingException;

import org.jboss.seam.mail.MailSession;
import org.jboss.seam.mail.MeldwareUser;
import org.testng.annotations.Test;

import com.sun.mail.smtp.SMTPSSLTransport;
import com.sun.mail.smtp.SMTPTransport;

public class MailTest
{

   private static final String HOST = "smtp.jboss.org";
   
   private static final String DEFAULT_HOST = "localhost";

   private static final int PORT = 666;
   
   private static final int DEFAULT_PORT = 25;
   
   private static final int DEFAULT_SSL_PORT = 465;

   private static final String USERNAME = "pmuir";

   private static final String PASSWORD = "letmein";
   
   private static final String EMAIL = "pete.muir@jboss.org";

   @Test
   public void testBasicMailSession()
   {

      MailSession mailSession = new MailSession();

      mailSession.create();

      Session session = null;

      try
      {
         session = mailSession.getSession();
      }
      catch (NamingException e)
      {
         assert false;
         // Naming exception can't occur if we aren't getting the Session from
         // JNDI
      }

      assert DEFAULT_HOST.equals(session.getProperty("mail.smtp.host"));

      int port = 0;

      try
      {
         port = Integer.parseInt(session.getProperty("mail.smtp.port"));
      }
      catch (NumberFormatException e)
      {
         assert false;
      }

      assert port == DEFAULT_PORT;

      assert "smtp".equals(session.getProperty("mail.transport.protocol"));
      
      SMTPTransport transport = null;

      try
      {
         assert session.getTransport() instanceof SMTPTransport;
         transport = (SMTPTransport) session.getTransport();
      }
      catch (NoSuchProviderException e)
      {
         assert false;
      }

      assert !session.getDebug();

      assert transport.getStartTLS();

   }

   @Test
   public void testMailSession()
   {

      MailSession mailSession = new MailSession();
      mailSession.setHost(HOST);
      mailSession.setPort(PORT);
      mailSession.setDebug(true);

      mailSession.create();

      Session session = null;

      try
      {
         session = mailSession.getSession();
      }
      catch (NamingException e)
      {
         assert false;
         // Naming exception can't occur if we aren't getting the Session from
         // JNDI
      }

      assert HOST.equals(session.getProperty("mail.smtp.host"));

      int port = 0;

      try
      {
         port = Integer.parseInt(session.getProperty("mail.smtp.port"));
      }
      catch (NumberFormatException e)
      {
         assert false;
      }

      assert port == PORT;

      try
      {
         assert session.getTransport() instanceof SMTPTransport;
      }
      catch (NoSuchProviderException e)
      {
         assert false;
      }

      assert session.getDebug();

   }

   @Test
   public void testAuthenticatedMailSession()
   {
      MailSession mailSession = new MailSession();
      mailSession.setUsername(USERNAME);
      mailSession.setPassword(PASSWORD);

      mailSession.create();

      Session session = null;

      try
      {
         session = mailSession.getSession();
      }
      catch (NamingException e)
      {
         assert false;
         // Naming exception can't occur if we aren't getting the Session from
         // JNDI
      }

      assert Boolean.parseBoolean(session.getProperty("mail.smtp.auth"));

      // TODO Check authentication

   }
   
   @Test
   public void testMissingPasswordMailSession()
   {
      MailSession mailSession = new MailSession();
      mailSession.setUsername(USERNAME);

      mailSession.create();

      Session session = null;

      try
      {
         session = mailSession.getSession();
      }
      catch (NamingException e)
      {
         assert false;
         // Naming exception can't occur if we aren't getting the Session from
         // JNDI
      }

      assert null == session.getProperty("mail.smtp.auth");

   }
   
   @Test
   public void testMissingUsernameMailSession()
   {
      MailSession mailSession = new MailSession();
      mailSession.setPassword(PASSWORD);

      mailSession.create();

      Session session = null;

      try
      {
         session = mailSession.getSession();
      }
      catch (NamingException e)
      {
         assert false;
         // Naming exception can't occur if we aren't getting the Session from
         // JNDI
      }

      assert null == session.getProperty("mail.smtp.auth");

   }

   @Test
   public void testBasicSslMailSession()
   {
      MailSession mailSession = new MailSession();
      
      mailSession.setSsl(true);

      mailSession.create();

      Session session = null;

      try
      {
         session = mailSession.getSession();
      }
      catch (NamingException e)
      {
         assert false;
         // Naming exception can't occur if we aren't getting the Session from
         // JNDI
      }

      assert "smtps".equals(session.getProperty("mail.transport.protocol"));
      
      SMTPSSLTransport transport = null;

      try
      {
         assert session.getTransport() instanceof SMTPSSLTransport;
         transport = (SMTPSSLTransport) session.getTransport();
      }
      catch (NoSuchProviderException e)
      {
         assert false;
      }
     
      int port = 0;
      
      try
      {
         port = Integer.parseInt(session.getProperty("mail.smtps.port"));
      }
      catch (NumberFormatException e)
      {
         assert false;
      }

      assert port == DEFAULT_SSL_PORT;
      
      assert DEFAULT_HOST.equals(session.getProperty("mail.smtps.host"));
      
      assert !session.getDebug();
      
      // TLS not used over SSL
      assert !transport.getStartTLS();

   }
   
   @Test
   public void testSslMailSession()
   {
      MailSession mailSession = new MailSession();
      mailSession.setHost(HOST);
      mailSession.setSsl(true);
      mailSession.setPort(PORT);

      mailSession.create();

      Session session = null;

      try
      {
         session = mailSession.getSession();
      }
      catch (NamingException e)
      {
         assert false;
         // Naming exception can't occur if we aren't getting the Session from
         // JNDI
      }
     
      int port = 0;
      
      try
      {
         port = Integer.parseInt(session.getProperty("mail.smtps.port"));
      }
      catch (NumberFormatException e)
      {
         assert false;
      }

      assert port == PORT;

   }
   
   @Test
   public void testAuthenticatedSslMailSession()
   {
      MailSession mailSession = new MailSession();
      mailSession.setUsername(USERNAME);
      mailSession.setPassword(PASSWORD);
      mailSession.setSsl(true);

      mailSession.create();

      Session session = null;

      try
      {
         session = mailSession.getSession();
      }
      catch (NamingException e)
      {
         assert false;
         // Naming exception can't occur if we aren't getting the Session from
         // JNDI
      }

      assert Boolean.parseBoolean(session.getProperty("mail.smtps.auth"));
      assert session.getProperty("mail.smtp.auth") == null;

      // TODO Check authentication

   }
   
   @Test
   public void testJndiMailSession()
   {
      MailSession mailSession = new MailSession();
      mailSession.setSessionJndiName("java:/Mail");
      
      mailSession.create();
      
      boolean failure = false;
      
      // We can't get a Session from JNDI without a full container.
      try
      {
         mailSession.getSession();
      }
      catch (Exception e)
      {
        failure = true;
      }
      
      assert failure;
   }
   
   @Test
   public void testMeldwareUser()
   {
      MeldwareUser meldwareUser = new MeldwareUser();
      meldwareUser.setUsername(USERNAME);
      meldwareUser.setPassword(PASSWORD);
      meldwareUser.getAliases().add(EMAIL);
      
      assert USERNAME.equals(meldwareUser.getUsername());
      assert PASSWORD.equals(meldwareUser.getPassword());
      assert meldwareUser.getAliases() != null;
      assert meldwareUser.getAliases().contains(EMAIL);
      assert meldwareUser.getRoles().contains("calendaruser");
      assert !meldwareUser.getRoles().contains("adminuser");
      
      meldwareUser.setAliases(new ArrayList<String>());
      assert meldwareUser.getAliases().isEmpty();
   }
   
   @Test
   public void testAdminMeldwareUser()
   {
      MeldwareUser meldwareUser = new MeldwareUser();
      meldwareUser.setAdministrator(true);
      
      assert meldwareUser.getRoles().contains("calendaruser");
      assert meldwareUser.getRoles().contains("adminuser");
   }
   
   // TODO Write tests for Meldware

}
