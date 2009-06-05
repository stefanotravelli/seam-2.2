package org.jboss.seam.pdf;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("org.jboss.seam.pdf.pdfKeyStore")
@Install(false)
@Scope(ScopeType.APPLICATION)
public class KeyStoreConfig
{
   String keyStore = null;
   String keyStorePassword = null;
   String keyPassword = null;
   String keyAlias = null;

   public String getKeyStore()
   {
      return keyStore;
   }

   public void setKeyStore(String keyStore)
   {
      this.keyStore = keyStore;
   }

   public String getKeyAlias()
   {
      return keyAlias;
   }

   public void setKeyAlias(String keyAlias)
   {
      this.keyAlias = keyAlias;
   }

   public String getKeyStorePassword()
   {
      return keyStorePassword;
   }

   public void setKeyStorePassword(String keyStorePassword)
   {
      this.keyStorePassword = keyStorePassword;
   }

   public String getKeyPassword()
   {
      return keyPassword;
   }

   public void setKeyPassword(String keyPassword)
   {
      this.keyPassword = keyPassword;
   }

   public static KeyStoreConfig instance()
   {
      return (KeyStoreConfig) Component.getInstance(KeyStoreConfig.class);
   }
}
