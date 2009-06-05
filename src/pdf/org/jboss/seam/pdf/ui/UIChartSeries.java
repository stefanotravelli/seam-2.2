package org.jboss.seam.pdf.ui;

import java.io.IOException;

import javax.faces.context.FacesContext;

import org.jboss.seam.log.*;

import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;

public class UIChartSeries extends ITextComponent {
    private static Log log = Logging.getLog(UIChartSeries.class);

    private String key;
    private String seriesPaint;
    private String seriesFillPaint;
    private String seriesOutlinePaint;
    private String seriesOutlineStroke;
    private String seriesStroke;
    private Boolean seriesVisible;
    private Boolean seriesVisibleInLegend;

    public String getKey() {
        return (String) valueBinding("key", key);
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSeriesPaint() {
        return (String) valueBinding("seriesPaint", seriesPaint);
    }

    public void setSeriesPaint(String seriesPaint) {
        this.seriesPaint = seriesPaint;
    }

    public String getSeriesFillPaint() {
        return (String) valueBinding("seriesFillPaint", seriesFillPaint);
    }

    public void setSeriesFillPaint(String seriesFillPaint) {
        this.seriesFillPaint = seriesFillPaint;
    }

    public String getSeriesOutlinePaint() {
        return (String) valueBinding("seriesOutlinePaint", seriesOutlinePaint);
    }

    public void setSeriesOutlinePaint(String seriesOutlinePaint) {
        this.seriesOutlinePaint = seriesOutlinePaint;
    }

    public String getSeriesOutlineStroke() {
        return (String) valueBinding("seriesOutlineStroke", seriesOutlineStroke);
    }

    public void setSeriesOutlineStroke(String seriesOutlineStroke) {
        this.seriesOutlineStroke = seriesOutlineStroke;
    }

    public String getSeriesStroke() {
        return (String) valueBinding("seriesStroke", seriesStroke);
    }

    public void setSeriesStroke(String seriesStroke) {
        this.seriesStroke = seriesStroke;
    }

    public Boolean getSeriesVisible() {
        return (Boolean) valueBinding("seriesVisible", seriesVisible);
    }

    public void setSeriesVisible(Boolean seriesVisible) {
        this.seriesVisible = seriesVisible;
    }

    public Boolean getSeriesVisibleInLegend() {
        return (Boolean) valueBinding("seriesVisibleInLegend",
                seriesVisibleInLegend);
    }

    public void setSeriesVisibleInLegend(Boolean seriesVisibleInLegend) {
        this.seriesVisibleInLegend = seriesVisibleInLegend;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);

        key = (String) values[1];
        seriesPaint = (String) values[2];
        seriesFillPaint = (String) values[3];
        seriesOutlinePaint = (String) values[4];
        seriesOutlineStroke = (String) values[5];
        seriesStroke = (String) values[6];
        seriesVisible = (Boolean) values[7];
        seriesVisibleInLegend = (Boolean) values[8];
    }

    @Override
    public Object saveState(FacesContext context) {
        Object[] values = new Object[9];

        values[0] = super.saveState(context);
        values[1] = key;
        values[2] = seriesPaint;
        values[3] = seriesFillPaint;
        values[4] = seriesOutlinePaint;
        values[5] = seriesOutlineStroke;
        values[6] = seriesStroke;
        values[7] = seriesVisible;
        values[8] = seriesVisibleInLegend;

        return values;
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        UIChart chart = (UIChart) findITextParent(getParent(), UIChart.class);

        if (chart != null) {
            Dataset dataset = chart.getDataset();
            Plot p = chart.getChart().getPlot();

            if (p instanceof CategoryPlot) {
                CategoryPlot plot = (CategoryPlot) p;

                int seriesIndex = ((CategoryDataset) dataset).getRowIndex(getKey());
                CategoryItemRenderer renderer = plot.getRenderer();

                // CategoryRenderer
                if (renderer instanceof AbstractRenderer) {
                    configureSeries((AbstractRenderer) renderer, seriesIndex);
                } else {
                    log.error("render is not AbtractRenderer" + renderer);
                }
                
            }
//             else if (p instanceof XYPlot) {
//                /// ??? 
//            }
        }
    }

    private void configureSeries(AbstractRenderer renderer, int seriesIndex) {
        if (getSeriesPaint() != null) {
            renderer.setSeriesPaint(seriesIndex, UIChart.findColor(getSeriesPaint()));
        }

        if (getSeriesFillPaint() != null) {
            renderer.setSeriesFillPaint(seriesIndex, UIChart.findColor(getSeriesFillPaint()));
        }

        if (getSeriesOutlinePaint() != null) {
            renderer.setSeriesOutlinePaint(seriesIndex, UIChart.findColor(getSeriesOutlinePaint()));
        }

        if (getSeriesOutlineStroke() != null) {
            renderer.setSeriesOutlineStroke(seriesIndex, UIChart.findStroke(getSeriesOutlineStroke()));
        }

        if (getSeriesStroke() != null) {
            renderer.setSeriesStroke(seriesIndex, UIChart.findStroke(getSeriesStroke()));
        }

        if (getSeriesVisible() != null) {
            renderer.setSeriesVisible(seriesIndex, getSeriesVisible());
        }

        if (getSeriesVisibleInLegend() != null) {
            renderer.setSeriesVisibleInLegend(seriesIndex, getSeriesVisibleInLegend());
        }
        
        renderer.setSeriesItemLabelsVisible(seriesIndex, true);
    }

    @Override
    public void createITextObject(FacesContext context) {
    }

    @Override
    public Object getITextObject() {
        return null;
    }

    @Override
    public void removeITextObject() {
    }

    @Override
    public void handleAdd(Object other) {
    }
}
