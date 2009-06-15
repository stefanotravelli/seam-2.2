package org.jboss.seam.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.core.Init;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.SortItem;
import org.jboss.seam.util.Sorter;
import org.jboss.seam.web.AbstractFilter;

/**
 * A servlet filter that orchestrates the stack of Seam
 * component filters, and controls ordering. Filter
 * ordering is specified via the @Filter annotation.
 * Filters may optionally extend AbstractFilter.
 * 
 * @see org.jboss.seam.annotations.web.Filter
 * @see AbstractFilter
 * 
 * @author Shane Bryzak
 * @author Pete Muir
 *
 */
public class SeamFilter implements Filter
{
   private static final LogProvider log = Logging.getLogProvider(SeamFilter.class);   
   
   private List<Filter> filters;
   
   private class FilterChainImpl implements FilterChain
   {  
      private FilterChain chain;
      private int index;
           
      private FilterChainImpl(FilterChain chain)
      {
         this.chain = chain;
         index = -1;
      }
      
      public void doFilter(ServletRequest request, ServletResponse response)
          throws IOException, ServletException
      {
         if ( ++index < filters.size() )
         {
            Filter filter = filters.get(index);
            
            if (filter instanceof AbstractFilter)
            {
               AbstractFilter bf = (AbstractFilter) filter;
               if ( bf.isMappedToCurrentRequestPath(request) )
               {
                  filter.doFilter(request, response, this);
               }
               else
               {
                  this.doFilter(request, response);
               }
            }            
            else
            {
               filter.doFilter(request, response, this);
            }
         }
         else
         {
            chain.doFilter(request, response);
         }
      }
   }

   public void init(FilterConfig filterConfig) throws ServletException 
   {
      Lifecycle.setupApplication(new ServletApplicationMap(filterConfig.getServletContext()));
      try
      {
         filters = getSortedFilters();
         for ( Filter filter : filters )
         {
            log.info( "Initializing filter: " + Component.getComponentName(filter.getClass()));
            filter.init(filterConfig);
         }
      }
      finally
      {
         Lifecycle.cleanupApplication();
      }
   }

   private List<Filter> getSortedFilters()
   {
      //retrieve the Filter instances from the application context
      Map<String, SortItem<Filter>> sortItemsMap = new HashMap<String, SortItem<Filter>>();
      List<SortItem<Filter>> sortItems = new ArrayList<SortItem<Filter>>();
      
      for (String filterName : Init.instance().getInstalledFilters())
      {
         Filter filter = (Filter) Component.getInstance(filterName, ScopeType.APPLICATION);
         boolean disabled = false;
         if (filter instanceof AbstractFilter)
         {
             disabled = ((AbstractFilter) filter).isDisabled();
         }
         if (!disabled)
         {
             SortItem<Filter> si = new SortItem<Filter>(filter);         
             sortItemsMap.put(filterName, si);
             sortItems.add(si);
         }
      }

      //create sort items
      for (SortItem<Filter> sortItem : sortItems)
      {
         org.jboss.seam.annotations.web.Filter filterAnn = getFilterAnnotation(sortItem.getObj().getClass());
         if ( filterAnn != null )
         {
            for (String s : Arrays.asList( filterAnn.around() ) )
            {
               SortItem<Filter> aroundSortItem = sortItemsMap.get(s);
               if (sortItem!=null && aroundSortItem != null) sortItem.getAround().add( aroundSortItem );
            }
            for (String s : Arrays.asList( filterAnn.within() ) )
            {
               SortItem<Filter> withinSortItem = sortItemsMap.get(s);
               if (sortItem!=null && withinSortItem != null) sortItem.getWithin().add( withinSortItem );
            }
         }
      }

      // Do the sort
      Sorter<Filter> sList = new Sorter<Filter>();
      sortItems = sList.sort(sortItems);
      List<Filter> sorted = new ArrayList<Filter>();
      for (SortItem<Filter> si: sortItems) sorted.add( si.getObj() );
      return sorted;
   }
   
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
       throws IOException, ServletException
   {
      new FilterChainImpl(chain).doFilter(request, response);
   }
   
   public void destroy() 
   {
      for (Filter filter: filters)
      {
         filter.destroy();
      }
   }
   
   private org.jboss.seam.annotations.web.Filter getFilterAnnotation(Class<?> clazz)
   {
      while (!Object.class.equals(clazz))
      {
         if (clazz.isAnnotationPresent(org.jboss.seam.annotations.web.Filter.class))
         {
            return clazz.getAnnotation(org.jboss.seam.annotations.web.Filter.class);
         }
         else
         {
            clazz = clazz.getSuperclass();
         }
      }
      return null;
   }
   
}
