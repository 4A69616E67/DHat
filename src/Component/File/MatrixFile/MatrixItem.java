package Component.File.MatrixFile;

import Component.File.AbstractItem;
import Component.tool.Tools;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import sun.font.FontDesignMetrics;

import javax.imageio.ImageIO;
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

    public MatrixItem(int rowDimension, int columnDimension) throws NotStrictlyPositiveException {
        item = new Array2DRowRealMatrix(rowDimension, columnDimension);
    }

    public static class MatrixComparator implements Comparator<MatrixItem> {

        @Override
        public int compare(MatrixItem o1, MatrixItem o2) {
            return 0;
        }
    }

    public static void PlotHeatMap(String Chr1, int StartSite1, String Chr2, int StartSite2, RealMatrix Matrix, int Resolution, File OutFile, float threshold) throws IOException {
        int MatrixHeight = Matrix.getRowDimension();
        int MatrixWidth = Matrix.getColumnDimension();
        int StandardImageSize = 2000;
        int Marginal = 160;
        int LegendWidth = 20;
        int interval = 200;
        int extend_len = 15;
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(-Math.PI / 2, 0, 0);
        Font t;
        //=======================================================
        ArrayList<Double> list = new ArrayList<>();
        for (int i = 0; i < MatrixHeight; i++) {
            for (int j = 0; j < MatrixWidth; j++) {
                list.add(Matrix.getEntry(i, j));
            }
        }
        Collections.sort(list);
        double ThresholdValue = list.get((int) (list.size() * threshold));
        for (int i = 0; i < MatrixHeight; i++) {
            for (int j = 0; j < MatrixWidth; j++) {
                if (Matrix.getEntry(i, j) > ThresholdValue) {
                    Matrix.setEntry(i, j, ThresholdValue);
                }
            }
        }
        BufferedImage matrix_image = new BufferedImage(MatrixWidth, MatrixHeight, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < MatrixHeight; i++) {
            for (int j = 0; j < MatrixWidth; j++) {
                double value = Matrix.getEntry(i, j);
                matrix_image.setRGB(MatrixHeight - i - 1, j, new Color(255, (int) (255 * (1 - value / ThresholdValue)), (int) (255 * (1 - value / ThresholdValue))).getRGB());
            }
        }
        BufferedImage image = new BufferedImage(MatrixWidth + Marginal * 2, MatrixHeight + Marginal * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        //set transparent background
        graphics.setColor(new Color(10, 10, 100, 0));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.drawImage(matrix_image, Marginal, Marginal, null);
        //draw x y label
        graphics.setColor(Color.BLACK);
        graphics.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        graphics.drawLine(Marginal, Marginal + MatrixHeight, Marginal + MatrixWidth, Marginal + MatrixHeight);//draw x label line
        graphics.drawLine(Marginal, Marginal + MatrixHeight, Marginal, Marginal);//draw y label line
        t = new Font(null, Font.PLAIN, 30);
        //draw x interval
        for (int i = 0; i <= MatrixWidth / interval; i++) {
            graphics.drawLine(Marginal + i * interval, Marginal + MatrixHeight, Marginal + i * interval, Marginal + MatrixHeight + extend_len);
            float value = (float) (StartSite2 + i * interval * Resolution) / 1000000;
            String value_str;
            if (value == (int) value) {
                value_str = String.format("%d", (int) (value));
            } else {
                value_str = String.format("%.2f", value);
            }
            int h = FontDesignMetrics.getMetrics(t).getHeight();
            Tools.DrawStringCenter(graphics, value_str, t, Marginal + i * interval, Marginal + MatrixHeight + extend_len + h / 2 + 2, 0);
        }
        //draw y interval
        for (int i = 0; i <= MatrixHeight / interval; i++) {
            graphics.drawLine(Marginal, Marginal + MatrixHeight - i * interval, Marginal - extend_len, Marginal + MatrixHeight - i * interval);
            float value = (float) (StartSite1 + i * interval * Resolution) / 1000000;
            String value_str;
            if (value == (int) value) {
                value_str = String.format("%d", (int) (value));
            } else {
                value_str = String.format("%.2f", value);
            }
            int h = FontDesignMetrics.getMetrics(t).getHeight();
            Tools.DrawStringCenter(graphics, value_str, t, Marginal - extend_len - h / 2 - 2, Marginal + MatrixHeight - i * interval, -Math.PI / 2);
        }
//        graphics.setStroke(new BasicStroke(3.0f));
        //draw legend
        interval = 40;
        graphics.setPaint(new GradientPaint(Marginal + MatrixWidth + interval, Marginal + MatrixHeight, Color.WHITE, Marginal + MatrixWidth + interval, Marginal, Color.RED));
        graphics.fillRect(Marginal + MatrixWidth + interval, Marginal, LegendWidth, MatrixHeight);
        graphics.setColor(Color.BLACK);
        for (int i = 0; i <= 10; i++) {
            graphics.drawLine(Marginal + MatrixWidth + interval + LegendWidth, Math.round(Marginal + MatrixHeight - (float) (i) / 10 * MatrixHeight), Marginal + MatrixWidth + interval + LegendWidth + extend_len, Math.round(Marginal + MatrixHeight - (float) (i) / 10 * MatrixHeight));
            String value_str = String.format("%.1f", list.get((int) ((float) (i) / 10 * list.size() * threshold)));
            Tools.DrawStringCenter(graphics, value_str, t, Marginal + MatrixWidth + interval + LegendWidth + extend_len + 2 + FontDesignMetrics.getMetrics(t).stringWidth(value_str) / 2, Math.round(Marginal + MatrixHeight - (float) (i) / 10 * MatrixHeight), 0);
        }
        //draw x,y title
        t = new Font(null, Font.BOLD, 80);
        Tools.DrawStringCenter(graphics, Chr1, t, FontDesignMetrics.getMetrics(t).getHeight() / 2 + 5, Marginal + MatrixHeight / 2, -Math.PI / 2);//draw y title,rotation pi/2 anticlockwise
        Tools.DrawStringCenter(graphics, Chr2, t, Marginal + MatrixWidth / 2, 2 * Marginal + MatrixHeight - 5 - FontDesignMetrics.getMetrics(t).getHeight() / 2, 0);//draw x title

        ImageIO.write(image, "png", OutFile);
    }

    public void PlotHeatMap(File OutFile, String Chr1, int StartSite1, String Chr2, int StartSite2, int resolution, float threshold) throws IOException {
        PlotHeatMap(Chr1, StartSite1, Chr2, StartSite2, item, resolution, OutFile, threshold);
    }

//    @Override
//    public int compareTo(MatrixItem o) {
//        return 0;
//    }
}
