package org.jboss.seam.excel.ui;

import java.io.IOException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Manager;
import org.jboss.seam.document.ByteArrayDocumentData;
import org.jboss.seam.document.DocumentData;
import org.jboss.seam.document.DocumentStore;
import org.jboss.seam.document.DocumentData.DocumentType;
import org.jboss.seam.excel.ExcelFactory;
import org.jboss.seam.excel.ExcelWorkbook;
import org.jboss.seam.navigation.Pages;

public class UIWorkbook extends ExcelComponent
{
   public enum CreationType
   {
      WITHOUT_SETTINGS_OR_TEMPLATE, WITHOUT_SETTINGS_WITH_TEMPLATE, WITH_SETTINGS_WITHOUT_TEMPLATE, WITH_SETTNGS_AND_TEMPLATE
   }

   public static final String COMPONENT_TYPE = "org.jboss.seam.excel.ui.UIWorkbook";

   private boolean sendRedirect = true;
   private ExcelWorkbook excelWorkbook = null;
   private String type = "";
   private String templateURI;
   private Integer arrayGrowSize;
   private Boolean autoFilterDisabled;
   private Boolean cellValidationDisabled;
   private Integer characterSet;
   private Boolean drawingsDisabled;
   private String encoding;
   private String excelDisplayLanguage;
   private String excelRegionalSettings;
   private Boolean formulaAdjust;
   private Boolean gcDisabled;
   private Boolean ignoreBlanks;
   private Integer initialFileSize;
   private String locale;
   private Boolean mergedCellCheckingDisabled;
   private Boolean namesDisabled;
   private Boolean propertySets;
   private Boolean rationalization;
   private Boolean supressWarnings;
   private String temporaryFileDuringWriteDirectory;
   private Boolean useTemporaryFileDuringWrite;
   private Boolean workbookProtected;
   private String exportKey;
   private String filename;

   public String getFilename()
   {
      return (String) valueOf("filename", filename);
   }

   public void setFilename(String filename)
   {
      this.filename = filename;
   }

   public String getExportKey()
   {
      return (String) valueOf("exportKey", exportKey);
   }

   public void setExportKey(String exportKey)
   {
      this.exportKey = exportKey;
   }

   public CreationType getCreationType()
   {
      if (hasSettings())
      {
         if (getTemplateURI() != null)
         {
            return CreationType.WITH_SETTNGS_AND_TEMPLATE;
         }
         else
         {
            return CreationType.WITH_SETTINGS_WITHOUT_TEMPLATE;
         }
      }
      else
      {
         if (getTemplateURI() != null)
         {
            return CreationType.WITHOUT_SETTINGS_WITH_TEMPLATE;
         }
         else
         {
            return CreationType.WITHOUT_SETTINGS_OR_TEMPLATE;
         }
      }
   }

   public Integer getArrayGrowSize()
   {
      return (Integer) valueOf("arrayGrowSize", arrayGrowSize);

   }

   public void setArrayGrowSize(Integer arrayGrowSize)
   {
      this.arrayGrowSize = arrayGrowSize;
   }

   public Boolean getAutoFilterDisabled()
   {
      return (Boolean) valueOf("autoFilterDisabled", autoFilterDisabled);
   }

   public void setAutoFilterDisabled(Boolean autoFilterDisabled)
   {
      this.autoFilterDisabled = autoFilterDisabled;
   }

   public Boolean getCellValidationDisabled()
   {
      return (Boolean) valueOf("cellValidationDisabled", cellValidationDisabled);
   }

   public void setCellValidationDisabled(Boolean cellValidationDisabled)
   {
      this.cellValidationDisabled = cellValidationDisabled;
   }

   public Integer getCharacterSet()
   {
      return (Integer) valueOf("characterSet", characterSet);
   }

   public void setCharacterSet(Integer characterSet)
   {
      this.characterSet = characterSet;
   }

   public Boolean getDrawingsDisabled()
   {
      return (Boolean) valueOf("drawingsDisabled", drawingsDisabled);
   }

   public void setDrawingsDisabled(Boolean drawingsDisabled)
   {
      this.drawingsDisabled = drawingsDisabled;
   }

   public String getEncoding()
   {
      return (String) valueOf("encoding", encoding);
   }

   public void setEncoding(String encoding)
   {
      this.encoding = encoding;
   }

   public String getExcelDisplayLanguage()
   {
      return (String) valueOf("excelDisplayLanguage", excelDisplayLanguage);
   }

   public void setExcelDisplayLanguage(String excelDisplayLanguage)
   {
      this.excelDisplayLanguage = excelDisplayLanguage;
   }

   public String getExcelRegionalSettings()
   {
      return (String) valueOf("excelRegionalSettings", excelRegionalSettings);
   }

   public void setExcelRegionalSettings(String excelRegionalSettings)
   {
      this.excelRegionalSettings = excelRegionalSettings;
   }

   public Boolean getFormulaAdjust()
   {
      return (Boolean) valueOf("formulaAdjust", formulaAdjust);
   }

   public void setFormulaAdjust(Boolean formulaAdjust)
   {
      this.formulaAdjust = formulaAdjust;
   }

   public Boolean getGcDisabled()
   {
      return (Boolean) valueOf("gcDisabled", gcDisabled);
   }

   public void setGcDisabled(Boolean gcDisabled)
   {
      this.gcDisabled = gcDisabled;
   }

   public Boolean getIgnoreBlanks()
   {
      return (Boolean) valueOf("ignoreBlanks", ignoreBlanks);
   }

   public void setIgnoreBlanks(Boolean ignoreBlanks)
   {
      this.ignoreBlanks = ignoreBlanks;
   }

   public Integer getInitialFileSize()
   {
      return (Integer) valueOf("initialFileSize", initialFileSize);
   }

