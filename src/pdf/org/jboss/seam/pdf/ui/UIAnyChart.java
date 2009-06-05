package org.jboss.seam.pdf.ui;

import javax.faces.context.FacesContext;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

public class UIAnyChart 
    extends UIChart
{

    @Override
    public JFreeChart createChart(FacesContext context)
    {
        throw new RuntimeException("the chart tag requires a chart");
    }

    @Override
    public Dataset createDataset()
    {
        return null;
    }

}
