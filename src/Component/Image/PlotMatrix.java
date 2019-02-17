package Component.Image;

import Component.tool.Tools;
import Component.unit.Configure;
import Component.unit.Opts;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class PlotMatrix {
    private File MatrixFile;
    private File OutFile;
    private int Resolution;
    private String ComLine;

    public PlotMatrix(File MatrixFile, File OutFile, int Resolution) {
        this.MatrixFile = MatrixFile;
        this.OutFile = OutFile;
        this.Resolution = Resolution;
    }

    public int Run(File BinSizeFile) throws IOException, InterruptedException {
        ComLine = "python " + Opts.PlotHeatMapScriptFile + " -m A -i " + MatrixFile + " -o " + OutFile + " -r " + Resolution + " -c " + BinSizeFile + " -q 98";
        Opts.CommandOutFile.Append(ComLine + "\n");
        if (Configure.DeBugLevel < 1) {
            return Tools.ExecuteCommandStr(ComLine, null, null);
        } else {
            return Tools.ExecuteCommandStr(ComLine, null, new PrintWriter(System.err));
        }
    }

    public int Run(String[] Region) throws IOException, InterruptedException {
        ComLine = "python " + Opts.PlotHeatMapScriptFile + " -t localGenome -m A -i " + MatrixFile + " -o " + OutFile + " -r " + Resolution + " -p " + String.join(":", Region) + " -q 95";
        Opts.CommandOutFile.Append(ComLine + "\n");
        if (Configure.DeBugLevel < 1) {
            return Tools.ExecuteCommandStr(ComLine, null, null);
        } else {
            return Tools.ExecuteCommandStr(ComLine, null, new PrintWriter(System.err));
        }
    }

    public String getComLine() {
        return ComLine;
    }
}
