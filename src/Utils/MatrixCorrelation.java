package Utils;

import Component.File.MatrixFile.MatrixFile;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.stat.StatUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by snowf on 2019/12/30.
 */

public class MatrixCorrelation {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("usage: java -cp " + MatrixCorrelation.class.getName() + " <file1> <file2> ");
            System.exit(1);
        }
        MatrixFile file1 = new MatrixFile(args[0]), file2 = new MatrixFile(args[1]);
        RealMatrix matrix1, matrix2;
//        matrix1 = new Array2DRowRealMatrix(new double[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}});
//        matrix2 = new Array2DRowRealMatrix(new double[][]{{11, 223, 31}, {8, 10, 12}, {14, 16, 18}});
        file1.ReadOpen();
        matrix1 = file1.ReadItem().item;
        file1.ReadClose();

        file2.ReadOpen();
        matrix2 = file2.ReadItem().item;
        file2.ReadClose();

        RealVector vector1 = new ArrayRealVector();
        for (int i = 0; i < matrix1.getRowDimension(); i++) {
            vector1 = vector1.append(matrix1.getRowVector(i));
        }

        RealVector vector2 = new ArrayRealVector();
        for (int i = 0; i < matrix2.getRowDimension(); i++) {
            vector2 = vector2.append(matrix2.getRowVector(i));
        }

        System.out.println(vector1.dotProduct(vector2) / Math.sqrt(StatUtils.sum(vector1.ebeMultiply(vector1).toArray())) / Math.sqrt(StatUtils.sum(vector2.ebeMultiply(vector2).toArray())));
    }
}
