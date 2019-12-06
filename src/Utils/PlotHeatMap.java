package Utils;

import Component.File.MatrixFile.MatrixFile;
import Component.File.MatrixFile.MatrixItem;
import Component.unit.Opts;
import org.apache.commons.cli.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by snowf on 2019/11/15.
 */

public class PlotHeatMap {
    public static void main(String[] args) throws IOException, ParseException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("i").hasArg().argName("file").required().desc("input matrix file").build());
        Argument.addOption(Option.builder("r").hasArg().argName("int").required().desc("resolution").build());
        Argument.addOption(Option.builder("chr1").hasArg().argName("str").desc("the name and start site of chromosome 1 (example Chr1:0)").build());
        Argument.addOption(Option.builder("chr2").hasArg().argName("str").desc("the name and start site of chromosome 2 (example Chr3:0)").build());
        Argument.addOption(Option.builder("o").hasArg().argName("file").desc("output file name").build());
        Argument.addOption(Option.builder("f").hasArg().argName("float").desc("threshold (>= 0 and <= 1) [default 0.99]").build());
        if (args.length <= 3) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getName() + " " + PlotHeatMap.class.getName(), Argument, true);
            System.exit(1);
        }
        CommandLine commandLine = new DefaultParser().parse(Argument, args);
        File InputFile = Opts.GetFileOpt(commandLine, "i", null);
        int Resolution = Opts.GetIntOpt(commandLine, "r", 1000000);
        String c1 = Opts.GetStringOpt(commandLine, "chr1", "Chr1?:0");
        String c2 = Opts.GetStringOpt(commandLine, "chr2", "Chr2?:0");
        File OutputFile = Opts.GetFileOpt(commandLine, "o", new File(InputFile.getPath() + ".png"));
        float Threshold = Opts.GetFloatOpt(commandLine, "f", 0.99f);
        String Chr1 = c1.split(":")[0].equals("") ? "Chr1?" : c1.split(":")[0];
        int Chr1Site = c1.split(":")[1].equals("") ? 0 : Integer.parseInt(c1.split(":")[1]);
        String Chr2 = c2.split(":")[0].equals("") ? "Chr2?" : c2.split(":")[0];
        int Chr2Site = c2.split(":")[1].equals("") ? 0 : Integer.parseInt(c2.split(":")[1]);
        //==============================================================================================================
        MatrixFile file = new MatrixFile(InputFile.getPath());
        file.ReadOpen();
        MatrixItem item = file.ReadItem();
        file.ReadClose();
        BufferedImage image = item.PlotHeatMap(Chr1, Chr1Site, Chr2, Chr2Site, Resolution, Threshold);
        ImageIO.write(image, OutputFile.getName().substring(OutputFile.getName().lastIndexOf('.') + 1), OutputFile);
//
    }
}