   public void setInitialFileSize(Integer initialFileSize)
   {
      this.initialFileSize = initialFileSize;
   }

   public String getLocale()
   {
      return (String) valueOf("locale", locale);
   }

   public void setLocale(String locale)
   {
      this.locale = locale;
   }

   public Boolean getMergedCellCheckingDisabled()
   {
      return (Boolean) valueOf("mergedCellCheckingDisabled", mergedCellCheckingDisabled);
   }

   public void setMergedCellCheckingDisabled(Boolean mergedCellCheckingDisabled)
   {
      this.mergedCellCheckingDisabled = mergedCellCheckingDisabled;
   }

   public Boolean getNamesDisabled()
   {
      return (Boolean) valueOf("namesDisabled", namesDisabled);
   }

   public void setNamesDisabled(Boolean namesDisabled)
   {
      this.namesDisabled = namesDisabled;
   }

   public Boolean getPropertySets()
   {
      return (Boolean) valueOf("propertySets", propertySets);
   }

   public void setPropertySets(Boolean propertySets)
   {
      this.propertySets = propertySets;
   }

   public Boolean getRationalization()
   {
      return (Boolean) valueOf("rationalization", rationalization);

   }

   public void setRationalization(Boolean rationalization)
   {
      this.rationalization = rationalization;
   }

   public Boolean getSupressWarnings()
   {
      return (Boolean) valueOf("supressWarnings", supressWarnings);
   }

   public void setSupressWarnings(Boolean supressWarnings)
   {
      this.supressWarnings = supressWarnings;
   }

   public String getTemporaryFileDuringWriteDirectory()
   {
      return (String) valueOf("temporaryFileDuringWriteDirectory", temporaryFileDuringWriteDirectory);
   }

   public void setTemporaryFileDuringWriteDirectory(String temporaryFileDuringWriteDirectory)
   {
      this.temporaryFileDuringWriteDirectory = temporaryFileDuringWriteDirectory;
   }

   public Boolean getUseTemporaryFileDuringWrite()
   {
      return (Boolean) valueOf("useTemporaryFileDuringWrite", useTemporaryFileDuringWrite);
   }

   public void setUseTemporaryFileDuringWrite(Boolean useTemporaryFileDuringWrite)
   {
      this.useTemporaryFileDuringWrite = useTemporaryFileDuringWrite;
   }

   @SuppressWarnings("unchecked")
   @Override
   public void encodeBegin(javax.faces.context.FacesContext facesContext) throws IOException
   {
      // Get workbook implementation
      excelWorkbook = ExcelFactory.instance().getExcelWorkbook(getType());

      // Create a new workbook
      excelWorkbook.createWorkbook(this);

      List<UILink> stylesheets = getChildrenOfType(getChildren(), UILink.class);
      excelWorkbook.setStylesheets(stylesheets);
   }

   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {

      // Get the bytes from workbook that should be passed on to the user
      byte[] bytes = excelWorkbook.getBytes();

      DocumentType type = excelWorkbook.getDocumentType();

      String viewId = Pages.getViewId(context);
      String baseName = Pages.getCurrentBaseName();

      DocumentData documentData = new ByteArrayDocumentData(baseName, type, bytes);
      documentData.setFilename(getFilename());

      if (getExportKey() != null)
      {
         Contexts.getEventContext().set(getExportKey(), documentData);
         return;
      }

      if (sendRedirect)
      {
         DocumentStore store = DocumentStore.instance();
         String id = store.newId();

         String url = store.preferredUrlForContent(baseName, type.getExtension(), id);
         url = Manager.instance().encodeConversationId(url, viewId);

         store.saveData(id, documentData);

         context.getExternalContext().redirect(url);

      }
      else
      {
         UIComponent parent = getParent();

         if (parent instanceof ValueHolder)
         {
            ValueHolder holder = (ValueHolder) parent;
            holder.setValue(documentData);
         }
      }
   }

   public boolean isSendRedirect()
   {
      return (Boolean) valueOf("sendRedirect", sendRedirect);
   }

   public void setSendRedirect(boolean sendRedirect)
   {
      this.sendRedirect = sendRedirect;
   }

   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }

   public String getType()
   {
      return (String) valueOf("type", type);
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public ExcelWorkbook getExcelWorkbook()
   {
      return excelWorkbook;
   }

   public void setExcelWorkbook(ExcelWorkbook excelWorkbook)
   {
      this.excelWorkbook = excelWorkbook;
   }

   public String getTemplateURI()
   {
      return (String) valueOf("templateURI", templateURI);
   }

   public void setTemplateURI(String templateURI)
   {
      this.templateURI = templateURI;
   }

   /**
    * Hack? Noooooooooooooooo
    */
   public boolean hasSettings()
   {
      return getArrayGrowSize() != null || getAutoFilterDisabled() != null || getCellValidationDisabled() != null || getCharacterSet() != null || getDrawingsDisabled() != null || getEncoding() != null || getExcelDisplayLanguage() != null || getExcelRegionalSettings() != null || getFormulaAdjust() != null || getGcDisabled() != null || getIgnoreBlanks() != null || getInitialFileSize() != null || getLocale() != null || getMergedCellCheckingDisabled() != null || getNamesDisabled() != null || getPropertySets() != null || getRationalization() != null || getSupressWarnings() != null || getTemporaryFileDuringWriteDirectory() != null || getUseTemporaryFileDuringWrite() != null;
   }

   public Boolean getWorkbookProtected()
   {
      return (Boolean) valueOf("workbookProtected", workbookProtected);
   }

   public void setWorkbookProtected(Boolean workbookProtected)
   {
      this.workbookProtected = workbookProtected;
   }

}
