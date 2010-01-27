/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.wiki.core.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.Component;
import org.jboss.seam.security.Identity;
import org.jboss.seam.annotations.*;
import org.jboss.seam.log.Log;
import org.jboss.seam.wiki.core.action.prefs.WikiPreferences;
import org.jboss.seam.wiki.core.dao.WikiNodeDAO;
import org.jboss.seam.wiki.core.model.WikiDirectory;
import org.jboss.seam.wiki.core.model.WikiTreeNode;
import org.jboss.seam.wiki.core.cache.PageFragmentCache;
import org.jboss.seam.wiki.preferences.Preferences;

import java.io.Serializable;
import java.util.List;

/**
 * Holds the nodes that are displayed in the site menu
 *
 * @author Christian Bauer
 */
@Name("menu")
@Scope(ScopeType.SESSION)
public class Menu implements Serializable {

    public static final String CACHE_REGION = "wiki.MainMenu";
    public static final String CACHE_KEY = "MainMenuForAccessLevel";

    @Logger
    Log log;

    @In
    Integer currentAccessLevel;

    List<WikiTreeNode<WikiDirectory>> tree;
    public List<WikiTreeNode<WikiDirectory>> getTree() {
        if (tree == null) {
            refreshTree();
        }
        return tree;
    }

    public String getCacheRegion() {
        return CACHE_REGION;
    }

    public String getCacheKey() {
        return CACHE_KEY + currentAccessLevel;
    }

    // Logout invalidates the session context, so we don't need to refresh afterwards
    @Observer(value = { "Node.updated", "Node.removed", Identity.EVENT_LOGIN_SUCCESSFUL})
    public void invalidateCache() {
        log.debug("invaliding menu items tree cache");
        PageFragmentCache.instance().removeAll(CACHE_REGION);
        tree = null;
    }

    private void refreshTree() {
        log.debug("Loading menu items tree");
        WikiPreferences wikiPreferences = Preferences.instance().get(WikiPreferences.class);
        tree = WikiNodeDAO.instance().findMenuItemTree(
                (WikiDirectory)Component.getInstance("wikiRoot"),
                wikiPreferences.getMainMenuDepth(),
                wikiPreferences.getMainMenuLevels(),
                wikiPreferences.isMainMenuShowAdminOnly()
        );
    }
}
