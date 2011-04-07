package org.jboss.seam.security.digest;

import org.jboss.seam.util.Base64;

public class DigestRequest
{
   public static final String DIGEST_REQUEST = "org.jboss.seam.security.digestRequest";   
   
   private boolean passwordAlreadyEncoded;
   private String systemRealm;
   private String realm;
   private String key;
   private String password;
   private String uri;
   
   /**
    * quality of protection, defined by RFC 2617
    */
   private String qop;
   private String nonce;
   private String nonceCount;
   private String clientNonce;
   private String httpMethod;
   
   /**
    * The digest that the client responds with
    */
   private String clientDigest;
   
   public String getClientNonce()
   {
      return clientNonce;
   }
   
   public void setClientNonce(String clientNonce)
   {
      this.clientNonce = clientNonce;
   }
   
   public String getNonce()
   {
      return nonce;
   }
   
   public void setNonce(String nonce)
   {
      this.nonce = nonce;
   }
   
   public String getNonceCount()
   {
      return nonceCount;
   }
   
   public void setNonceCount(String nonceCount)
   {
      this.nonceCount = nonceCount;
   }
   
   public String getPassword()
   {
      return password;
   }
   
   public void setPassword(String password)
   {
      this.password = password;
   }
   
   public boolean isPasswordAlreadyEncoded()
   {
      return passwordAlreadyEncoded;
   }
   
   public void setPasswordAlreadyEncoded(boolean passwordAlreadyEncoded)
   {
      this.passwordAlreadyEncoded = passwordAlreadyEncoded;
   }
   
   public String getQop()
   {
      return qop;
   }
   
   public void setQop(String qop)
   {
      this.qop = qop;
   }
   
   public String getRealm()
   {
      return realm;
   }
   
   public String getSystemRealm()
   {
      return systemRealm;
   }
   
   public void setSystemRealm(String systemRealm)
   {
      this.systemRealm = systemRealm;
   }
   
   public void setRealm(String realm)
   {
      this.realm = realm;
   }
   
   public String getKey()
   {
      return key;
   }
   
   public void setKey(String key)
   {
      this.key = key;
   }
   
   public String getUri()
   {
      return uri;
   }
   
   public void setUri(String uri)
   {
      this.uri = uri;
   } 
   
   public String getHttpMethod()
   {
      return httpMethod;
   }
   
   public void setHttpMethod(String httpMethod)
   {
      this.httpMethod = httpMethod;
   }
   
   public String getClientDigest()
   {
      return clientDigest;
   }
   
   public void setClientDigest(String clientDigest)
   {
      this.clientDigest = clientDigest;
   }
   
   public void validate()
      throws DigestValidationException
   {
      // Check all required parameters were supplied (ie RFC 2069)
      if (realm == null) throw new DigestValidationException("Mandatory field 'realm' not specified");
      if (nonce == null) throw new DigestValidationException("Mandatory field 'nonce' not specified");
      if (uri == null) throw new DigestValidationException("Mandatory field 'uri' not specified");
      if (clientDigest == null) throw new DigestValidationException("Mandatory field 'response' not specified");
      
      // Check all required parameters for an "auth" qop were supplied (ie RFC 2617)
      if ("auth".equals(qop)) 
      {
         if (nonceCount == null) 
         {
            throw new DigestValidationException("Mandatory field 'nc' not specified");
         }
         
         if (clientNonce == null) 
         {
            throw new DigestValidationException("Mandatory field 'cnonce' not specified");
         }
      }         
      

      String nonceAsText = new String(Base64.decode(nonce));               
      
      String[] nonceTokens = nonceAsText.split(":");
      if (nonceTokens.length != 2) 
      {
         throw new DigestValidationException("Nonce should provide two tokens - nonce received: " + nonce);
      }
      
      // Check realm name equals what we expected
      if (!systemRealm.equals(realm)) 
      {
         throw new DigestValidationException("Realm name [" + realm + 
                  "] does not match system realm name [" + systemRealm + "]");
      }            
      
      long nonceExpiry = 0;      
      try 
      {
         nonceExpiry = new Long(nonceTokens[0]).longValue();
      } 
      catch (NumberFormatException nfe) 
      {
         throw new DigestValidationException("First nonce token should be numeric, but was: " + nonceTokens[0]);
      }
      

      // To get this far, the digest must have been valid
      // Check the nonce has not expired
      // We do this last so we can direct the user agent its nonce is stale
      // but the request was otherwise appearing to be valid
      if (nonceExpiry < System.currentTimeMillis()) 
      {
         throw new DigestValidationException("Nonce has expired", true);
      }        
      
      String expectedNonceSignature = DigestUtils.md5Hex(nonceExpiry + ":" + key);
      
      if (!expectedNonceSignature.equals(nonceTokens[1])) 
      {
         throw new DigestValidationException("Nonce token invalid: " + nonceAsText);
      }       
   }
}
