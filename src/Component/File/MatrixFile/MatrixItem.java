package Component.File.MatrixFile;

import Component.File.AbstractItem;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

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

//    @Override
//    public int compareTo(MatrixItem o) {
//        return 0;
//    }
}
