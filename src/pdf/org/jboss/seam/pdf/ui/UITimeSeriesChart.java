package org.jboss.seam.pdf.ui;

import javax.faces.context.FacesContext;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

public class UITimeSeriesChart 
    extends UIChart 
{
    private String domainAxisLabel;
    private String domainAxisPaint;
    private Boolean domainGridlinesVisible;
    private String domainGridlinePaint;
    private String domainGridlineStroke;

    private String rangeAxisLabel;
    private String rangeAxisPaint;
    private Boolean rangeGridlinesVisible;
    private String rangeGridlinePaint;
    private String rangeGridlineStroke;
    
    public String getDomainAxisLabel() {
        return (String) valueBinding("domainAxisLabel", domainAxisLabel);
    }

    public void setDomainAxisLabel(String categoryAxisLabel) {
        this.domainAxisLabel = categoryAxisLabel;
    }

    public String getRangeAxisLabel() {
        return (String) valueBinding("rangeAxisLabel", rangeAxisLabel);
    }

    public void setRangeAxisLabel(String valueAxisLabel) {
        this.rangeAxisLabel = valueAxisLabel;
    }
    
    public String getDomainGridlinePaint() {
        return (String) valueBinding("domainGridlinePaint", domainGridlinePaint);
    }

    public void setDomainGridlinePaint(String domainGridlinePaint) {
        this.domainGridlinePaint = domainGridlinePaint;
    }

    public String getDomainGridlineStroke() {
        return (String) valueBinding("domainGridlineStroke",
                domainGridlineStroke);
    }

    public void setDomainGridlineStroke(String domainGridlineStroke) {
        this.domainGridlineStroke = domainGridlineStroke;
    }

    public Boolean getDomainGridlinesVisible() {
        return (Boolean) valueBinding("domainGridlinesVisible",
                domainGridlinesVisible);
    }

    public void setDomainGridlinesVisible(Boolean domainGridlinesVisible) {
        this.domainGridlinesVisible = domainGridlinesVisible;
    }

    public String getRangeGridlinePaint() {
        return (String) valueBinding("rangeGridlinePaint", rangeGridlinePaint);
    }

    public void setRangeGridlinePaint(String rangeGridlinePaint) {
        this.rangeGridlinePaint = rangeGridlinePaint;
    }

    public String getRangeGridlineStroke() {
        return (String) valueBinding("rangeGridlineStroke", rangeGridlineStroke);
    }

    public void setRangeGridlineStroke(String rangeGridlineStroke) {
        this.rangeGridlineStroke = rangeGridlineStroke;
    }

    public Boolean getRangeGridlinesVisible() {
        return (Boolean) valueBinding("rangeGridlinesVisible",
                rangeGridlinesVisible);
    }

    public void setRangeGridlinesVisible(Boolean rangeGridlinesVisible) {
        this.rangeGridlinesVisible = rangeGridlinesVisible;
    }

    public String getDomainAxisPaint() {
        return (String) valueBinding("domainAxisPaint", domainAxisPaint);
    }

    public void setDomainAxisPaint(String domainAxisPaint) {
        this.domainAxisPaint = domainAxisPaint;
    }

    public String getRangeAxisPaint() {
        return (String) valueBinding("rangeAxisPaint", rangeAxisPaint);
    }

    public void setRangeAxisPaint(String rangeAxisPaint) {
        this.rangeAxisPaint = rangeAxisPaint;
    }

    
    @Override
    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;

        super.restoreState(context, values[0]);

        //orientation = (String) values[1];
        //legend = (Boolean) values[2];
        //is3D = (Boolean) values[3];
//        titleBackgroundPaint = (String) values[5];
//        titlePaint = (String) values[6];
//        legendBackgroundPaint = (String) values[7];
//        legendItemPaint = (String) values[8];
//        legendOutlinePaint = (String) values[9];
        domainAxisLabel = (String) values[10];
        domainAxisPaint = (String) values[11];
        domainGridlinesVisible = (Boolean) values[12];
        domainGridlinePaint = (String) values[13];
        domainGridlineStroke = (String) values[14];
        rangeAxisLabel = (String) values[15];
        rangeAxisPaint = (String) values[16];
        rangeGridlinesVisible = (Boolean) values[17];
        rangeGridlinePaint = (String) values[18];
        rangeGridlineStroke = (String) values[19];
        //domainLabelPosition = (String) values[20];
    }

    @Override
    public Object saveState(FacesContext context) {
        Object[] values = new Object[21];
        values[0] = super.saveState(context);
        //values[1] = orientation;
        //values[2] = legend;
        //values[3] = is3D;
        //values[4] = title;
//        values[5] = titleBackgroundPaint;
//        values[6] = titlePaint;
//        values[7] = legendBackgroundPaint;
//        values[8] = legendItemPaint;
//        values[9] = legendOutlinePaint;
        values[10] = domainAxisLabel;
        values[11] = domainAxisPaint;
        values[12] = domainGridlinesVisible;
        values[13] = domainGridlinePaint;
        values[14] = domainGridlineStroke;
        values[15] = rangeAxisLabel;
        values[16] = rangeAxisPaint;
        values[17] = rangeGridlinesVisible;
        values[18] = rangeGridlinePaint;
        values[19] = rangeGridlineStroke;
        //values[20] = domainLabelPosition;

        return values;
    }
    @Override
    public JFreeChart createChart(FacesContext context)
    {
          return ChartFactory.createTimeSeriesChart(getTitle(), 
                  getDomainAxisLabel(), 
                  getRangeAxisLabel(), 
                  (XYDataset) getDataset(), 
                  true, false, false);
    }

   @Override
    public void configurePlot(Plot plot) {
        super.configurePlot(plot);
        
        if (plot instanceof XYPlot) {
            configureXYPlot((XYPlot) plot);
        }  
   }
   
   public void configureXYPlot(XYPlot plot) {

        if (getDomainGridlinesVisible() != null) {
            plot.setDomainGridlinesVisible(getDomainGridlinesVisible());
        }

        if (findColor(getDomainGridlinePaint()) != null) {
            plot.setDomainGridlinePaint(findColor(getDomainGridlinePaint()));
        }
        
        if (findStroke(getDomainGridlineStroke()) != null) {
            plot.setDomainGridlineStroke(findStroke(getDomainGridlineStroke()));
        }
        
        if (findColor(getDomainAxisPaint()) != null) {
            plot.getDomainAxis().setLabelPaint(findColor(getDomainAxisPaint()));
        }

        if (getRangeGridlinesVisible() != null) {
            plot.setRangeGridlinesVisible(getRangeGridlinesVisible());
        }
        
        if (findColor(getRangeGridlinePaint()) != null) {
            plot.setRangeGridlinePaint(findColor(getRangeGridlinePaint()));
        }
        
        if (findStroke(getRangeGridlineStroke()) != null) {
            plot.setRangeGridlineStroke(findStroke(getRangeGridlineStroke()));
        }
        
        if (findColor(getRangeAxisPaint()) != null) {
            plot.getRangeAxis().setLabelPaint(findColor(getRangeAxisPaint()));
        }
    } 
   
    
    @Override
    public Dataset createDataset() {
        return new DefaultXYDataset();
    }

}
