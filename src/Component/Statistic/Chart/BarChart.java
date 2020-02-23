package Component.Statistic.Chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by snowf on 2019/11/19.
 */

public class BarChart implements ChartInterface {
    public String XLabel = "";
    public String YLabel = "";
    public String FigureTitle = "";
    public ArrayList<String> XSample = new ArrayList<>();
    public String[] Classification = new String[1];
    public ArrayList<double[]> data = new ArrayList<>();
    private JFreeChart barChart;

    @Override
    public void loadData(File inputFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String Line = reader.readLine();
        String[] strs = Line.split("\\s+");
        try {
            XLabel = strs[0];
            YLabel = strs[1];
            System.arraycopy(strs, 1, Classification, 0, strs.length - 1);
        } catch (IndexOutOfBoundsException ignored) {

        }
        while ((Line = reader.readLine()) != null) {
            strs = Line.split("\\s+");
            try {
                XSample.add(strs[0]);
                data.add(new double[Classification.length]);
                for (int i = 0; i < Classification.length; i++) {
                    data.get(data.size() - 1)[i] = Double.parseDouble(strs[i + 1].replaceAll("\\s", ""));
                }
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                System.err.println("Error!\tIncorrect data format in follow line:\n" + Line);
                System.exit(1);
            }
        }
    }

    @Override
    public void drawing(File outputFile) throws IOException {
        DefaultCategoryDataset DataSet = getDataSet();
        barChart = ChartFactory.createBarChart(this.FigureTitle, XLabel, YLabel, DataSet, PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot plot = barChart.getCategoryPlot();
        barChart.setBackgroundPaint(Color.WHITE);
        if (Classification.length <= 1) {
            barChart.removeLegend();
        }
        ChartUtilities.saveChartAsPNG(outputFile, barChart, 1280, 960);
    }

    public DefaultCategoryDataset getDataSet() {
        if (Classification[0] == null) {
            Classification[0] = "";
        }
        DefaultCategoryDataset DataSet = new DefaultCategoryDataset();
        for (int i = 0; i < Classification.length; i++) {
            for (int j = 0; j < XSample.size(); j++) {
//                System.out.println("add value: " + data.get(j)[i] + " " + Classification[i] + " " + XSample.get(j));
                DataSet.addValue(data.get(j)[i], Classification[i], XSample.get(j));
            }
        }
        return DataSet;
    }

    public JFreeChart getBarChart() {

        DefaultCategoryDataset DataSet = getDataSet();
        barChart = ChartFactory.createBarChart(this.FigureTitle, XLabel, YLabel, DataSet, PlotOrientation.VERTICAL, true, true, false);
        return barChart;
    }
}
