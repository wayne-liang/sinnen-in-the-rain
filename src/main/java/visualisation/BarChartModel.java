package visualisation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demonstration application showing how to create a bar chart with a custom item
 * label generator.
 *
 */
@SuppressWarnings("serial")
public class BarChartModel extends JPanel {
	
	private DefaultCategoryDataset _dataset;
	private String series1;
	private String series2;
	private int scheduleCounter;
	
	 /**
     * A custom renderer that returns a different color for each item in a single series.
     */
    class CustomRenderer extends BarRenderer {

        /** The colors. */
        private Paint[] colors;

        /**
         * Creates a new renderer.
         *
         * @param colors  the colors.
         */
        public CustomRenderer(final Paint[] colors) {
            this.colors = colors;
        }

        /**
         * Returns the paint for an item.  Overrides the default behaviour inherited from
         * AbstractSeriesRenderer.
         *
         * @param row  the series.
         * @param column  the category.
         *
         * @return The item color.
         */
        public Paint getItemPaint(final int row, final int column) {
            return this.colors[column % this.colors.length];
        }
    }
    
    public BarChartModel() {

    	//setLayout
        _dataset = createDataset();
        final JFreeChart chart = createChart(_dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        //chartPanel.p
        //chartPanel.s
        chartPanel.setPreferredSize(new Dimension(600, 450));
        this.add(chartPanel);
        //setContentPane(chartPanel);
    }

    /**
     * Returns a sample dataset.
     * 
     * @return The dataset.
     */
    private DefaultCategoryDataset createDataset() {
        
    	// create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // row keys...
        series1 = "First";
        series2 = "Second";
        /*final String series2 = "Second";
        final String series3 = "Third";*/

        // column keys...
        /*final String category1 = "Category 1";
        final String category2 = "Category 2";
        final String category3 = "Category 3";
        final String category4 = "Category 4";
        final String category5 = "Category 5";*/

        // create the dataset...
        //final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        //dataset.addValue(500, series1, category1);
        /*dataset.addValue(4.0, series1, category2);
        dataset.addValue(3.0, series1, category3);
        dataset.addValue(5.0, series1, category4);
        dataset.addValue(5.0, series1, category5);*/
        
        return dataset;
        
    }
    
    public void addDataToSeries(double value){
    	final String category = "Category " + scheduleCounter++;
  
    		_dataset.addValue(value, series1, category);

    }
    
    /**
     * Creates a sample chart.
     * 
     * @param dataset  the dataset.
     * 
     * @return The chart.
     */
    private JFreeChart createChart(final CategoryDataset dataset) {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
            "Improvements in Schedules",       // chart title
            "Schedules",                   // domain axis label
            "Best Time",                  // range axis label
            dataset,                  // data
            PlotOrientation.VERTICAL, // orientation
            false,                    // include legend
            true,                     // tooltips?
            false                     // URLs?
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customization...
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        final CategoryItemRenderer renderer = new CustomRenderer(
                new Paint[] {Color.red, Color.blue}
            );

        plot.setRenderer(renderer);
        renderer.setSeriesItemLabelsVisible(0, Boolean.TRUE);
        // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperMargin(0.15);
        
        // disable bar outlines...
        /*final CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesItemLabelsVisible(0, Boolean.TRUE);*/
        
        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        // OPTIONAL CUSTOMISATION COMPLETED.
        
        return chart;
        
    }

}      