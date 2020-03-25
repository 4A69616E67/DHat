package Component.File.MatrixFile;

import Component.File.AbstractItem;
import Component.tool.Tools;
import Component.unit.ChrRegion;
import Component.unit.InterAction;
import Component.unit.Region;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import sun.font.FontDesignMetrics;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by 浩 on 2019/2/1.
 */

public class MatrixItem extends AbstractItem {
    public Array2DRowRealMatrix item;
    public ChrRegion Chr1 = new ChrRegion("C1", 0, 0);
    public ChrRegion Chr2 = new ChrRegion("C2", 0, 0);
    public int Resolution;
    private int Fold = 1;
    private double MinValue;
    private double MaxValue;
    private double ThresholdValue;
    private int Marginal = 160;
    public boolean Legend = true;
    public boolean Label = true;
    public Unit unit = Unit.MB;

    public enum Unit {
        BP, KB, MB
    }

    public MatrixItem(InterAction action, int resolution) {
        Chr1 = action.getLeft();
        Chr2 = action.getRight();
        Resolution = resolution;
        item = new Array2DRowRealMatrix(Chr1.region.getLength() / Resolution + 1, Chr2.region.getLength() / Resolution + 1);
    }

    public MatrixItem(ChrRegion chr1, ChrRegion chr2, double[][] matrix) {
        this(matrix);
        Chr1 = chr1;
        Chr2 = chr2;
    }

    public MatrixItem(int rowDimension, int columnDimension) throws NotStrictlyPositiveException {
        item = new Array2DRowRealMatrix(rowDimension, columnDimension);
    }

    public MatrixItem(double[][] matrix) {
        item = new Array2DRowRealMatrix(matrix);
    }

    public static class MatrixComparator implements Comparator<MatrixItem> {

        @Override
        public int compare(MatrixItem o1, MatrixItem o2) {
            return 0;
        }
    }

    public BufferedImage DrawMatrix(float threshold, boolean reverse) {
        int MatrixHeight = item.getRowDimension();
        int MatrixWidth = item.getColumnDimension();
        ArrayList<Double> list = new ArrayList<>();
        for (int i = 0; i < MatrixHeight; i++) {
            for (int j = 0; j < MatrixWidth; j++) {
                list.add(item.getEntry(i, j));
            }
        }
        Collections.sort(list);
        MinValue = list.get(0);
        MaxValue = list.get(list.size() - 1);
        ThresholdValue = list.get((int) ((list.size() - 1) * threshold));
        //generate heatmap
        BufferedImage matrix_image = new BufferedImage(MatrixWidth, MatrixHeight, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < MatrixHeight; i++) {
            for (int j = 0; j < MatrixWidth; j++) {
                double value = item.getEntry(i, j);
                Color c;
                if (value >= MinValue && value <= MaxValue) {
                    value = Math.min(value, ThresholdValue);
                    double percent = 1 - (value - MinValue) / (ThresholdValue - MinValue);
                    c = new Color(255, (int) (255 * percent), (int) (255 * percent));
                } else {
                    c = new Color(255, 255, 255, 0);
                }
                if (reverse) {
                    matrix_image.setRGB(j, MatrixHeight - 1 - i, c.getRGB());
                } else {
                    matrix_image.setRGB(j, i, c.getRGB());
                }
            }
        }
        return matrix_image;
    }

    public BufferedImage DrawHeatMap(int resolution, float threshold, boolean reverse) {
        return DrawHeatMap(Chr1.Chr, Chr1.region.Start, Chr2.Chr, Chr2.region.Start, resolution, threshold, reverse);
    }

