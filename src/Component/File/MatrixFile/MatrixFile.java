package Component.File.MatrixFile;

import Component.File.AbstractFile;
import Component.File.BedFile.BedItem;
import Component.SystemDhat.CommandLineDhat;
import Component.tool.Tools;
import Component.unit.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by 浩 on 2019/2/1.
 */
public class MatrixFile extends AbstractFile<MatrixItem> {
    private enum Format {
        DenseMatrix, SparseMatrix, EmptyFile, ErrorFormat
    }

    public MatrixFile(String pathname) {
        super(pathname);
    }

    @Override
    protected MatrixItem ExtractItem(String[] s) {
        MatrixItem Item;
        if (s != null && s.length > 0) {
            Item = new MatrixItem(s.length, s[0].split("\\s+|,+").length);
            for (int i = 0; i < s.length; i++) {
                String[] ss = s[i].split("\\s+|,+");
                for (int j = 0; j < ss.length; j++) {
                    Item.item.setEntry(i, j, Double.parseDouble(ss[j]));
                }
            }
        } else {
            Item = null;
        }
        return Item;
    }

    @Override
    public synchronized String[] ReadItemLine() throws IOException {
        String line;
        ArrayList<String> matrix_line = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            matrix_line.add(line.trim());
        }
        return matrix_line.toArray(new String[0]);
    }

    @Override
    public void WriteItem(MatrixItem item) throws IOException {
        WriteItem(item, "\t");
    }

    public void WriteItem(MatrixItem item, String separator) throws IOException {
        for (int i = 0; i < item.item.getRowDimension(); i++) {
            for (int j = 0; j < item.item.getColumnDimension(); j++) {
                writer.write(item.item.getEntry(i, j) + separator);
            }
            writer.write("\n");
        }
    }

//    @Deprecated
//    @Override
//    public SortItem<MatrixItem> ReadSortItem() {
//        return null;
//    }
//
//    @Override
//    protected SortItem<MatrixItem> ExtractSortItem(String[] s) {
//        return null;
//    }

    public static Format FormatDetection(MatrixFile file) throws IOException {
        file.ReadOpen();
        BufferedReader reader = file.getReader();
        String Line = reader.readLine();
        if (Line == null) {
            return Format.EmptyFile;
        }
        String[] Str = Line.split("\\s+|,");
        try {
            StringArrays.toInteger(Str);
        } catch (NumberFormatException e) {
            return Format.ErrorFormat;
        }
        if (Str.length > 3) {
            return Format.DenseMatrix;
        }
        return Format.SparseMatrix;
    }

    public void PlotHeatMap(ArrayList<ChrRegion> bin_size, int resolution, File outFile) throws IOException {
        ReadOpen();
        MatrixItem item = ReadItem();
        ReadClose();
        BufferedImage image = item.DrawHeatMap(bin_size,resolution);
        ImageIO.write(image, outFile.getName().substring(outFile.getName().lastIndexOf('.') + 1), outFile);
        //=======================================================
//        String ComLine = Configure.Python.Exe() + " " + Opts.PlotHeatMapScriptFile + " -m A -i " + getPath() + " -o " + outFile + " -r " + resolution + " -c " + binSizeFile + " -q 98";
//        Opts.CommandOutFile.Append(ComLine + "\n");
//        if (Configure.DeBugLevel < 1) {
//            return CommandLineDhat.run(ComLine);
//        } else {
//            return CommandLineDhat.run(ComLine, null, new PrintWriter(System.err));
//        }
    }

    public void PlotHeatMap(ChrRegion chr1, ChrRegion chr2, int resolution, float threshold, File outFile) throws IOException {
        ReadOpen();
        MatrixItem item = ReadItem();
        ReadClose();
        item.Chr1 = chr1;
        item.Chr2 = chr2;
        ImageIO.write(item.DrawHeatMap(resolution, threshold, true), outFile.getName().substring(outFile.getName().lastIndexOf('.') + 1), outFile);
//        String ComLine = Configure.Python.Exe() + " " + Opts.PlotHeatMapScriptFile + " -t localGenome -m A -i " + getPath() + " -o " + outFile + " -r " + resolution + " -p " + String.join(":", Region) + " -q 95";
//        Opts.CommandOutFile.Append(ComLine + "\n");
//        if (Configure.DeBugLevel < 1) {
//            return CommandLineDhat.run(ComLine);
//        } else {
//            return CommandLineDhat.run(ComLine, null, new PrintWriter(System.err));
//        }
    }
}
