package org.jboss.seam.test.unit;

import org.jboss.seam.security.management.PasswordHash;
import org.testng.annotations.Test;

public class PasswordHashTest
{   
   @SuppressWarnings("deprecation")
   @Test
   public void testMd5Hash()
   {
      PasswordHash passwordHash = new PasswordHash();
      String hash = passwordHash.generateHash("secret", "MD5");
      assert hash.equals("Xr4ilOzQ4PCOq3aQ0qbuaQ==");
   }
   
   @SuppressWarnings("deprecation")
   @Test
   public void testShaHash()
   {
      PasswordHash passwordHash = new PasswordHash();
      String hash = passwordHash.generateHash("secret", "SHA");
      assert hash.equals("5en6G6MezRroT3XKqkdPOmY/BfQ=");
   }
}
