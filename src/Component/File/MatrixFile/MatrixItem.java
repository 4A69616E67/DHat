package Component.File.MatrixFile;

import Component.File.AbstractItem;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
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

    public static void PlotHeatMap(RealMatrix Matrix, File OutFile, float threshold) throws IOException {
        ArrayList<Double> list = new ArrayList<>();
        for (int i = 0; i < Matrix.getRowDimension(); i++) {
            for (int j = 0; j < Matrix.getColumnDimension(); j++) {
                list.add(Matrix.getEntry(i, j));
            }
        }
        Collections.sort(list);
        double ThresholdValue = list.get((int) (list.size() * threshold));
        for (int i = 0; i < Matrix.getRowDimension(); i++) {
            for (int j = 0; j < Matrix.getColumnDimension(); j++) {
                if (Matrix.getEntry(i, j) > ThresholdValue) {
                    Matrix.setEntry(i, j, ThresholdValue);
                }
            }
        }
        BufferedImage matrix_image = new BufferedImage(Matrix.getColumnDimension(), Matrix.getRowDimension(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < Matrix.getRowDimension(); i++) {
            for (int j = 0; j < Matrix.getColumnDimension(); j++) {
                double value = Matrix.getEntry(i, j);
                matrix_image.setRGB(Matrix.getRowDimension() - i - 1, j, new Color(255, (int) (255 * (1 - value / ThresholdValue)), (int) (255 * (1 - value / ThresholdValue))).getRGB());
            }
        }
        int Marginal = 20;
        BufferedImage image = new BufferedImage(Matrix.getColumnDimension() + Marginal * 2, Matrix.getRowDimension() + Marginal * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.drawImage(matrix_image, Marginal, Marginal, null);
        ImageIO.write(image, "png", OutFile);
    }

    public void PlotHeatMap(File OutFile, float threshold) throws IOException {
        PlotHeatMap(item, OutFile, threshold);
    }

//    @Override
//    public int compareTo(MatrixItem o) {
//        return 0;
//    }
}
