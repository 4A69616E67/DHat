import java.io.IOException;

import Component.File.BedpeFile;
import Component.tool.Statistic;
import Component.unit.Opts;
import org.apache.commons.cli.*;

public class RangeCount {
    public static void main(String[] args) throws IOException, ParseException {
        Options Argument = new Options();
        Argument.addOption("t", true, "Threads number");
        Argument.addOption(Option.builder("r").required().argName("min:max").hasArg().desc("The range of you want to calculator (the value of minimum and maximum could't set)").build());
        Argument.addOption(Option.builder("f").required().argName("bedpe file").hasArg().desc("Bedpe file you want to calculator").build());
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getAbsolutePath() + " " + RangeCount.class.getName() + " [option]", Argument);
            System.exit(0);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        BedpeFile File = new BedpeFile(ComLine.getOptionValue("f"));
        float[] range = new float[2];
        try {
            range[0] = Float.parseFloat(ComLine.getOptionValue("r").split(":")[0]);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            range[0] = Float.NEGATIVE_INFINITY;
        }
        try {
            range[1] = Float.parseFloat(ComLine.getOptionValue("r").split(":")[1]);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            range[1] = Float.POSITIVE_INFINITY;
        }
        int Threads = ComLine.hasOption("t") ? Integer.parseInt(ComLine.getOptionValue("t")) : 1;
        try {
            System.out.println("Range between " + range[0] + " to " + range[1] + " :\t" + Statistic.RangeCount(File, range[0], range[1], Threads));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}