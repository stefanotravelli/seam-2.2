package org.jboss.seam.pdf.ui;

import javax.faces.context.FacesContext;

import org.jboss.seam.log.*;

import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;

public abstract class UICategoryChartBase extends UIChart {
    private static Log log = Logging.getLog(UICategoryChartBase.class);

    private String orientation;

    private String titleBackgroundPaint;
    private String titlePaint;

    private String legendBackgroundPaint;
    private String legendItemPaint;
    private String legendOutlinePaint;

    private String domainAxisLabel;
    private String domainLabelPosition;
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

    public String getDomainLabelPosition() {
        return (String) valueBinding("domainLabelPosition", domainLabelPosition);
    }

    public void setDomainLabelPosition(String domainLabelPosition) {
        this.domainLabelPosition = domainLabelPosition;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getOrientation() {
        return (String) valueBinding("orientation", orientation);
    }

    public void setTitleBackgroundPaint(String titleBackgroundPaint) {
        this.titleBackgroundPaint = titleBackgroundPaint;
    }

    public String getTitleBackgroundPaint() {
        return (String) valueBinding("titleBackgroundPaint",
                titleBackgroundPaint);
    }

    public void setTitlePaint(String titlePaint) {
        this.titlePaint = titlePaint;
    }

    public String getTitlePaint() {
        return (String) valueBinding("titlePaint", titlePaint);
    }

    public String getLegendBackgroundPaint() {
        return (String) valueBinding("legendBackgroundPaint",
                legendBackgroundPaint);
    }

    public void setLegendBackgroundPaint(String legendBackgroundPaint) {
        this.legendBackgroundPaint = legendBackgroundPaint;
    }

    public String getLegendItemPaint() {
        return (String) valueBinding("legendItemPaint", legendItemPaint);
    }

    public void setLegendItemPaint(String legendItemPaint) {
        this.legendItemPaint = legendItemPaint;
    }

    public String getLegendOutlinePaint() {
        return (String) valueBinding("legendOutlinePaint", legendOutlinePaint);
    }

    public void setLegendOutlinePaint(String legendOutlinePaint) {
        this.legendOutlinePaint = legendOutlinePaint;
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

        orientation = (String) values[1];
        //legend = (Boolean) values[2];
        //is3D = (Boolean) values[3];
        titleBackgroundPaint = (String) values[5];
        titlePaint = (String) values[6];
        legendBackgroundPaint = (String) values[7];
        legendItemPaint = (String) values[8];
        legendOutlinePaint = (String) values[9];
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
        domainLabelPosition = (String) values[20];
    }

    @Override
    public Object saveState(FacesContext context) {
        Object[] values = new Object[21];
        values[0] = super.saveState(context);
        values[1] = orientation;
        //values[2] = legend;
        //values[3] = is3D;
        //values[4] = title;
        values[5] = titleBackgroundPaint;
        values[6] = titlePaint;
        values[7] = legendBackgroundPaint;
        values[8] = legendItemPaint;
        values[9] = legendOutlinePaint;
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
        values[20] = domainLabelPosition;

        return values;
    }

    @Override
    public void configurePlot(Plot plot) {
        super.configurePlot(plot);
        
        if (plot instanceof CategoryPlot) {
            configurePlot((CategoryPlot) plot);
        } else {
            log.error("UICATEGORYCHART --- unknown plot " + plot);
        }
    }

    public void configurePlot(CategoryPlot plot) {
        // plot.setAxisOffset(RectangleInsets)
        // plot.setDomainAxisLocation(arg0);
        // plot.setRangeAxisLocation(arg0);

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

        if (getDomainLabelPosition() != null) {
            CategoryLabelPositions positions = categoryLabelPosition(getDomainLabelPosition());
            plot.getDomainAxis().setCategoryLabelPositions(positions);  
            
        }
        
        configureRenderer(plot.getRenderer());
    }

    private CategoryLabelPositions categoryLabelPosition(String position) {
        if (position == null) {
            return CategoryLabelPositions.STANDARD;
        } else if (position.equals("UP_45")) {
            return CategoryLabelPositions.UP_45;
        } else if (position.equals("UP_90")) {
            return CategoryLabelPositions.UP_90;
        } else if (position.equals("DOWN_45")) {
            return CategoryLabelPositions.DOWN_45;
        } else if (position.equals("DOWN_90")) {
            return CategoryLabelPositions.DOWN_90;
        }
      
        double angle = Double.parseDouble(position);
        if (angle>0) {
            return CategoryLabelPositions.createUpRotationLabelPositions(angle);
        } else {
            return CategoryLabelPositions.createDownRotationLabelPositions(-angle);
        }
    }

    public void configureRenderer(CategoryItemRenderer renderer) {
        // renderer.setItemMargin(0.0);

        // renderer.setBase(arg0);
        // renderer.setBaseFillPaint(arg0);
        // renderer.setBaseItemLabelFont(arg0);
        // renderer.setBaseItemLabelPaint(arg0);
        // renderer.setBaseItemLabelsVisible(arg0);
        // renderer.setBaseOutlinePaint(arg0);
        // renderer.setBaseOutlineStroke(arg0);
        // renderer.setBaseSeriesVisible(arg0);
        // renderer.setBaseSeriesVisibleInLegend(arg0);
        // renderer.setBaseShape(arg0);
        // renderer.setBaseStroke();
        // renderer.setFillPaint(arg0);
        // renderer.setItemLabelFont(arg0);
        // renderer.setItemLabelPaint(arg0);
        // renderer.setItemLabelsVisible(arg0);
        // renderer.setItemMargin(arg0);
        // renderer.setOutlinePaint(arg0)
        // renderer.setOutlineStroke(arg0)
        // renderer.setPaint(arg0);
        // renderer.setStroke(arg0);

        // renderer.setBaseOutlineStroke(new BasicStroke(2f,
        // BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10f,
        // new float[] {10,3}, 0));
    }

    public void configureTitle(TextTitle chartTitle) {
        if (chartTitle != null) {
            if (findColor(getTitleBackgroundPaint()) != null) {
                chartTitle.setBackgroundPaint(findColor(getTitleBackgroundPaint()));
            }

            if (findColor(getTitlePaint()) != null) {
                chartTitle.setPaint(findColor(getTitlePaint()));
            }
        }
    }

    void configureLegend(LegendTitle chartLegend) {
        if (chartLegend != null) {
            if (findColor(getLegendBackgroundPaint()) != null) {
                chartLegend.setBackgroundPaint(findColor(getLegendBackgroundPaint()));
            }

            if (findColor(getLegendOutlinePaint()) != null) {
                chartLegend.setFrame(new BlockBorder(findColor(getLegendOutlinePaint())));
            }

            if (findColor(getLegendItemPaint()) != null) {
                chartLegend.setItemPaint(findColor(getLegendItemPaint()));
            }
        }
    }

}
