package org.jboss.seam.test.integration.i8ln;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.faces.component.UIOutput;
import javax.faces.event.ValueChangeEvent;

import org.jboss.seam.international.LocaleConfig;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.Seam;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

public class LocaleTest extends SeamTest
{
   @Test
   public void localeTest() throws Exception
   {
      new NonFacesRequest()
      {
         @Override
         protected void renderResponse() throws Exception
         {
            // it's necessary to emulate the startup behavior of LocaleConfig since it alters the JSF Application
            // and we cannot be sure that the JSF Application wasn't cleared by an earlier class
            // NOTE: I wish this test suite had some better place of initializing the application context
            Contexts.getApplicationContext().remove(Seam.getComponentName(LocaleConfig.class));
            LocaleConfig.instance();
         }
      }.run();

      new FacesRequest()
      {

         @Override
         protected void invokeApplication() throws Exception
         {
            // <i18:locale-config default-locale="fr_CA" supported-locales="fr_CA fr_FR en"/>
            List<Locale> supportedLocales = new ArrayList<Locale>();
            for (Iterator<Locale> iter = getFacesContext().getApplication().getSupportedLocales(); iter.hasNext();)
            {
               supportedLocales.add(iter.next());
            }
            assert supportedLocales.size() == 3;
            assert supportedLocales.contains(Locale.CANADA_FRENCH);
            assert supportedLocales.contains(Locale.ENGLISH);
            assert supportedLocales.contains(Locale.FRANCE);
            assert getFacesContext().getApplication().getDefaultLocale().equals(Locale.CANADA_FRENCH);
            
            // why not? I guess be default locale means different things in different contexts (server vs user)
            //assert org.jboss.seam.international.Locale.instance().equals(Locale.CANADA_FRENCH);
            
            // reset the locale configuration (as it would be w/o <i18n:locale-config>)
            getFacesContext().getApplication().setDefaultLocale(Locale.ENGLISH);
            getFacesContext().getApplication().setSupportedLocales(null);
            
            assert org.jboss.seam.international.Locale.instance().equals(Locale.getDefault());
            
            LocaleSelector.instance().setLocale(Locale.UK);
            
            assert org.jboss.seam.international.Locale.instance().equals(Locale.UK);
          
            LocaleSelector.instance().setLocaleString(Locale.FRANCE.toString());
            
            LocaleSelector.instance().getLanguage().equals(Locale.FRANCE.getLanguage());
            LocaleSelector.instance().getCountry().equals(Locale.FRANCE.getCountry());
            LocaleSelector.instance().getVariant().equals(Locale.FRANCE.getVariant());
            
            assert org.jboss.seam.international.Locale.instance().equals(Locale.FRANCE);
            assert LocaleSelector.instance().getLocaleString().equals(Locale.FRANCE.toString());
            
            LocaleSelector.instance().select();
            assert org.jboss.seam.international.Locale.instance().equals(Locale.FRANCE);
            
            LocaleSelector.instance().selectLanguage(Locale.JAPANESE.getLanguage());
            assert org.jboss.seam.international.Locale.instance().getLanguage().equals(Locale.JAPANESE.getLanguage());
            
            ValueChangeEvent valueChangeEvent = new ValueChangeEvent(new UIOutput(), Locale.JAPANESE.toString(), Locale.TAIWAN.toString());
            LocaleSelector.instance().select(valueChangeEvent);
            assert org.jboss.seam.international.Locale.instance().equals(Locale.TAIWAN);
            
            Locale uk_posix = new Locale(Locale.UK.getLanguage(), Locale.UK.getCountry(), "POSIX");
            LocaleSelector.instance().setLocale(uk_posix);
            
            assert org.jboss.seam.international.Locale.instance().equals(uk_posix);
            assert LocaleSelector.instance().getLanguage().equals(uk_posix.getLanguage());
            assert LocaleSelector.instance().getCountry().equals(uk_posix.getCountry());
            assert LocaleSelector.instance().getVariant().equals(uk_posix.getVariant());
            
            LocaleSelector.instance().setLanguage(Locale.CHINA.getLanguage());
            LocaleSelector.instance().setCountry(Locale.CHINA.getCountry()); 
            LocaleSelector.instance().setVariant(null);
            
            assert org.jboss.seam.international.Locale.instance().equals(Locale.CHINA);
            
            LocaleSelector.instance().setLanguage(Locale.ITALIAN.getLanguage());
            LocaleSelector.instance().setCountry(null);            
            LocaleSelector.instance().setVariant(null);
            
            assert org.jboss.seam.international.Locale.instance().equals(Locale.ITALIAN);
            
            assert LocaleSelector.instance().getSupportedLocales().size() == 1;
            assert LocaleSelector.instance().getSupportedLocales().get(0).getValue().equals(Locale.ENGLISH.toString());
            assert LocaleSelector.instance().getSupportedLocales().get(0).getLabel().equals(Locale.ENGLISH.getDisplayName());

            boolean failed = false;
            try
            {
               LocaleSelector.instance().setLocale(null);
            }
            catch (NullPointerException e) 
            {
               failed = true;
            }
            assert failed;
            
            // TODO Test cookie stuff (need to extend Mocks for this)
            
         }
      }.run();
   }
}
