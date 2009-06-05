package org.jboss.seam.mail.ui;

import javax.mail.Message.RecipientType;

/**
 * JSF component for rendering a Cc
 */
public class UICc extends RecipientAddressComponent
{

   @Override
   protected RecipientType getRecipientType()
   {
     return RecipientType.CC;
   }

}
