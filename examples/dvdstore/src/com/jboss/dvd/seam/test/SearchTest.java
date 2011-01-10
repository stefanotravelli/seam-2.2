package com.jboss.dvd.seam.test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.faces.model.ListDataModel;

import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

import com.jboss.dvd.seam.FullTextSearch;
import com.jboss.dvd.seam.Product;

public class SearchTest 
    extends SeamTest
{   
    @Test
    public void testNoParamSearch() 
        throws Exception
    {
        
        new FacesRequest() {
           FullTextSearch search;
            @Override
            protected void updateModelValues()
            {
                search = (FullTextSearch) getInstance("search");
                search.setSearchQuery("king");
            }
            @Override
            protected void invokeApplication()
            {
                String outcome = search.doSearch();
                assertEquals("search outcome", "browse", outcome);
            }
            @Override
            protected void renderResponse()
            {
                ListDataModel model = (ListDataModel) lookup("searchResults");
                //exact number of matches depends on search algorithm,
                //so we only check that at least something was found:
                assertTrue("should have found something",model.isRowAvailable());
                Product firstMatch = (Product) model.getRowData();
                assertTrue("at least top match should have keyword in title",
                      firstMatch.getTitle().toLowerCase().contains("king"));
                assertTrue("in conversation", isLongRunningConversation());
            }
        }.run();
    }
    
}
