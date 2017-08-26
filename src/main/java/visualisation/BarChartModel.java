package visualisation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
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
	private String _series1;
	private int _scheduleCounter;

	/**
	 * A custom renderer that returns a different color for each item in a single series.
	 */
	class CustomRenderer extends BarRenderer {

		/** The colors. */
		private Paint[] colors;

		/**
		 * Creates a new renderer with two colors and other specified properties.
		 *
		 * @param colors  the colors.
		 */
		public CustomRenderer() {
			this.colors = new Paint[] {Color.ORANGE, new Color(128,0,128)};
			setShadowVisible(false);
			this.setBaseFillPaint(Color.BLACK);
	
		}

		/**
		 * Returns the paint for an item.  If last bar then, we color it red;
		 * otherwise we switch between two colors.
		 *
		 * @param row  the series.
		 * @param column  the category.
		 *
		 * @return The item color.
		 */
		public Paint getItemPaint(final int row, final int column) {
			if (column == _dataset.getColumnCount()-1){
				return Color.red;
			} else {
				return this.colors[column % this.colors.length];
			}
		}
	}

	public BarChartModel() {

		_dataset = createDataset();
		final JFreeChart chart = createChart(_dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);

		chartPanel.setPreferredSize(new Dimension(630, 435));
		this.add(chartPanel);

	}

	/**
	 * Returns a dataset with 2 series (different colours)
	 * 
	 * @return The dataset.
	 */
	private DefaultCategoryDataset createDataset() {

		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		// keys for the rows
		_series1 = "First";

		return dataset;

	}
	/**
	 * Add a data value (double) to the bar graph.
	 * 
	 * @param value
	 */
	public void addDataToSeries(double value){
		final String category = "S " + _scheduleCounter++;
		_dataset.addValue(value, _series1, category);

	}

	/**
	 * Creates a sample chart and defines the properties of the chart
	 * 
	 * @param dataset  the dataset.
	 * 
	 * @return A plot of the barchart with specified properties.
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

		// set the background color of chart
		chart.setBackgroundPaint(Color.white);

		// setting grid and background of grid
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		
		// configure renderer to switch between two colors
		final CategoryItemRenderer renderer = new CustomRenderer(
				);
		
		//renderer.drawOutline(arg0, arg1, arg2);

		plot.setRenderer(renderer);
		
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setUpperMargin(0.15);

		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		
		
        
		return chart;

	}

}      