    public BufferedImage DrawHeatMap(ArrayList<ChrRegion> bin_size, int resolution, float thresholdValue) {
        int High = item.getRowDimension();
        int Width = item.getColumnDimension();
        int interval = 30;
        Label = false;
        BufferedImage image = DrawHeatMap(resolution, thresholdValue, true);
        Graphics2D g = image.createGraphics();
        int fold = getFold();
        int marginal = getMarginal();
        BasicStroke stroke = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 2, new float[]{10, 5}, 0);
        g.setStroke(stroke);
        Font t = new Font("Times New Roman", Font.PLAIN, 20);
        g.setFont(t);
        g.setColor(Color.BLACK);
        for (int i = 0; i < bin_size.size(); i++) {
            ChrRegion r = bin_size.get(i);
            if (i > 0) {
                g.drawLine(marginal + r.region.Start * fold - 1, marginal, marginal + r.region.Start * fold - 1, marginal + High * fold);//draw vertical line
                g.drawLine(marginal, (High - r.region.Start) * fold + marginal, marginal + Width * fold, (High - r.region.Start) * fold + marginal);// draw horizontal line
            }
            Tools.DrawStringCenter(g, r.Chr, t, marginal + r.region.Center() * fold, High * fold + interval + marginal, 0);// draw X label
            Tools.DrawStringCenter(g, r.Chr, t, marginal - interval, (High - r.region.Center()) * fold + marginal, -Math.PI / 2);// draw Y label
        }
        return image;
    }

    public BufferedImage DrawHeatMap(String Chr1, int StartSite1, String Chr2, int StartSite2, int resolution, float threshold, boolean reverse) {
        int MatrixHeight = item.getRowDimension();
        int MatrixWidth = item.getColumnDimension();
        int StandardImageSize = 2000;
        int LegendWidth = 20;
        int interval = 200;
        int extend_len = 15;
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(-Math.PI / 2, 0, 0);
        Font t;
        BufferedImage matrix_image = DrawMatrix(threshold, reverse);
        //zoom on if the original graphic size is too small
        Fold = Math.max(StandardImageSize / MatrixHeight, StandardImageSize / MatrixWidth);
        Fold = Math.max(Fold, 1);
        //calculate new figure size
        MatrixHeight = MatrixHeight * Fold;
        MatrixWidth = MatrixWidth * Fold;
        resolution = resolution / Fold;//correct resolution,
        BufferedImage image = new BufferedImage(MatrixWidth + Marginal * 2, MatrixHeight + Marginal * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        //set transparent background
        graphics.setColor(new Color(255, 255, 255, 0));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.drawImage(matrix_image, Marginal, Marginal, MatrixWidth, MatrixHeight, null);
        graphics.setColor(Color.BLACK);
        graphics.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        //draw x y label
        if (Label) {
            t = new Font("Times New Roman", Font.PLAIN, 30);
            graphics.drawLine(Marginal, Marginal + MatrixHeight, Marginal + MatrixWidth, Marginal + MatrixHeight);//draw x label line
            graphics.drawLine(Marginal, Marginal + MatrixHeight, Marginal, Marginal);//draw y label line
            //draw sub x interval
            for (int i = 0; i <= MatrixWidth * 10 / interval; i++) {
                graphics.drawLine(Marginal + i * interval / 10, Marginal + MatrixHeight, Marginal + i * interval / 10, Marginal + MatrixHeight + extend_len / 2);
            }
            //draw x interval
            for (int i = 0; i <= MatrixWidth / interval; i++) {
                graphics.drawLine(Marginal + i * interval, Marginal + MatrixHeight, Marginal + i * interval, Marginal + MatrixHeight + extend_len);
                float value = 0;
                switch (unit) {
                    case BP:
                        value = (float) (StartSite2 + i * interval * resolution);
                        break;
                    case KB:
                        value = StartSite2 / 1000 + (float) resolution / 1000 * i * interval;
                        break;
                    case MB:
                        value = StartSite2 / 1000000 + (float) resolution / 1000000 * i * interval;
                        break;
                }
                String value_str;
                if (value == (int) value) {
                    value_str = String.format("%d", (int) (value));
                } else {
                    value_str = String.format("%.2f", value);
                }
                int h = FontDesignMetrics.getMetrics(t).getHeight();
                Tools.DrawStringCenter(graphics, value_str, t, Marginal + i * interval, Marginal + MatrixHeight + extend_len + h / 2 + 2, 0);
            }
            //draw sub y interval
            for (int i = 0; i <= MatrixHeight * 10 / interval; i++) {
                int y1 = Marginal + MatrixHeight - i * interval / 10;
                graphics.drawLine(Marginal, y1, Marginal - extend_len / 2, y1);
            }
            //draw y interval
            for (int i = 0; i <= MatrixHeight / interval; i++) {
                graphics.drawLine(Marginal, Marginal + MatrixHeight - i * interval, Marginal - extend_len, Marginal + MatrixHeight - i * interval);
                float value = 0, index;
                switch (unit) {
                    case BP:
                        value = (float) (StartSite1 + i * interval * resolution);
                        break;
                    case KB:
                        value = StartSite1 / 1000 + (float) resolution / 1000 * i * interval;
                        break;
                    case MB:
                        value = StartSite1 / 1000000 + (float) resolution / 1000000 * i * interval;
                        break;
                }
                String value_str;
                if (value == (int) value) {
                    value_str = String.format("%d", (int) (value));
                } else {
                    value_str = String.format("%.2f", value);
                }
                if (reverse) {
                    index = MatrixHeight / interval - i;
                } else {
                    index = i;
                }
                int h = FontDesignMetrics.getMetrics(t).getHeight();
                Tools.DrawStringCenter(graphics, value_str, t, Marginal - extend_len - h / 2 - 2, Marginal + (int) index * interval, -Math.PI / 2);
            }
        }
        //draw legend
        if (Legend) {
            interval = 40;
            t = new Font("Times New Roman", Font.PLAIN, 30);
            graphics.setPaint(new GradientPaint(Marginal + MatrixWidth + interval, Marginal + MatrixHeight, Color.WHITE, Marginal + MatrixWidth + interval, Marginal, Color.RED));
            graphics.fillRect(Marginal + MatrixWidth + interval, Marginal, LegendWidth, MatrixHeight);
            graphics.setColor(Color.BLACK);
            for (int i = 0; i <= 10; i++) {
                graphics.drawLine(Marginal + MatrixWidth + interval + LegendWidth, Math.round(Marginal + MatrixHeight - (float) (i) / 10 * MatrixHeight), Marginal + MatrixWidth + interval + LegendWidth + extend_len, Math.round(Marginal + MatrixHeight - (float) (i) / 10 * MatrixHeight));
                String value_str = String.format("%.1f", MinValue + (ThresholdValue - MinValue) * (float) (i) / 10);
                Tools.DrawStringCenter(graphics, value_str, t, Marginal + MatrixWidth + interval + LegendWidth + extend_len + 2 + FontDesignMetrics.getMetrics(t).stringWidth(value_str) / 2, Math.round(Marginal + MatrixHeight - (float) (i) / 10 * MatrixHeight), 0);
            }
        }
        //draw x,y title
        if (Label) {
            t = new Font("Times New Roman", Font.BOLD, 80);
            Tools.DrawStringCenter(graphics, Chr1, t, FontDesignMetrics.getMetrics(t).getHeight() / 2 + 5, Marginal + MatrixHeight / 2, -Math.PI / 2);//draw y title,rotation pi/2 anticlockwise
            Tools.DrawStringCenter(graphics, Chr2, t, Marginal + MatrixWidth / 2, 2 * Marginal + MatrixHeight - 5 - FontDesignMetrics.getMetrics(t).getHeight() / 2, 0);//draw x title
        }
        return image;
    }

    public int getFold() {
        return Fold;
    }

    public int getMarginal() {
        return Marginal;
    }

    public boolean add(MatrixItem item) {
        if (this.item.getRowDimension() != item.item.getRowDimension() || this.item.getColumnDimension() != item.item.getColumnDimension()) {
            return false;
        }
        for (int i = 0; i < this.item.getRowDimension(); i++) {
            for (int j = 0; j < this.item.getColumnDimension(); j++) {
                this.item.addToEntry(i, j, item.item.getEntry(i, j));
            }
        }
        return true;
    }

    public boolean add(InterAction action, double value) {
        ChrRegion left = action.getLeft();
        ChrRegion right = action.getRight();
        boolean flag = false;
        if (left.Chr.equals(Chr1.Chr) && right.Chr.equals(Chr2.Chr)) {
            add(left.region, right.region, value);
            flag = true;
        }
        if (right.Chr.equals(Chr1.Chr) && left.Chr.equals(Chr2.Chr)) {
            add(right.region, left.region, value);
            flag = true;
        }
        return flag;
    }

    public boolean add(Region reg1, Region reg2, double value) {
        boolean flag = false;
        if (Chr1.region.IsContain(reg1.Center()) && Chr2.region.IsContain(reg2.Center())) {
            int[] index = new int[]{(reg1.Center() - Chr1.region.Start) / Resolution, (reg2.Center() - Chr2.region.Start) / Resolution};
            item.addToEntry(index[0], index[1], value);
            flag = true;
        }
        return flag;
    }

}
