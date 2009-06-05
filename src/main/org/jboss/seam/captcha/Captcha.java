package org.jboss.seam.captcha;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Random;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

/**
 * Default CAPTCHA algorithm, a simple addition problem. May be
 * extended and customized.
 * 
 * @author Gavin King
 *
 */
@Name("org.jboss.seam.captcha.captcha")
@Scope(ScopeType.SESSION)
@Install(precedence=Install.BUILT_IN)
@BypassInterceptors
public class Captcha implements Serializable
{
   private static Random random = new Random( System.currentTimeMillis() );
   
   private String correctResponse;
   private String challenge;
   private String response;
   
   /**
    * Initialize the challenge and correct response.
    * May be overridden and customized by a subclass.
    */
   @Create
   public void init()
   {
       int x = random.nextInt(50);
       int y = random.nextInt(50);
       setCorrectResponse( Integer.toString( x + y ) );
       setChallenge( Integer.toString(x) + " + " + Integer.toString(y) + " =" );
   }
   
   /**
    * Set the challenge question
    */
   protected void setChallenge(String challenge) 
   {
       this.challenge = challenge;
   }
   
   /**
    * Get the challenge question
    */
   protected String getChallenge()
   {
       return challenge;
   }
   
   /**
    * Set the correct response
    */
   protected void setCorrectResponse(String correctResponse) 
   {
       this.correctResponse = correctResponse;
   }
   
   /**
    * Validate that the entered response is the correct
    * response
    */
   public boolean validateResponse(String response)
   {
      boolean valid = response!=null && 
                      correctResponse!=null && 
                      response.trim().equals(correctResponse);
      if (!valid) 
      {
         init();
      }
      return valid;
   }
   
   @CaptchaResponse
   public String getResponse()
   {
      return response;
   }

   public void setResponse(String input)
   {
      this.response = input;
   }
   
   /**
    * Render the challenge question as an image.
    * May be overridden by subclasses to achieve
    * a stronger CAPTCHA.
    */
   public BufferedImage renderChallenge() 
   {
      BufferedImage challenge = new BufferedImage(70, 20, BufferedImage.TYPE_BYTE_GRAY);
      Graphics graphics = challenge.getGraphics();
      graphics.setColor( getChallengeBackgroundColor() );
      graphics.fillRect(0, 0, getChallengeImageWidth(), 20);
      graphics.setColor( getChallengeTextColor() );
      graphics.drawString( getChallenge(), 5, 15 );
      return challenge;
   }

   /**
    * May be overridden by subclasses
    * @return the width, in pixels, of the challenge question
    */
   protected int getChallengeImageWidth() {
      return 70;
   }
   
   /**
    * May be overridden by subclasses
    * @return the background color of the challenge image
    */
   protected Color getChallengeBackgroundColor()
   {
      return Color.WHITE;
   }

   /**
    * May be overridden by subclasses
    * @return @return the foreground color of the challenge image
    */
   protected Color getChallengeTextColor() 
   {
      return Color.BLACK;
   }
   
   public static Captcha instance()
   {
      if ( !Contexts.isSessionContextActive() )
      {
         throw new IllegalStateException("No session context active");
      }
      return (Captcha) Component.getInstance(Captcha.class, ScopeType.SESSION);
   }

}
