package Utils;

import Component.File.MatrixFile.MatrixFile;
import Component.File.MatrixFile.MatrixItem;

import java.io.IOException;

public class MatrixConversion {
    public static void main(String[] args) throws IOException {
        MatrixFile file1 =new MatrixFile(args[0]);
        MatrixFile file2 = new MatrixFile(args[1]);
        MatrixItem item = file1.ReadItem();
        MatrixFile.Format format = MatrixFile.FormatDetection(file1);
    }
}
