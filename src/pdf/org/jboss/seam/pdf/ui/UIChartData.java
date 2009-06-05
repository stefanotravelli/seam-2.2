package org.jboss.seam.pdf.ui;

import java.io.IOException;
import java.math.BigDecimal;

import javax.faces.context.FacesContext;

import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;

public class UIChartData extends ITextComponent {
    private String key;
    private String series;
    private Object value;
    private Float explodedPercent;

    private String sectionPaint;
    private String sectionOutlinePaint;
    private String sectionOutlineStroke;

    public Object getValue() {
        return valueBinding("value", value);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Number getNumericValue() {
        Object val = getValue();
        if (val instanceof Number) {
            return (Number) getValue();
        } else if (val instanceof String) {
            return new BigDecimal((String) val);
        } else {
            throw new RuntimeException("Can't convert "
                    + val.getClass().getName() + " to numeric value");
        }
    }

    public String getKey() {
        return (String) valueBinding("key", key);
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSeries() {
        String value = (String) valueBinding("series", series);
        if (value == null) {
            UIChartSeries series = (UIChartSeries) findITextParent(this,
                    UIChartSeries.class);
            value = series.getKey();
        }
        return value;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Float getExplodedPercent() {
        return (Float) valueBinding("explodedPercent", explodedPercent);
    }

    public void setExplodedPercent(Float explodedPercent) {
        this.explodedPercent = explodedPercent;
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

    public String getSectionPaint() {
        return (String) valueBinding("sectionPaint", sectionPaint);
    }

    public void setSectionPaint(String sectionPaint) {
        this.sectionPaint = sectionPaint;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);

        key = (String) values[1];
        // columnKey = (String) values[2];
        series = (String) values[3];
        value = values[4];
        explodedPercent = (Float) values[5];
        sectionPaint = (String) values[6];
        sectionOutlinePaint = (String) values[7];
        sectionOutlineStroke = (String) values[8];
    }

    @Override
    public Object saveState(FacesContext context) {
        Object[] values = new Object[9];

        values[0] = super.saveState(context);
        values[1] = key;
        // values[2] = columnKey;
        values[3] = series;
        values[4] = value;
        values[5] = explodedPercent;
        values[6] = sectionPaint;
        values[7] = sectionOutlinePaint;
        values[8] = sectionOutlineStroke;

        return values;
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        super.encodeEnd(context);

        UIChart chart = (UIChart) findITextParent(getParent(), UIChart.class);
        if (chart != null) {
            Dataset dataset = chart.getDataset();

            if (dataset instanceof DefaultPieDataset) {
                DefaultPieDataset piedata = (DefaultPieDataset) dataset;
                piedata.setValue(getKey(), getNumericValue());

                PiePlot plot = (PiePlot) chart.getChart().getPlot();

                if (getExplodedPercent() != null) {
                    plot.setExplodePercent(getKey(), getExplodedPercent());
                }

                if (UIChart.findColor(getSectionPaint()) != null) {
                    plot.setSectionPaint(getKey(), UIChart.findColor(getSectionPaint()));
                }

                if (UIChart.findColor(getSectionOutlinePaint()) != null) {
                    plot.setSectionOutlinePaint(getKey(), UIChart.findColor(getSectionOutlinePaint()));
                }

                if (UIChart.findStroke(getSectionOutlineStroke()) != null) {
                    plot.setSectionOutlineStroke(getKey(), UIChart.findStroke(getSectionOutlineStroke()));
                }
            } else if (dataset instanceof DefaultCategoryDataset) {
                DefaultCategoryDataset data = (DefaultCategoryDataset) dataset;

                // CategoryPlot plot = (CategoryPlot)
                // chart.getChart().getPlot();
                data.addValue(getNumericValue(), getSeries(), getKey());
//            } else if (dataset instanceof DefaultXYDataset) {
//              DefaultXYDataset data =   (DefaultXYDataset) dataset;

            } else {
                throw new RuntimeException(
                        "Cannot add data to dataset of type "
                                + dataset.getClass());
            }
        }
    }

    @Override
    public void createITextObject(FacesContext context) {

    }

    @Override
    public Object getITextObject() {
        return null;
    }

    @Override
    public void handleAdd(Object other) {

    }

    @Override
    public void removeITextObject() {

    }
}
