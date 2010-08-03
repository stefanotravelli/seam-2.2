package org.jboss.seam.wiki.core.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.store.DirectoryProvider;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Validators;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.Identity;
import org.jboss.seam.wiki.core.dao.SpamReportDAO;
import org.jboss.seam.wiki.core.dao.WikiNodeDAO;
import org.jboss.seam.wiki.core.model.Blacklist;
import org.jboss.seam.wiki.core.model.LinkProtocol;
import org.jboss.seam.wiki.core.model.User;
import org.jboss.seam.wiki.core.model.WikiSpamReport;
import org.jboss.seam.wiki.core.plugin.PluginRegistry;
import org.jboss.seam.wiki.core.plugin.metamodel.Plugin;
import org.jboss.seam.wiki.core.search.IndexManager;
import org.jboss.seam.wiki.core.search.metamodel.SearchRegistry;
import org.jboss.seam.wiki.core.search.metamodel.SearchableEntity;
import org.jboss.seam.wiki.preferences.PreferenceVisibility;
import org.jboss.seam.wiki.preferences.metamodel.PreferenceEntity;
import org.jboss.seam.wiki.util.Progress;

@Name("adminHome")
@Scope(ScopeType.CONVERSATION)
public class AdminHome implements Serializable {

    @Logger
    static Log log;

    @In
    private StatusMessages statusMessages;

    @In
    EntityManager entityManager;
    
    @In
    SpamReportDAO spamReportDAO;
    
    @In
    WikiNodeDAO wikiNodeDAO;

    @Create
    public void create() {
        if (!Identity.instance().hasPermission("User", "isAdmin", (User)Component.getInstance("currentUser") ) ) {
            throw new AuthorizationException("You don't have permission for this operation");
        }
    }

    @Restrict("#{s:hasPermission('User', 'isAdmin', currentUser)}")
    public String update() {
        log.debug("updating system settings");

        // Preferences
        if (preferenceEditor != null) {
            String editorFailed = preferenceEditor.save();
            if (editorFailed != null) return null;
        }

        log.debug("flushing the entityManager (maybe again)");
        entityManager.flush(); // Flush everything (maybe again if prefEditor.save() already flushed)

        statusMessages.addFromResourceBundleOrDefault(
            StatusMessage.Severity.INFO,
            "lacewiki.msg.SystemSettingsUpdated",
            "System settings updated"
        );

        return null;
    }

    // ####################### PREFERENCES ##################################

    PreferenceEditor preferenceEditor;

    @DataModel(value = "systemPreferenceEntities")
    private List<PreferenceEntity> systemPreferenceEntities;

    @Factory("systemPreferenceEntities")
    public void initPreferencesEditor() {
        preferenceEditor = (PreferenceEditor)Component.getInstance(PreferenceEditor.class);
        preferenceEditor.setVisibilities(new PreferenceVisibility[] {PreferenceVisibility.SYSTEM});
        systemPreferenceEntities = preferenceEditor.getPreferenceEntities();
        Contexts.getConversationContext().set("preferenceEditor", preferenceEditor);
    }

    // ####################### LINK PROTOCOLS ##################################

    @DataModel(value = "linkProtocols")
    private List<LinkProtocol> linkProtocols;

    @DataModelSelection(value = "linkProtocols")
    private LinkProtocol selectedLinkProtocol;
    LinkProtocol linkProtocol = new LinkProtocol();
    LinkProtocol newLinkProtocol;

    @Factory("linkProtocols")
    public void loadLinkProtocols() {
        //noinspection unchecked
        Map<String, LinkProtocol> linkProtocolMap = (Map<String, LinkProtocol>)Component.getInstance("linkProtocolMap");
        linkProtocols = new ArrayList<LinkProtocol>(linkProtocolMap.values());
    }

    public LinkProtocol getLinkProtocol() {
        return linkProtocol;
    }

    public void addLinkProtocol() {

        // TODO: http://jira.jboss.com/jira/browse/JBSEAM-1297
        ClassValidator<LinkProtocol> validator = Validators.instance().getValidator(LinkProtocol.class);
        InvalidValue[] ivs = validator.getInvalidValues(linkProtocol);
        if (ivs.length>0) {
            for (InvalidValue iv : ivs) {
                statusMessages.addToControl(iv.getPropertyName(), StatusMessage.Severity.INFO, iv.getMessage());
            }
            return;
        }

        newLinkProtocol = linkProtocol;
        linkProtocols.add(newLinkProtocol);
        entityManager.persist(newLinkProtocol);
        linkProtocol = new LinkProtocol();
    }

