package org.jboss.seam.security.digest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jboss.seam.util.Hex;

/**
 * Digest-related utility methods, adapted from Acegi and Apache Commons.
 *  
 * @author Shane Bryzak
 */
public class DigestUtils
{   
   public static String generateDigest(boolean passwordAlreadyEncoded, String username,
            String realm, String password, String httpMethod, String uri, String qop, String nonce,
            String nc, String cnonce) throws IllegalArgumentException
   {
      String a1Md5 = null;
      String a2 = httpMethod + ":" + uri;
      String a2Md5 = new String(DigestUtils.md5Hex(a2));

      if (passwordAlreadyEncoded)
      {
         a1Md5 = password;
      }
      else
      {
         a1Md5 = encodePasswordInA1Format(username, realm, password);
      }

      String digest;

      if (qop == null)
      {
         // as per RFC 2069 compliant clients (also reaffirmed by RFC 2617)
         digest = a1Md5 + ":" + nonce + ":" + a2Md5;
      }
      else if ("auth".equals(qop))
      {
         // As per RFC 2617 compliant clients
         digest = a1Md5 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + a2Md5;
      }
      else
      {
         throw new IllegalArgumentException("This method does not support a qop: '" + qop + "'");
      }

      String digestMd5 = new String(DigestUtils.md5Hex(digest));

      return digestMd5;
   }

   public static String encodePasswordInA1Format(String username, String realm, String password)
   {
      String a1 = username + ":" + realm + ":" + password;
      String a1Md5 = new String(DigestUtils.md5Hex(a1));

      return a1Md5;
   } 
   
   public static String md5Hex(String value)
   {
      try
      {
         MessageDigest md = MessageDigest.getInstance("MD5");
         return new String(Hex.encodeHex(md.digest(value.getBytes())));
      }
      catch (NoSuchAlgorithmException ex)
      {
         throw new RuntimeException("Invalid algorithm");
      }
   }
}
