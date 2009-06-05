package org.jboss.seam.pdf.ui;

import javax.faces.context.FacesContext;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

public class UIBarChart extends UICategoryChartBase
{
   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object[] values = (Object[]) state;
      super.restoreState(context, values[0]);
   }

   @Override
   public Object saveState(FacesContext context)
   {
      Object[] values = new Object[1];
      values[0] = super.saveState(context);

      return values;
   }

   @Override
   public Dataset createDataset()
   {
      return new DefaultCategoryDataset();
   }

   // @Override
   // public void configurePlot(Plot p) {
   // super.configurePlot(p);
   // }

   @Override
   public void configureRenderer(CategoryItemRenderer renderer)
   {
      super.configureRenderer(renderer);
      if (renderer instanceof BarRenderer)
      {
         configureRenderer((BarRenderer) renderer);
      }
   }

   public void configureRenderer(BarRenderer renderer)
   {
   }

   @Override
   public JFreeChart createChart(FacesContext context)
   {
      JFreeChart chart;

      if (!getIs3D())
      {
         chart = ChartFactory.createBarChart(getTitle(), getDomainAxisLabel(), getRangeAxisLabel(), (CategoryDataset) dataset, plotOrientation(getOrientation()), getLegend(), false, false);
      }
      else
      {
         chart = ChartFactory.createBarChart3D(getTitle(), getDomainAxisLabel(), getRangeAxisLabel(), (CategoryDataset) dataset, plotOrientation(getOrientation()), getLegend(), false, false);
      }

      configureTitle(chart.getTitle());
      configureLegend(chart.getLegend());

      return chart;
   }
}
