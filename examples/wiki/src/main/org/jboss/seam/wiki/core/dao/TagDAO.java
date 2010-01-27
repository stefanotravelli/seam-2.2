package org.jboss.seam.wiki.core.dao;

import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.transform.Transformers;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.jboss.seam.wiki.core.model.*;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;

@Name("tagDAO")
@AutoCreate
public class TagDAO {

    @Logger
    static Log log;

    @In
    protected EntityManager restrictedEntityManager;

    @In
    protected WikiNodeDAO wikiNodeDAO;

    public List<DisplayTagCount> findTagCounts(WikiDirectory startDir, WikiFile ignoreFile, int limit, long minimumCount) {

        StringBuilder queryString = new StringBuilder();
        queryString.append("select t as tag, count(t) as count").append(" ");
        queryString.append("from WikiFile f join f.tags as t").append(" ");
        queryString.append("where f.parent.id in (:parentDirIds) ");
        if (ignoreFile != null && ignoreFile.getId() != null) queryString.append("and not f = :ignoreFile").append(" ");
        queryString.append("group by t").append(" ");
        queryString.append("having count(t) >= :minimumCount").append(" ");
        queryString.append("order by count(t) desc, t asc ");

        Query tagQuery = getSession().createQuery(queryString.toString());
        tagQuery.setParameterList("parentDirIds", wikiNodeDAO.findWikiDirectoryTreeIDs(startDir));
        tagQuery.setParameter("minimumCount", minimumCount);
        if (ignoreFile != null && ignoreFile.getId() != null)
            tagQuery.setParameter("ignoreFile", ignoreFile);
        if (limit > 0) {
            tagQuery.setMaxResults(limit);
        }

        tagQuery.setResultTransformer(Transformers.aliasToBean(DisplayTagCount.class));

        return tagQuery.list();
    }

    public List<WikiFile> findWikFiles(WikiDirectory startDir, WikiFile ignoreFile, final String tag,
                                       WikiNode.SortableProperty orderBy, boolean orderAscending) {

        if (tag == null || tag.length() == 0) return Collections.EMPTY_LIST;

        StringBuilder queryString = new StringBuilder();

        queryString.append("select distinct f from WikiFile f join f.tags as t ");
        queryString.append("where f.parent.id in (:parentDirIds) ");
        if (ignoreFile != null && ignoreFile.getId() != null) queryString.append("and not f = :ignoreFile").append(" ");
        queryString.append("and t = :tag").append(" ");
        queryString.append("order by f.").append(orderBy.name()).append(" ").append(orderAscending ? "asc" : "desc");

        Query fileQuery = getSession().createQuery(queryString.toString());
        fileQuery.setParameterList("parentDirIds", wikiNodeDAO.findWikiDirectoryTreeIDs(startDir));
        if (ignoreFile != null && ignoreFile.getId() != null)
            fileQuery.setParameter("ignoreFile", ignoreFile);
        fileQuery.setParameter("tag", tag);

        return fileQuery.list();
    }

    private Session getSession() {
        return ((Session)((org.jboss.seam.persistence.EntityManagerProxy) restrictedEntityManager).getDelegate());
    }


}
