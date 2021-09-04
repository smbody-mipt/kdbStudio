package studio.ui.chart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.*;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;
import studio.kdb.K;
import studio.kdb.KTableModel;
import studio.kdb.ToDouble;
import studio.ui.Util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class Chart {

    private static final Logger log = LogManager.getLogger();

    private KTableModel table;
    private ChartPanel chartPanel = null;
    private JFrame frame;
    private JPanel contentPane;
    private ChartConfigPanel pnlConfig;

    private List<Integer> yIndex;

    private final static HashSet<Class> domainKClass = new HashSet<>();
    private final static HashSet<Class> rangeKClass = new HashSet<>();
    static {
        rangeKClass.add(K.KIntVector.class);
        rangeKClass.add(K.KDoubleVector.class);
        rangeKClass.add(K.KFloatVector.class);
        rangeKClass.add(K.KShortVector.class);
        rangeKClass.add(K.KLongVector.class);
        rangeKClass.add(K.KBaseVector.class);

        domainKClass.addAll(rangeKClass);
        domainKClass.add(K.KTimespanVector.class);
        domainKClass.add(K.KDatetimeVector.class);
        domainKClass.add(K.KTimestampVector.class);
        domainKClass.add(K.KSecondVector.class);
        domainKClass.add(K.KDateVector.class);
        domainKClass.add(K.KMonthVector.class);
        domainKClass.add(K.KMinuteVector.class);
        domainKClass.add(K.KTimeVector.class);
    }

    private static Paint[] colors = DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE;
    private static Shape[] shapes = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE;

    private static StandardChartTheme currentTheme = new StandardChartTheme("JFree");
    static {
        currentTheme.setXYBarPainter(new StandardXYBarPainter());
    }

    public Paint[] getColors(int count) {
        Paint[] aColors = new Paint[count];
        for (int i=0,index=0; i<count; i++) {
            aColors[i] = colors[index++];
            if (index == colors.length) index = 0;
        }
        return aColors;
    }

    public Shape[] getShapes(int count) {
        Shape[] aShapes = new Shape[count];
        for (int i=0,index=0; i<count; i++) {
            aShapes[i] = shapes[index++];
            if (index == shapes.length) index = 0;
        }
        return aShapes;
    }

    public Shape[] getAllShapes() {
        return shapes;
    }

    public Chart(KTableModel table) {
        this.table = table;
        initComponents();
    }

    private void initComponents() {
        List<String> names = new ArrayList<>();
        List<Integer> xIndex = new ArrayList<>();
        yIndex = new ArrayList<>();
        for (int index = 0; index<table.getColumnCount(); index++) {
            names.add(table.getColumnName(index));
            Class clazz = table.getColumnClass(index);
            if (domainKClass.contains(clazz)) xIndex.add(index);
            if (rangeKClass.contains(clazz)) yIndex.add(index);
        }

        contentPane = new JPanel(new BorderLayout());
        pnlConfig = new ChartConfigPanel(this, names, xIndex, yIndex);
        contentPane.add(pnlConfig, BorderLayout.EAST);

        createPlot();

        frame = new JFrame("Studio for kdb+ [chart]");
        frame.setContentPane(contentPane);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setIconImage(Util.CHART_BIG_ICON.getImage());

        frame.pack();
        frame.setVisible(true);
        frame.requestFocus();
        frame.toFront();
    }

    void createPlot() {
        if (chartPanel !=null ) {
            contentPane.remove(chartPanel);
            chartPanel = null;
        }

        JFreeChart chart = createChart();
        if (chart != null) {
            chartPanel = new ChartPanel(createChart());
            chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
            chartPanel.setMouseWheelEnabled(true);
            chartPanel.setMouseZoomable(true, true);
            contentPane.add(chartPanel, BorderLayout.CENTER);
        }

        contentPane.revalidate();
        contentPane.repaint();
    }

    private JFreeChart createChart() {
        NumberAxis yAxis = new NumberAxis("");
        yAxis.setAutoRangeIncludesZero(false);

        XYPlot plot = new XYPlot(null, null, yAxis, null);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        ValueAxis xAxis = null;
        int datasetIndex = 0;
        for (int index = 0; index<yIndex.size(); index++) {
            if (! pnlConfig.isSeriesEnables(index)) continue;

            IntervalXYDataset dataset = getDateset(yIndex.get(index));
            boolean timeSeries = dataset instanceof TimeSeriesCollection;

            if (xAxis == null) {
                if (timeSeries) {
                    xAxis = new DateAxis("");
                    xAxis.setLowerMargin(0.02);  // reduce the default margins
                    xAxis.setUpperMargin(0.02);
                } else {
                    NumberAxis axis = new NumberAxis("");
                    axis.setAutoRangeIncludesZero(false);
                    xAxis = axis;
                }
                plot.setDomainAxis(xAxis);
            }

            XYToolTipGenerator toolTipGenerator = timeSeries ? StandardXYToolTipGenerator.getTimeSeriesInstance() :
                                                                new StandardXYToolTipGenerator();

            XYItemRenderer renderer;

            ChartType chartType = pnlConfig.getChartType(index);
            if (chartType == ChartType.BAR) {
                renderer = new BarRenderer();
            } else {
                renderer = new XYLineAndShapeRenderer(chartType.hasLine(), chartType.hasShape());
            }
            renderer.setDefaultToolTipGenerator(toolTipGenerator);
            renderer.setSeriesPaint(0, pnlConfig.getColor(index));
            renderer.setSeriesShape(0, pnlConfig.getShape(index));
            ((AbstractRenderer)renderer).setAutoPopulateSeriesPaint(false);
            ((AbstractRenderer)renderer).setAutoPopulateSeriesShape(false);

            plot.setRenderer(datasetIndex, renderer);
            plot.setDataset(datasetIndex, dataset);
            datasetIndex++;
        }
        if (xAxis == null) return null;

        JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT,
                plot, false);
        currentTheme.apply(chart);
        return chart;
    }

    private IntervalXYDataset getDateset(int col) {
        int xIndex = pnlConfig.getDomainIndex();

        Class xClazz = table.getColumnClass(xIndex);

        boolean xySeries = rangeKClass.contains(xClazz);
        if (xySeries) {
            XYSeriesCollection collection = new XYSeriesCollection();
            collection.setAutoWidth(true);
            XYSeries series = new XYSeries(table.getColumnName(col));
            for (int row = 0; row < table.getRowCount(); row++) {
                K.KBase xValue = (K.KBase)table.getValueAt(row, xIndex);
                K.KBase yValue = (K.KBase)table.getValueAt(row, col);
                if (xValue.isNull() || yValue.isNull()) continue;

                double x = ((ToDouble)xValue).toDouble();
                double y = ((ToDouble)yValue).toDouble();
                series.add(x, y);
            }
            collection.addSeries(series);
            return collection;
        } else {
            TimeSeriesCollection collection = new TimeSeriesCollection();
            TimeSeries series = new TimeSeries(table.getColumnName(col));
            for (int row = 0; row < table.getRowCount(); row++) {
                K.KBase xValue = (K.KBase) table.getValueAt(row, xIndex);
                if (xValue.isNull()) continue;

                RegularTimePeriod period = null;
                //@TODO: need to extrace this logic into K.KBase to avoid such sort of switch
                if (xValue instanceof K.KDate) {
                    K.KDate date = (K.KDate) xValue;
                    period = new Day(date.toDate());
                } else if (xValue instanceof K.KTime) {
                    K.KTime time = (K.KTime) xValue;
                    period = new Millisecond(time.toTime());
                } else if (xValue instanceof K.KTimestamp) {
                    K.KTimestamp timestamp = (K.KTimestamp) xValue;
                    period = new Millisecond(timestamp.toTimestamp());
                } else if (xValue instanceof K.KTimespan) {
                    K.KTimespan timespan = (K.KTimespan) xValue;
                    period = new Millisecond(timespan.toTime());
                } else if (xValue instanceof K.KDatetime) {
                    K.KDatetime datetime = (K.KDatetime) xValue;
                    period = new Millisecond(datetime.toTimestamp());
                } else if (xValue instanceof K.Month) {
                    K.Month month = (K.Month) xValue;
                    int m = month.toInt() + 24000;
                    int y = m / 12;
                    m = 1 + m % 12;
                    period = new Month(m, y);
                } else if (xValue instanceof K.Second) {
                    K.Second second = (K.Second) xValue;
                    int value = second.toInt();
                    int s = value % 60;
                    int m = value / 60 % 60;
                    int h = value / 3600;
                    period = new Second(s, m, h, 1, 1, 2001);
                } else if (xValue instanceof K.Minute) {
                    K.Minute minute = (K.Minute) xValue;
                    period = new Minute(minute.toInt() % 60, minute.toInt() / 60, 1, 1, 2001);
                } else {
                    throw new IllegalStateException("Unexpected class: " + xValue.getClass());
                }

                K.KBase value = (K.KBase) table.getValueAt(row, col);
                if (value.isNull()) continue;

                series.addOrUpdate(period, ((ToDouble)value).toDouble());
            }
            collection.addSeries(series);

            return collection;
        }
    }
}
