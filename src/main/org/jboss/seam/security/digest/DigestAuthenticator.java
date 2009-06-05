package org.jboss.seam.security.digest;

import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.Identity;

/**
 * This class provides methods for performing Digest (RFC 2617) authentication
 * and is intended to be extended by a concrete Authenticator implementation. 
 *  
 * @author Shane Bryzak
 */
public abstract class DigestAuthenticator
{
   @SuppressWarnings("deprecation")
   protected boolean validatePassword(String password)
   {
      Context ctx = Contexts.getSessionContext();
      
      DigestRequest digestRequest = (DigestRequest) ctx.get(DigestRequest.DIGEST_REQUEST);
      if (digestRequest == null)
      {
         throw new IllegalStateException("No digest request found in session scope");
      }
      
      // Remove the digest request from the session now
      ctx.remove(DigestRequest.DIGEST_REQUEST);
      
      // Calculate the expected digest
      String serverDigestMd5 = DigestUtils.generateDigest(
               digestRequest.isPasswordAlreadyEncoded(), 
               Identity.instance().getUsername(), digestRequest.getRealm(), 
               password, digestRequest.getHttpMethod(), 
               digestRequest.getUri(), digestRequest.getQop(), 
               digestRequest.getNonce(), digestRequest.getNonceCount(), 
               digestRequest.getClientNonce());

      return serverDigestMd5.equals(digestRequest.getClientDigest());
   }  
}