    public void removeLinkProtocol() {
        entityManager.remove(selectedLinkProtocol);
        linkProtocols.remove(selectedLinkProtocol);
    }
    
    // ####################### SPAM REPORTS ##################################
    
    @DataModel(value = "spamReports")
    private List<WikiSpamReport> spamReports;
    
    @Factory(value = "spamReports")
    public void refreshSpamReports()
    {
       spamReports = entityManager.createQuery("select r from WikiSpamReport r")
           .getResultList();
    }    
        
    public String removeSpamReport(Long commentId)
    {              
       spamReportDAO.removeReports(spamReportDAO.findReports(wikiNodeDAO.findWikiComment(commentId)));
       refreshSpamReports();
       return null;
    }    
    
    // ####################### BLACKLIST ##################################
    
    @DataModel(value = "blacklist")
    private List<Blacklist> blacklist;
    
    @Factory(value = "blacklist")
    public void refreshBlacklist()
    {
       blacklist = entityManager.createQuery("select b from Blacklist b").getResultList();
    }
    
    public String removeBlacklist(Long id)
    {
       Blacklist bl = entityManager.find(Blacklist.class, id);
       if (bl != null)
       {
          entityManager.remove(bl);
          entityManager.flush();
       }       
       
       refreshBlacklist();
       return null;
    }

    // ####################### INDEXING ##################################

    @DataModel(value = "indexedEntities")
    private List<SearchableEntity> indexedEntities;

    @DataModelSelection(value = "indexedEntities")
    private SearchableEntity selectedIndexedEntity;

    @Factory("indexedEntities")
    public void loadIndexedEntities() throws Exception {

        SearchRegistry registry = (SearchRegistry)Component.getInstance(SearchRegistry.class);
        indexedEntities = registry.getSearchableEntities();

        EntityManager em = (EntityManager) Component.getInstance("entityManager");

        for (SearchableEntity indexedEntity : indexedEntities) {
            DirectoryProvider dirProvider = ((FullTextSession)em.getDelegate()).getSearchFactory().getDirectoryProviders(indexedEntity.getClazz())[0];
            IndexReader reader = IndexReader.open(dirProvider.getDirectory());

            indexedEntity.setNumOfIndexedDocuments(reader.numDocs());

            TermEnum te = reader.terms();
            long numTerms = 0;
            while (te.next()) numTerms++;
            indexedEntity.setNumOfIndexedTerms(numTerms);

            long size = 0;
            String [] fileNames = dirProvider.getDirectory().list();
            for (String fileName : fileNames) {
                size += dirProvider.getDirectory().fileLength(fileName);
            }
            indexedEntity.setIndexSizeInBytes(size);

            reader.close();
        }
    }

    @In(required = false) @Out(required = false, scope = ScopeType.SESSION)
    public Map<String, Progress> indexingProgressMonitors;

    public void resetSearchIndex() throws Exception {

        IndexManager indexMgr = (IndexManager)Component.getInstance(IndexManager.class);
        Progress progress = new Progress(selectedIndexedEntity.getClazz().getName());
        indexMgr.rebuildIndex(selectedIndexedEntity.getClazz(), progress);

        if (indexingProgressMonitors == null) indexingProgressMonitors = new HashMap<String, Progress>();
        indexingProgressMonitors.put(selectedIndexedEntity.getClazz().getName(), progress);
    }

    @WebRemote
    public Progress getIndexingProgress(String className) {
        return indexingProgressMonitors != null ? indexingProgressMonitors.get(className) : null;
    }

    @WebRemote
    public void resetIndexingProgress(String className) {
        if (indexingProgressMonitors != null) indexingProgressMonitors.remove(className);
    }

    // ####################### PLUGINS ##################################

    @In
    private PluginRegistry pluginRegistry;

    private List<Plugin> installedPlugins;
    private Plugin selectedInstalledPlugin;

    public List<Plugin> getInstalledPlugins() {
        if (installedPlugins == null) installedPlugins = pluginRegistry.getPlugins();
        return installedPlugins;
    }

    public void selectInstalledPlugin(Plugin selectedPlugin) {
        selectedInstalledPlugin = selectedPlugin;
    }

    public Plugin getSelectedInstalledPlugin() {
        return selectedInstalledPlugin;
    }
}
