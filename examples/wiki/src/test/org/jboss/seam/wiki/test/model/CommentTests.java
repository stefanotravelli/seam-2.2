/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.wiki.test.model;

import org.jboss.seam.mock.DBUnitSeamTest;
import org.jboss.seam.wiki.core.model.WikiDocument;
import org.jboss.seam.wiki.core.model.WikiComment;
import org.jboss.seam.wiki.core.model.User;
import org.jboss.seam.wiki.util.WikiUtil;
import org.dbunit.operation.DatabaseOperation;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import javax.persistence.EntityManager;
import java.util.List;

public class CommentTests extends DBUnitSeamTest {

    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
                new DataSetOperation("org/jboss/seam/wiki/test/WikiBaseData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }

    @Test
    public void findAllComments() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                EntityManager em = (EntityManager) getInstance("restrictedEntityManager");
                WikiDocument d = (WikiDocument)
                        em.createQuery("select d from WikiDocument d where d.id = :id")
                                .setParameter("id", 6l)
                                .getSingleResult();
                assert d.getName().equals("One");

                List<WikiComment> comments =
                        em.createQuery("select c from WikiComment c where c.parent = :doc order by c.createdOn asc")
                        .setParameter("doc", d)
                        .getResultList();

                assert comments.size() == 6;

                assert comments.get(0).getName().equals("One.Comment11967298211870");
                assert comments.get(1).getSubject().equals("Two");
                assert comments.get(2).getSubject().equals("Three");
                assert comments.get(3).getSubject().equals("Four");
                assert comments.get(4).getSubject().equals("Five");
                assert comments.get(5).getSubject().equals("Six");
            }
        }.run();
    }

    @Test
    public void insertCommentNewThread() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                EntityManager em = (EntityManager) getInstance("restrictedEntityManager");
                WikiDocument d = (WikiDocument)
                        em.createQuery("select d from WikiDocument d where d.id = :id")
                                .setParameter("id", 6l)
                                .getSingleResult();
                assert d.getName().equals("One");

                WikiComment newComment = new WikiComment();

                newComment.setAreaNumber(d.getAreaNumber());
                newComment.setDerivedName(d);
                newComment.setWikiname(WikiUtil.convertToWikiName(newComment.getName()));
                newComment.setCreatedBy(em.find(User.class, 1l));

                newComment.setSubject("Seven");
                newComment.setContent("Testcomment Seven");
                newComment.setUseWikiText(true);

                newComment.setParent(d);

                em.persist(newComment);

                em.flush();
                em.clear();

                List<WikiComment> comments =
                        em.createQuery("select c from WikiComment c where c.parent = :doc order by c.createdOn asc")
                        .setParameter("doc", d)
                        .getResultList();

                assert comments.size() == 7;

                assert comments.get(0).getName().equals("One.Comment11967298211870");
                assert comments.get(1).getSubject().equals("Two");
                assert comments.get(2).getSubject().equals("Three");
                assert comments.get(3).getSubject().equals("Four");
                assert comments.get(4).getSubject().equals("Five");
                assert comments.get(5).getSubject().equals("Six");
                assert comments.get(6).getSubject().equals("Seven");
                assert comments.get(6).getId().equals(newComment.getId());

            }
        }.run();
    }


    @Test
    public void findCommentParent() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                EntityManager em = (EntityManager) getInstance("restrictedEntityManager");
                WikiComment comment  = (WikiComment)
                        em.createQuery("select c from WikiComment c where c.id = :id")
                                .setParameter("id", 13l)
                                .getSingleResult();
                assert comment.getSubject().equals("Four");
                assert comment.getParent().getId().equals(6l);
                assertEquals(comment.getPermURL(".lace"), "6.lace#comment13");
                assertEquals(comment.getWikiURL(), "CCC/One#comment13");
            }
        }.run();
    }


}