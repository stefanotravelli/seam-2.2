package org.jboss.seam.pdf.ui;

import javax.faces.context.FacesContext;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;

public class UIPieChart 
    extends UIChart 
{
    private String label;

    private Double labelLinkMargin;
    private String labelLinkPaint;
    private String labelLinkStroke;
    private Boolean labelLinksVisible;
    private String labelOutlinePaint;
    private String labelOutlineStroke;
    private String labelShadowPaint;
    private String labelPaint;
    private Double labelGap;
    private String labelBackgroundPaint;
    private Double startAngle;
    private Boolean circular;
    private String direction;
    private String sectionOutlinePaint;
    private String sectionOutlineStroke;
    private Boolean sectionOutlinesVisible;
    private String baseSectionOutlinePaint;
    private String baseSectionPaint;
    private String baseSectionOutlineStroke;

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return (String) valueBinding("label", label);
    }


    public String getBaseSectionOutlinePaint() {
        return (String) valueBinding("baseSectionOutlinePaint",
                baseSectionOutlinePaint);
    }

    public void setBaseSectionOutlinePaint(String baseSectionOutlinePaint) {
        this.baseSectionOutlinePaint = baseSectionOutlinePaint;
    }

    public String getBaseSectionOutlineStroke() {
        return (String) valueBinding("baseSectionOutlineStroke",
                baseSectionOutlineStroke);
    }

    public void setBaseSectionOutlineStroke(String baseSectionOutlineStroke) {
        this.baseSectionOutlineStroke = baseSectionOutlineStroke;
    }

    public String getBaseSectionPaint() {
        return (String) valueBinding("baseSectionPaint", baseSectionPaint);
    }

    public void setBaseSectionPaint(String baseSectionPaint) {
        this.baseSectionPaint = baseSectionPaint;
    }

    public Boolean getCircular() {
        return (Boolean) valueBinding("isCircular", circular);
    }

    public void setCircular(Boolean circular) {
        this.circular = circular;
    }

    public String getLabelBackgroundPaint() {
        return (String) valueBinding("labelBackgroundPaint",
                labelBackgroundPaint);
    }

    public void setLabelBackgroundPaint(String labelBackgroundPaint) {
        this.labelBackgroundPaint = labelBackgroundPaint;
    }

    public Double getLabelGap() {
        return (Double) valueBinding("labelGap", labelGap);
    }

    public void setLabelGap(Double labelGap) {
        this.labelGap = labelGap;
    }

    public Double getLabelLinkMargin() {
        return (Double) valueBinding("labelLinkMargin", labelLinkMargin);
    }

    public void setLabelLinkMargin(double labelLinkMargin) {
        this.labelLinkMargin = labelLinkMargin;
    }

    public String getLabelLinkPaint() {
        return (String) valueBinding("labelLinkPaint", labelLinkPaint);
    }

    public void setLabelLinkPaint(String labelLinkPaint) {
        this.labelLinkPaint = labelLinkPaint;
    }

    public String getLabelLinkStroke() {
        return (String) valueBinding("labelLinkStroke", labelLinkStroke);
    }

    public void setLabelLinkStroke(String labelLinkStroke) {
        this.labelLinkStroke = labelLinkStroke;
    }

    public Boolean isLabelLinksVisible() {
        return (Boolean) valueBinding("labelLinksVisible", labelLinksVisible);
    }

    public void setLabelLinksVisible(Boolean labelLinksVisible) {
        this.labelLinksVisible = labelLinksVisible;
    }

    public String getLabelOutlinePaint() {
        return (String) valueBinding("labelOutlinePaint", labelOutlinePaint);
    }

    public void setLabelOutlinePaint(String labelOutlinePaint) {
        this.labelOutlinePaint = labelOutlinePaint;
    }

    public String getLabelOutlineStroke() {
        return (String) valueBinding("labelOutlineStroke", labelOutlineStroke);
    }

    public void setLabelOutlineStroke(String labelOutlineStroke) {
        this.labelOutlineStroke = labelOutlineStroke;
    }

    public String getLabelPaint() {
        return (String) valueBinding("labelPaint", labelPaint);
    }

    public void setLabelPaint(String labelPaint) {
        this.labelPaint = labelPaint;
    }

    public String getLabelShadowPaint() {
        return (String) valueBinding("labelShadowPaint", labelShadowPaint);
    }

    public void setLabelShadowPaint(String labelShadowPaint) {
        this.labelShadowPaint = labelShadowPaint;
    }

    public String getDirection() {
        return (String) valueBinding("rotation", direction);
    }

    public void setDirection(String rotation) {
        this.direction = rotation;
    }

    public String getSectionOutlinePaint() {
        return (String) valueBinding("sectionOutlinePaint", sectionOutlinePaint);
    }

    public void setSectionOutlinePaint(String sectionOutlinePaint) {
        this.sectionOutlinePaint = sectionOutlinePaint;
    }

    public String getSectionOutlineStroke() {
        return (String) valueBinding("sectionOutlineStroke",
                sectionOutlineStroke);
    }

    public void setSectionOutlineStroke(String sectionOutlineStroke) {
        this.sectionOutlineStroke = sectionOutlineStroke;
    }

    public Boolean isSectionOutlinesVisible() {
        return (Boolean) valueBinding("sectionOutlineVisible",
                sectionOutlinesVisible);
    }

    public void setSectionOutlinesVisible(Boolean sectionOutlinesVisible) {
        this.sectionOutlinesVisible = sectionOutlinesVisible;
    }

    public Double getStartAngle() {
        return (Double) valueBinding("startAngle", startAngle);
    }

    public void setStartAngle(Double startAngle) {
        this.startAngle = startAngle;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);

        // title = (String) values[1];
        label = (String) values[2];
        //legend = (Boolean) values[3];
        //is3D = (Boolean) values[4];
        labelLinkMargin = (Double) values[5];
        labelLinkPaint = (String) values[6];
        labelLinkStroke = (String) values[7];
        labelLinksVisible = (Boolean) values[8];
        labelOutlinePaint = (String) values[9];
        labelOutlineStroke = (String) values[10];
        labelShadowPaint = (String) values[11];
        labelPaint = (String) values[12];
        labelGap = (Double) values[13];
        labelBackgroundPaint = (String) values[14];
        startAngle = (Double) values[15];
        circular = (Boolean) values[16];
        direction = (String) values[17];
        sectionOutlinePaint = (String) values[18];
        sectionOutlineStroke = (String) values[19];
        sectionOutlinesVisible = (Boolean) values[20];
        baseSectionOutlinePaint = (String) values[21];
        baseSectionPaint = (String) values[22];
        baseSectionOutlineStroke = (String) values[23];
    }

    @Override
    public Object saveState(FacesContext context) {
        Object[] values = new Object[24];

        values[0] = super.saveState(context);
        // values[1] = title;
        values[2] = label;
        //values[3] = legend;
        //values[4] = is3D;
        values[5] = labelLinkMargin;
        values[6] = labelLinkPaint;
        values[7] = labelLinkStroke;
        values[8] = labelLinksVisible;
        values[9] = labelOutlinePaint;
        values[10] = labelOutlineStroke;
        values[11] = labelShadowPaint;
        values[12] = labelPaint;
        values[13] = labelGap;
        values[14] = labelBackgroundPaint;
        values[15] = startAngle;
        values[16] = circular;
        values[17] = direction;
        values[18] = sectionOutlinePaint;
        values[19] = sectionOutlineStroke;
        values[20] = sectionOutlinesVisible;
        values[21] = baseSectionOutlinePaint;
        values[22] = baseSectionPaint;
        values[23] = baseSectionOutlineStroke;

        return values;
    }

    @Override
    public Dataset createDataset() {
        return new DefaultPieDataset();
    }

    public Rotation rotationValue(String value) {
        if (value == null || value.equalsIgnoreCase("anticlockwise")) {
            return Rotation.ANTICLOCKWISE;
        } else {
            return Rotation.CLOCKWISE;
        }
    }

    @Override
    public void configurePlot(Plot plot) {
        super.configurePlot(plot);

        if (plot instanceof PiePlot) {
            PiePlot pieplot = (PiePlot) plot;
            if (label != null) {
                pieplot.setLabelGenerator(new StandardPieSectionLabelGenerator(label));
            }

            if (baseSectionOutlinePaint != null) {
                pieplot.setBaseSectionOutlinePaint(findColor(baseSectionOutlinePaint));
            }
            if (baseSectionOutlineStroke != null) {
                pieplot.setBaseSectionOutlineStroke(findStroke(baseSectionOutlineStroke));
            }
            if (baseSectionPaint != null) {
                pieplot.setBaseSectionPaint(findColor(baseSectionPaint));
            }

            if (circular != null) {
                pieplot.setCircular(circular);
            }
            if (startAngle != null) {
                pieplot.setStartAngle(startAngle);
            }
            if (direction != null) {
                pieplot.setDirection(rotationValue(direction));
            }

            if (sectionOutlinePaint != null) {
                pieplot
                        .setBaseSectionOutlinePaint(findColor(sectionOutlinePaint));
            }
            if (sectionOutlineStroke != null) {
                pieplot
                        .setBaseSectionOutlineStroke(findStroke(sectionOutlineStroke));
            }
            if (sectionOutlinesVisible != null) {
                pieplot.setSectionOutlinesVisible(sectionOutlinesVisible);
            }

            // pieplot.setLabelFont(arg0);
            if (labelBackgroundPaint != null) {
                pieplot.setLabelBackgroundPaint(findColor(labelBackgroundPaint));
            }
            if (labelGap != null) {
                pieplot.setLabelGap(labelGap);
            }
            if (labelLinkMargin != null) {
                pieplot.setLabelLinkMargin(labelLinkMargin);
            }
            if (labelLinkPaint != null) {
                pieplot.setLabelLinkPaint(findColor(labelLinkPaint));
            }
            if (labelLinkStroke != null) {
                pieplot.setLabelLinkStroke(findStroke(labelLinkStroke));
            }
            if (labelLinksVisible != null) {
                pieplot.setLabelLinksVisible(labelLinksVisible);
            }
            if (labelOutlinePaint != null) {
                pieplot.setLabelOutlinePaint(findColor(labelOutlinePaint));
            }
            if (labelOutlineStroke != null) {
                pieplot.setLabelOutlineStroke(findStroke(labelOutlineStroke));
            }
            if (labelPaint != null) {
                pieplot.setLabelPaint(findColor(labelPaint));
            }
            if (labelShadowPaint != null) {
                pieplot.setLabelShadowPaint(findColor(labelShadowPaint));
            }
        }
    }

    @Override
    public JFreeChart createChart(FacesContext context) {

        if (!getIs3D()) {
            return ChartFactory.createPieChart(getTitle(), (PieDataset) dataset, getLegend(), false, false);
        } else {
            return ChartFactory.createPieChart3D(getTitle(), (PieDataset) dataset, getLegend(), false, false);
        }
    }
}
