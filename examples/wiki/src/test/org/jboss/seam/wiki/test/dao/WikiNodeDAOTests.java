/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.wiki.test.dao;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.wiki.core.dao.WikiNodeDAO;
import org.jboss.seam.wiki.core.model.*;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.Test;
import org.testng.Assert;

import java.util.List;

public class WikiNodeDAOTests extends DBUnitSeamTest {

    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
                new DataSetOperation("org/jboss/seam/wiki/test/WikiBaseData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }

    @Test
    public void findDocumentById() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                WikiDocument d = dao.findWikiDocument(6l);
                assert d.getName().equals("One");
            }
        }.run();
    }

    @Test
    public void findDefaultFile() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                WikiDirectory dir = dao.findWikiDirectory(3l);
                WikiFile d = dao.findDefaultWikiFile(dir);
                assert d.getName().equals("One");
            }
        }.run();
    }

    // TODO: This can go away soon, see WikiRequestResolver
    @Test
    public void findDefaultDocument() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                WikiDirectory dir = dao.findWikiDirectory(3l);
                WikiDocument d = dao.findDefaultDocument(dir);
                assert d.getName().equals("One");
            }
        }.run();
    }

    @Test
    public void findDocumentInArea() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                WikiDocument d = dao.findWikiDocumentInArea(3l, "Two");
                assert d.getName().equals("Two");
                assert d.getId().equals(7l);
            }
        }.run();
    }

    @Test
    public void findDocumentsOrderByLastModified() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                List<WikiDocument> result = dao.findWikiDocuments(2, WikiNode.SortableProperty.lastModifiedOn, false);
                assert result.size() == 2;
                assert result.get(0).getId().equals(6l);
                assert result.get(1).getId().equals(7l);
            }
        }.run();
    }

    @Test
    public void findDirectoryById() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                WikiDirectory d = dao.findWikiDirectory(1l);
                assert d.getName().equals("AAA");
            }
        }.run();
    }

    @Test
    public void findDirectoryInArea() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                WikiDirectory d = dao.findWikiDirectoryInArea(3l, "DDD");
                assert d.getName().equals("DDD");
                assert d.getId().equals(4l);
            }
        }.run();
    }

    @Test
    public void findAreaByWikiname() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                WikiDirectory d = dao.findArea("BBB");
                assert d.getName().equals("BBB");
                assert d.getId().equals(2l);
            }
        }.run();
    }

    @Test
    public void findAreaByNumber() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                WikiDirectory d = dao.findArea(2l);
                assert d.getName().equals("BBB");
                assert d.getId().equals(2l);
            }
        }.run();
    }

    @Test
    public void isUniqueWikinameTrue() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                Boolean result = dao.isUniqueWikiname(3l, "Foobar");
                assert result;
            }
        }.run();
    }

    @Test
    public void isUniqueWikinameFalse() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                Boolean result = dao.isUniqueWikiname(3l, "One");
                assert !result;
            }
        }.run();
    }

    @Test
    public void isUniqueWikinameTrueWithNode() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                WikiDocument newDoc = new WikiDocument();
                newDoc.setWikiname("Foobar");
                Boolean result = dao.isUniqueWikiname(3l, newDoc);
                assert result;
            }
        }.run();
    }

    @Test
    public void isUniqueWikinameFalseWithNode() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                WikiDocument newDoc = new WikiDocument();
                newDoc.setWikiname("One");
                Boolean result = dao.isUniqueWikiname(3l, newDoc);
                assert !result;
            }
        }.run();
    }

    @Test
    public void findComments() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                WikiDocument d = dao.findWikiDocument(6l);
                assert d.getName().equals("One");

                List<WikiComment> comments = dao.findWikiComments(d, true);
                assert comments.size() == 6;

                assert comments.get(0).getId().equals(10l);
                assert comments.get(1).getId().equals(11l);
                assert comments.get(2).getId().equals(12l);
                assert comments.get(3).getId().equals(13l);
                assert comments.get(4).getId().equals(14l);
                assert comments.get(5).getId().equals(15l);
            }
        }.run();
    }

    @Test
    public void findSiblings() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                WikiDocument d = dao.findWikiDocument(6l);
                assert d.getName().equals("One");

                assert dao.findSiblingWikiDocumentInDirectory(d, WikiNode.SortableProperty.createdOn, true) == null;
                assert dao.findSiblingWikiDocumentInDirectory(d, WikiNode.SortableProperty.createdOn, false).getId().equals(7l);
            }
        }.run();
    }

    @Test
    public void findWikiDirectoryTree() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);

                WikiDirectory root = dao.findWikiDirectory(1l);
                List<WikiTreeNode<WikiDirectory>> tree = dao.findWikiDirectoryTree(root, WikiNode.SortableProperty.name, true);

                Assert.assertEquals(tree.size(), 7);

                Assert.assertEquals(tree.get(0).getLevel(), 1);
                Assert.assertEquals(tree.get(0).getNode().getName(), "AAA");
                Assert.assertEquals(tree.get(1).getLevel(), 2);
                Assert.assertEquals(tree.get(1).getNode().getName(), "BBB");
                Assert.assertEquals(tree.get(2).getLevel(), 2);
                Assert.assertEquals(tree.get(2).getNode().getName(), "CCC");
                Assert.assertEquals(tree.get(3).getLevel(), 3);
                Assert.assertEquals(tree.get(3).getNode().getName(), "DDD");
                Assert.assertEquals(tree.get(4).getLevel(), 3);
                Assert.assertEquals(tree.get(4).getNode().getName(), "EEE");
                Assert.assertEquals(tree.get(5).getLevel(), 2);
                Assert.assertEquals(tree.get(5).getNode().getName(), "Members");
                Assert.assertEquals(tree.get(6).getLevel(), 2);
                Assert.assertEquals(tree.get(6).getNode().getName(), "Trash");

            }
        }.run();
    }

    @Test
    public void findWikiDirectorySubtree() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);

                WikiDirectory root = dao.findWikiDirectory(3l);
                List<WikiTreeNode<WikiDirectory>> tree = dao.findWikiDirectoryTree(root, WikiNode.SortableProperty.name, false);

                Assert.assertEquals(tree.size(), 3);

                Assert.assertEquals(tree.get(0).getLevel(), 1);
                Assert.assertEquals(tree.get(0).getNode().getName(), "CCC");
                Assert.assertEquals(tree.get(1).getLevel(), 2);
                Assert.assertEquals(tree.get(1).getNode().getName(), "EEE");
                Assert.assertEquals(tree.get(2).getLevel(), 2);
                Assert.assertEquals(tree.get(2).getNode().getName(), "DDD");

            }
        }.run();
    }

    @Test
    public void findMenuItems() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);

                WikiDirectory root = dao.findWikiDirectory(1l);

                List<WikiTreeNode<WikiDirectory>> tree = dao.findMenuItemTree(root, 3l, 3l, false);
                Assert.assertEquals(tree.size(), 3);
                Assert.assertEquals(tree.get(0).getLevel(), 1);
                Assert.assertEquals(tree.get(0).getNode().getName(), "BBB");
                Assert.assertEquals(tree.get(1).getLevel(), 1);
                Assert.assertEquals(tree.get(1).getNode().getName(), "CCC");
                Assert.assertEquals(tree.get(2).getLevel(), 2);
                Assert.assertEquals(tree.get(2).getNode().getName(), "DDD");

                tree = dao.findMenuItemTree(root, 3l, 1l, false);
                Assert.assertEquals(tree.size(), 3);
                Assert.assertEquals(tree.get(0).getLevel(), 1);
                Assert.assertEquals(tree.get(0).getNode().getName(), "BBB");
                Assert.assertEquals(tree.get(1).getLevel(), 1);
                Assert.assertEquals(tree.get(1).getNode().getName(), "CCC");
                Assert.assertEquals(tree.get(2).getLevel(), 1);
                Assert.assertEquals(tree.get(2).getNode().getName(), "DDD");

                tree = dao.findMenuItemTree(root, 1l, 3l, false);
                Assert.assertEquals(tree.size(), 2);
                Assert.assertEquals(tree.get(0).getLevel(), 1);
                Assert.assertEquals(tree.get(0).getNode().getName(), "BBB");
                Assert.assertEquals(tree.get(1).getLevel(), 1);
                Assert.assertEquals(tree.get(1).getNode().getName(), "CCC");

            }
        }.run();
    }

}
