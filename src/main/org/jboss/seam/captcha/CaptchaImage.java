package org.jboss.seam.captcha;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.web.AbstractResource;

/**
 * Serves CAPTCHA images
 * 
 * @author Shane Bryzak
 */
@Scope(APPLICATION)
@Name("org.jboss.seam.captcha.captchaImage")
@BypassInterceptors
@Install(precedence = BUILT_IN)
public class CaptchaImage extends AbstractResource
{   
   public static CaptchaImage instance()
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("No application context active");
      }
      return (CaptchaImage) Contexts.getApplicationContext().get(CaptchaImage.class);
   }

   @Override
   public String getResourcePath()
   {
      return "/captcha";
   }
   
   @Override
   public void getResource(HttpServletRequest request, HttpServletResponse response)
       throws IOException
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      ServletLifecycle.beginRequest(request,getServletContext());         
      try
      {
         ImageIO.write( Captcha.instance().renderChallenge(), "jpeg", out );
      }
      finally
      {
         ServletLifecycle.endRequest(request);
      }

      response.setHeader("Cache-Control", "no-cache");
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "0");
      response.setContentType("image/jpeg");
      response.getOutputStream().write( out.toByteArray() );
      response.getOutputStream().flush();
      response.getOutputStream().close();
   }
   
}
