package org.jboss.seam.captcha;

import java.lang.annotation.Annotation;

import org.hibernate.validator.Validator;

/**
 * Validates that the input entered by the user matches
 * the captcha image.
 * 
 * @author Gavin King
 *
 */
public class CaptchaResponseValidator implements Validator
{

   public void initialize(Annotation captchaResponse) {}

   public boolean isValid(Object response)
   {
      return Captcha.instance().validateResponse( (String) response );
   }

}
