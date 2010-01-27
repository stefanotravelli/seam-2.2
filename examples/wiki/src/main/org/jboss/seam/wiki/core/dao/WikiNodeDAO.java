package org.jboss.seam.wiki.core.dao;

import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.jboss.seam.wiki.core.model.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import java.util.*;

/**
 * DAO for nodes, transparently respects security access levels.
 * <p>
 * All node access should go through this component, this component knows
 * about access levels because it relies on a restricted (filtered) Entitymanager.
 *
 * @author Christian Bauer
 *
 */
@Name("wikiNodeDAO")
@AutoCreate
public class WikiNodeDAO {

    @Logger
    static Log log;

    // Most of the DAO methods use this
    @In protected EntityManager restrictedEntityManager;

    // Some run unrestricted (e.g. internal unique key validation of wiki names)
    // Make sure that these methods do not return detached objects!
    @In
    protected EntityManager entityManager;

    public void makePersistent(WikiNode node) {
        entityManager.persist(node);
    }

    public WikiNode findWikiNode(Long nodeId) {
        try {
            return (WikiNode) restrictedEntityManager
                    .createQuery("select n from WikiNode n where n.id = :id")
                    .setParameter("id", nodeId)
                    .setHint("org.hibernate.comment", "Find wikinode by id")
                    .setHint("org.hibernate.cacheable", false)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;

    }

    public List<WikiNode> findWikiNodes(List<Long> ids) {
        return restrictedEntityManager
                .createQuery("select n from WikiNode n where n.id in (:idList)")
                .setParameter("idList", ids)
                .setHint("org.hibernate.comment", "Find wikinodes by id list")
                .setHint("org.hibernate.cacheable", false)
                .getResultList();
    }

    public WikiNode findWikiNodeInArea(Long areaNumber, String wikiname) {
        return findWikiNodeInArea(areaNumber, wikiname, restrictedEntityManager);
    }

    public WikiNode findWikiNodeInArea(Long areaNumber, String wikiname, EntityManager em) {
        try {
            return (WikiNode) em
                    .createQuery("select n from WikiNode n where n.areaNumber = :areaNumber and n.wikiname = :wikiname")
                    .setParameter("areaNumber", areaNumber)
                    .setParameter("wikiname", wikiname)
                    .setHint("org.hibernate.comment", "Find node in area")
                    .setHint("org.hibernate.cacheable", false)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }

    public Long findChildrenCount(WikiNode node) {
        try {
            return (Long) restrictedEntityManager
                    .createQuery("select count(n) from WikiNode n where n.parent = :parent")
                    .setParameter("parent", node)
                    .setHint("org.hibernate.comment", "Find number of wikinode children")
                    .setHint("org.hibernate.cacheable", true)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }

    public List<WikiNode> findChildren(WikiNode node, WikiNode.SortableProperty orderBy, boolean orderAscending, int firstResult, int maxResults) {

        StringBuilder queryString = new StringBuilder();
        queryString.append("select n from WikiNode n where n.parent = :parent").append(" ");
        queryString.append("order by n.").append(orderBy.name()).append(" ").append(orderAscending ? "asc" : "desc");

        return restrictedEntityManager
                .createQuery(queryString.toString())
                .setHint("org.hibernate.comment", "Find wikinode children order by "+orderBy.name())
                .setParameter("parent", node)
                .setHint("org.hibernate.cacheable", false)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .getResultList();
    }

    public List<WikiDirectory> findChildWikiDirectories(WikiDirectory dir) {
        return restrictedEntityManager
                .createQuery("select d from WikiDirectory d left join fetch d.feed where d.parent = :parent")
                .setHint("org.hibernate.comment", "Find wikinode children directories")
                .setParameter("parent", dir)
                .setHint("org.hibernate.cacheable", false)
                .getResultList();
    }

    public WikiComment findWikiComment(Long commentId) {
        try {
            return (WikiComment) restrictedEntityManager
                    .createQuery("select c from WikiComment c where c.id = :id")
                    .setParameter("id", commentId)
                    .setHint("org.hibernate.comment", "Find comment by id")
                    .setHint("org.hibernate.cacheable", false)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }

    public List<WikiComment> findWikiComments(WikiDocument document, boolean orderbyDateAscending) {
        String query =
                "select c from WikiComment c where c.parent = :parentDoc order by c.createdOn " +
                (orderbyDateAscending ? "asc" : "desc");
        return (List<WikiComment>)restrictedEntityManager
                .createQuery(query)
                .setParameter("parentDoc", document)
                .setHint("org.hibernate.comment", "Finding all comments of document")
                .getResultList();
    }

    public WikiFile findWikiFile(Long fileId) {
        try {
            return (WikiFile) restrictedEntityManager
                    .createQuery("select f from WikiFile f where f.id = :id")
                    .setParameter("id", fileId)
                    .setHint("org.hibernate.comment", "Find wikifile by id")
                    .setHint("org.hibernate.cacheable", false)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }

    public WikiFile findWikiFileInArea(Long areaNumber, String wikiname) {
        return findWikiFileInArea(areaNumber, wikiname, restrictedEntityManager);
    }

    public WikiFile findWikiFileInArea(Long areaNumber, String wikiname, EntityManager em) {
        try {
            return (WikiFile) em
                    .createQuery("select f from WikiFile f where f.areaNumber = :areaNumber and f.wikiname = :wikiname")
                    .setParameter("areaNumber", areaNumber)
                    .setParameter("wikiname", wikiname)
                    .setHint("org.hibernate.comment", "Find wikifile in area")
                    .setHint("org.hibernate.cacheable", false)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }

    public WikiDocument findWikiDocument(Long documentId) {
        try {
            return (WikiDocument) restrictedEntityManager
                    .createQuery("select d from WikiDocument d where d.id = :id")
                    .setParameter("id", documentId)
                    .setHint("org.hibernate.comment", "Find document by id")
                    .setHint("org.hibernate.cacheable", false)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }

    // Access restricted version of directory.getDefaultFile()
    public WikiFile findDefaultWikiFile(WikiDirectory directory) {
        if (directory == null) return null;
        try {
            return (WikiFile) restrictedEntityManager
                    .createQuery("select d.defaultFile from WikiDirectory d where d = :dir")
                    .setParameter("dir", directory)
                    .setHint("org.hibernate.comment", "Find default file")
                    .setHint("org.hibernate.cacheable", false)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }
    // Access restricted version of directory.getDefaultFile(), also narrows it down to a document (see WikiRequestResolver)
    public WikiDocument findDefaultDocument(WikiDirectory directory) {
        if (directory == null) return null;
        try {
            return (WikiDocument) restrictedEntityManager
                    .createQuery("select doc from WikiDocument doc, WikiDirectory d where d = :dir and doc.id = d.defaultFile.id")
                    .setParameter("dir", directory)
                    .setHint("org.hibernate.comment", "Find default doc")
                    .setHint("org.hibernate.cacheable", false)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }

    public List<WikiDocument> findWikiDocuments(WikiDirectory directory, WikiNode.SortableProperty orderBy, boolean orderAscending) {

        StringBuilder query = new StringBuilder();
        query.append("select d from WikiDocument d where d.parent = :dir");
        query.append(" order by d.").append(orderBy.name()).append(" ").append(orderAscending ? "asc" : "desc");
        return restrictedEntityManager.createQuery(query.toString())
                .setParameter("dir", directory)
                .setHint("org.hibernate.comment", "Find documents of directory")
                .setHint("org.hibernate.cacheable", false)
                .getResultList();
    }

    public WikiDocument findWikiDocumentInArea(Long areaNumber, String wikiname) {
        return findWikiDocumentInArea(areaNumber, wikiname, restrictedEntityManager);
    }

    public WikiDocument findWikiDocumentInArea(Long areaNumber, String wikiname, EntityManager em) {
        try {
            return (WikiDocument) em
                    .createQuery("select d from WikiDocument d where d.areaNumber = :areaNumber and d.wikiname = :wikiname")
                    .setParameter("areaNumber", areaNumber)
                    .setParameter("wikiname", wikiname)
                    .setHint("org.hibernate.comment", "Find document in area")
                    .setHint("org.hibernate.cacheable", false)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }

    public List<WikiDocument> findWikiDocuments(int maxResults, WikiNode.SortableProperty orderBy, boolean orderAscending) {

        StringBuilder query = new StringBuilder();
        query.append("select d from WikiDocument d where d.").append(orderBy.name()).append(" is not null");
        query.append(" order by d.").append(orderBy.name()).append(" ").append(orderAscending ? "asc" : "desc");


        return (List<WikiDocument>)restrictedEntityManager
                .createQuery(query.toString())
                .setHint("org.hibernate.comment", "Find documents order by " + orderBy.name())
                .setHint("org.hibernate.cacheable", false)
                .setMaxResults(maxResults)
                .getResultList();
    }

    public WikiDocument findSiblingWikiDocumentInDirectory(WikiDocument currentDocument, WikiNode.SortableProperty byProperty, boolean previousOrNext) {
        try {
            return (WikiDocument)restrictedEntityManager
                    .createQuery("select sibling from WikiDocument sibling, WikiDocument current" +
                                 " where sibling.parent = current.parent and current = :current and not sibling = :current" +
                                 " and sibling."+ byProperty.name() + " " + (previousOrNext ? "<=" : ">=") + "current."+ byProperty.name() +
                                 " order by sibling." +byProperty + " " + (previousOrNext ? "desc" : "asc") )
                    .setHint("org.hibernate.cacheable", false)
                    .setMaxResults(1)
                    .setParameter("current", currentDocument)
                    .getSingleResult();
            } catch (EntityNotFoundException ignored) {
            } catch (NoResultException ignored) {
        }
        return null;
    }

    public WikiUpload findWikiUpload(Long uploadId) {
        try {
            return (WikiUpload) restrictedEntityManager
                    .createQuery("select u from WikiUpload u where u.id = :id")
                    .setParameter("id", uploadId)
                    .setHint("org.hibernate.comment", "Find upload by id")
                    .setHint("org.hibernate.cacheable", false)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }

    public List<WikiUpload> findWikiUploads(WikiDirectory directory, WikiNode.SortableProperty orderBy, boolean orderAscending) {
        StringBuilder query = new StringBuilder();
        query.append("select u from WikiUpload u where u.parent = :dir");
        query.append(" order by u.").append(orderBy.name()).append(" ").append(orderAscending ? "asc" : "desc");

        return restrictedEntityManager.createQuery(query.toString())
                .setParameter("dir", directory)
                .setHint("org.hibernate.comment", "Find uploads of directory")
                .setHint("org.hibernate.cacheable", false)
                .getResultList();
    }

    public WikiDirectory findWikiDirectory(Long directoryId) {
        try {
            return (WikiDirectory) restrictedEntityManager
                    .createQuery("select d from WikiDirectory d left join fetch d.feed where d.id = :id")
                    .setParameter("id", directoryId)
                    .setHint("org.hibernate.comment", "Find directory by id")
                    .setHint("org.hibernate.cacheable", false)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }

    public User findWikiDirectoryMemberHome(Long directoryId) {
        try {
            return (User) restrictedEntityManager
                    .createQuery("select u from User u where u.memberHome.id = :id")
                    .setParameter("id", directoryId)
                    .setHint("org.hibernate.comment", "Find user for directory member home by id")
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }

    public WikiDirectory findWikiDirectoryInAreaUnrestricted(Long areaNumber, String wikiname) {
        return findWikiDirectoryInArea(areaNumber, wikiname, entityManager);
    }

    public WikiDirectory findWikiDirectoryInArea(Long areaNumber, String wikiname) {
        return findWikiDirectoryInArea(areaNumber, wikiname, restrictedEntityManager);
    }

    public WikiDirectory findWikiDirectoryInArea(Long areaNumber, String wikiname, EntityManager em) {
        try {
            return (WikiDirectory) em
                    .createQuery("select d from WikiDirectory d left join fetch d.feed where d.areaNumber = :areaNumber and d.wikiname = :wikiname")
                    .setParameter("areaNumber", areaNumber)
                    .setParameter("wikiname", wikiname)
                    .setHint("org.hibernate.comment", "Find directory in area")
                    .setHint("org.hibernate.cacheable", false)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }

    public List<Long> findWikiDirectoryTreeIDs(WikiDirectory startDir) {
        List<Long> parentDirIds = new ArrayList();
        List<WikiTreeNode<WikiDirectory>> directoryTree = findWikiDirectoryTree(startDir, WikiNode.SortableProperty.createdOn, true);
        for (WikiTreeNode<WikiDirectory> treeNode : directoryTree) {
            parentDirIds.add(treeNode.getNode().getId());
        }
        return parentDirIds;
    }

    public List<WikiTreeNode<WikiDirectory>> findWikiDirectoryTree(WikiDirectory rootDir, WikiNode.SortableProperty sortByProperty, boolean sortAscending) {
        List<WikiTreeNode<WikiDirectory>> tree = new ArrayList();
        long level = 1;
        tree.add(new WikiTreeNode<WikiDirectory>(level++, rootDir));

        String query = "select d from WikiDirectory d where d.parent.id = :parentNodeId and d.readAccessLevel <= :readAccessLevel order by " +
                sortByProperty.name() + " " + (sortAscending ? "asc" : "desc");

        appendWikiNodeChildren(query, tree, rootDir.getId(), level, null, null);
        return tree;
    }

    public List<WikiTreeNode<WikiDirectory>> findMenuItemTree(WikiDirectory rootDir, Long maxDepth, Long flattenToLevel, boolean onlyCreatedByAdminUser) {
        List<WikiTreeNode<WikiDirectory>> tree = new ArrayList();
        long level = 1;
        // TODO: Root in or out?
        // tree.add(new WikiTreeNode<WikiDirectory>(level++, rootDir));

        String query = "select d from WikiMenuItem mi join mi.directory d" +
                " where d.parent.id = :parentNodeId" +
                " and d.readAccessLevel <= :readAccessLevel" +
                (onlyCreatedByAdminUser ? " and d.createdBy.id = '"+((User)Component.getInstance("adminUser")).getId()+"'" : "") +
                " order by mi.displayPosition asc";

        appendWikiNodeChildren(query, tree, rootDir.getId(), level, maxDepth, flattenToLevel);
        return tree;
    }

    // Recursive! Don't use for large trees...
    private void appendWikiNodeChildren(String query, List tree, long parentNodeId, long currentLevel, Long maxDepth, Long flattenToLevel) {
        List<WikiNode> nodes = restrictedEntityManager.createQuery(query)
                .setHint("org.hibernate.comment", "Querying children of wiki node: " + parentNodeId)
                .setParameter("parentNodeId", parentNodeId)
                .setParameter("readAccessLevel", Component.getInstance("currentAccessLevel"))
                .getResultList();
        for (WikiNode node : nodes) {
            tree.add(new WikiTreeNode(currentLevel, node));
            if (maxDepth == null || currentLevel < maxDepth) {
                if (flattenToLevel == null || currentLevel < flattenToLevel) {
                    currentLevel++;
                    appendWikiNodeChildren(query, tree, node.getId(), currentLevel, maxDepth, flattenToLevel);
                    currentLevel--;
                } else {
                    appendWikiNodeChildren(query, tree, node.getId(), currentLevel, maxDepth, flattenToLevel);
                }
            }
        }
    }

    public WikiDirectory findAreaUnrestricted(String wikiname) {
        return findArea(wikiname, entityManager);
    }

    public WikiDirectory findArea(String wikiname) {
        return findArea(wikiname, restrictedEntityManager);
    }

    public WikiDirectory findArea(String wikiname, EntityManager em) {
        try {
            return (WikiDirectory) em
                    .createQuery("select d from WikiDirectory d left join fetch d.feed, WikiDirectory r where r.parent is null and d.parent = r and d.wikiname = :wikiname")
                    .setParameter("wikiname", wikiname)
                    .setHint("org.hibernate.comment", "Find area by wikiname")
                    .setHint("org.hibernate.cacheable", false)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }

    public WikiDirectory findArea(Long areaNumber) {
        try {
            return (WikiDirectory) restrictedEntityManager
                    .createQuery("select d from WikiDirectory d left join fetch d.feed, WikiDirectory r where r.parent is null and d.parent = r and d.areaNumber = :areaNumber")
                    .setParameter("areaNumber", areaNumber)
                    .setHint("org.hibernate.comment", "Find area by area number")
                    .setHint("org.hibernate.cacheable", false)
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;
    }

    // TODO: This method is a mess...
    // Returns a detached object
    public WikiDocument findHistoricalDocumentAndDetach(String entityName, Long historyId) {
        // Initialize bytecode-enhanced fields with 'fetch all properties'!
        log.debug("fetching WikiFile historical revision with id: " + historyId + " and initializing all properties");
        WikiDocument historicalFile = (WikiDocument)
                getSession(true).createQuery("select f from " + entityName + " f fetch all properties where f.historicalFileId = :id")
                .setParameter("id", historyId)
                .uniqueResult();
        if (historicalFile != null) {
            historicalFile.getContent(); // TODO: the fetch all properties doesn't work for some reason..
            getSession(true).evict(historicalFile);
            return historicalFile;
        } else {
            return null;
        }
    }

    public List<WikiFile> findHistoricalFiles(WikiFile file) {
        if (file == null || file.getId() == null) return null;
        return getSession(true).createQuery("select f from " + file.getHistoricalEntityName() + " f where f.id = :fileId order by f.revision desc")
                                .setParameter("fileId", file.getId())
                                .list();
    }

    public Long findNumberOfHistoricalFiles(WikiFile file) {
        if (file == null || file.getId() == null) return 0l;
        return (Long)getSession(true).createQuery("select count(f) from " + file.getHistoricalEntityName() + " f where f.id = :fileId")
                                  .setParameter("fileId", file.getId())
                                  .setCacheable(true)
                                  .uniqueResult();
    }

    public void persistHistoricalFile(WikiFile historicalFile) {
        if (historicalFile.getHistoricalEntityName() != null) {
            log.debug("persisting historical file: " + historicalFile + " as revision: " + historicalFile.getRevision());
            // Use the nonrestricted persistence context, which should be safe here so we can flush it again
            // TODO: I wish Hibernate/JBoss weren't so retarted about getting a StatelessSession when you need one...
            getSession(false).persist(historicalFile.getHistoricalEntityName(), historicalFile);
            getSession(false).flush();
            getSession(false).evict(historicalFile);
        }
    }

    public void removeHistoricalFiles(WikiFile file) {
        if (file == null || file.getId() == null) return;
        ((WikiFile)getSession(true).load(WikiFile.class, file.getId())).setRevision(0);
        getSession(true).flush();
        getSession(true).createQuery("delete from " + file.getHistoricalEntityName() + " f where f.id = :fileId")
                         .setParameter("fileId", file.getId())
                         .executeUpdate();
    }

    // Multi-row constraint validation
    public boolean isUniqueWikiname(Long areaNumber, WikiNode node) {
        WikiNode foundNode = findWikiNodeInArea(areaNumber, node.getWikiname(), entityManager);
        return foundNode == null || node.getId() != null && node.getId().equals(foundNode.getId());
    }

    public boolean isUniqueWikiname(Long areaNumber, String wikiname) {
        WikiNode foundNode = findWikiNodeInArea(areaNumber, wikiname, entityManager);
        return foundNode == null;
    }

    public WikiMenuItem findMenuItem(WikiDirectory dir) {
        try {
            return (WikiMenuItem) restrictedEntityManager
                    .createQuery("select m from WikiMenuItem m where m.directory = :dir")
                    .setParameter("dir", dir)
                    .setHint("org.hibernate.comment", "Find menu item of directory")
                    .getSingleResult();
        } catch (EntityNotFoundException ignored) {
        } catch (NoResultException ignored) {
        }
        return null;

    }

    public List<WikiMenuItem> findMenuItems(WikiDirectory parentDir) {
        return restrictedEntityManager.createQuery("select m from WikiMenuItem m where m.directory.parent = :parent order by m.displayPosition asc")
                .setParameter("parent", parentDir)
                .getResultList();
    }

    private Session getSession(boolean restricted) {
        if (restricted) {
            return ((Session)((org.jboss.seam.persistence.EntityManagerProxy) restrictedEntityManager).getDelegate());
        } else {
            return ((Session)((org.jboss.seam.persistence.EntityManagerProxy) entityManager).getDelegate());
        }
    }

    public static WikiNodeDAO instance() {
        return (WikiNodeDAO)Component.getInstance(WikiNodeDAO.class);
    }

}
