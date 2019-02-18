package Component.File;

import Component.tool.Tools;
import Component.unit.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Created by 浩 on 2019/2/1.
 */
public class MatrixFile extends AbstractFile<MatrixItem> {
    private enum Format {
        DenseMatrix, SpareMatrix, EmptyFile, ErrorFormat
    }

    public MatrixFile(String pathname) {
        super(pathname);
    }

    @Override
    protected MatrixItem ExtractItem(String s) {
        if (s != null) {
            String[] ss = s.split("\\n+");
            Item = new MatrixItem(ss.length, ss.length);
            for (int i = 0; i < ss.length; i++) {
                String[] sss = ss[i].split("\\s+|,+");
                for (int j = 0; j < sss.length; j++) {
                    Item.setEntry(i, j, Double.parseDouble(sss[j]));
                }
            }
        } else {
            Item = null;
        }
        return Item;
    }

    @Override
    public synchronized String ReadItemLine() throws IOException {
        StringBuilder s = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            s.append(line.trim()).append("\n");
        }
        s.setLength(s.length() - 1);
        return s.toString();
    }

    @Override
    public void WriteItem(MatrixItem item) throws IOException {
        WriteItem(item, "\t");
    }

    public void WriteItem(MatrixItem item, String separator) throws IOException {
        for (int i = 0; i < Item.getRowDimension(); i++) {
            for (int j = 0; j < Item.getColumnDimension(); j++) {
                writer.write(String.valueOf(Item.getEntry(i, j)) + separator);
            }
            writer.write("\n");
        }
    }

    @Deprecated
    @Override
    public SortItem<MatrixItem> ReadSortItem() {
        return null;
    }

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
        return Format.SpareMatrix;
    }

    public int PlotHeatMap(File binSizeFile, int resolution, File outFile) throws IOException, InterruptedException {
        String ComLine = "python " + Opts.PlotHeatMapScriptFile + " -m A -i " + getPath() + " -o " + outFile + " -r " + resolution + " -c " + binSizeFile + " -q 98";
        Opts.CommandOutFile.Append(ComLine + "\n");
        if (Configure.DeBugLevel < 1) {
            return Tools.ExecuteCommandStr(ComLine, null, null);
        } else {
            return Tools.ExecuteCommandStr(ComLine, null, new PrintWriter(System.err));
        }
    }

    public int PlotHeatMap(String[] Region, int resolution, File outFile) throws IOException, InterruptedException {
        String ComLine = "python " + Opts.PlotHeatMapScriptFile + " -t localGenome -m A -i " + getPath() + " -o " + outFile + " -r " + resolution + " -p " + String.join(":", Region) + " -q 95";
        Opts.CommandOutFile.Append(ComLine + "\n");
        if (Configure.DeBugLevel < 1) {
            return Tools.ExecuteCommandStr(ComLine, null, null);
        } else {
            return Tools.ExecuteCommandStr(ComLine, null, new PrintWriter(System.err));
        }
    }
}
