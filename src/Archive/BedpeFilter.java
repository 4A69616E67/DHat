package Archive;

import Component.File.BedpeFile;
import Component.File.FileTool;
import Component.tool.BedPeFilter;
import Component.unit.ChrRegion;
import Component.unit.InterAction;
import Component.unit.Opts;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;

public class BedpeFilter {
    public static void main(String[] args) throws ParseException, IOException {
        Options Argument = new Options();
        Argument.addOption("i", true, "bedpefile");
        Argument.addOption("f", true, "filter list");
        Argument.addOption("o", true, "out file");
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getAbsolutePath() + " " + BedpeFilter.class.getName() + " [option]", Argument);
            System.exit(1);
        }
        CommandLine Comline = new DefaultParser().parse(Argument, args);
        BedpeFile InFile = new BedpeFile(Comline.getOptionValue("i"));
        BedpeFile FilterFile = new BedpeFile(Comline.getOptionValue("f"));
        String OutFile = Comline.getOptionValue("o");
//        ArrayList<InterAction> List = FileTool.ReadInterActionFile(InFile, -1, -1, -1, -1);
        ArrayList<InterAction> FilterList = FileTool.ReadInterActionFile(FilterFile, -1, 6, -1, -1);
        BedPeFilter Filter = new BedPeFilter(FilterList);
        BufferedReader in = new BufferedReader(new FileReader(InFile));
        BufferedWriter out = new BufferedWriter(new FileWriter(OutFile));
        String line;
        String[] str;
        switch (InFile.BedpeDetect()) {
            case BedpePointFormat:
                while ((line = in.readLine()) != null) {
                    str = line.split("\\s+");
                    if (Filter.Run(new InterAction(new ChrRegion(new String[]{str[0], str[1], str[1]}), new ChrRegion(new String[]{str[2], str[3], str[3]})))) {
                        out.write(line + "\n");
                    }
                }
                break;
            case BedpeRegionFormat:
                while ((line = in.readLine()) != null) {
                    str = line.split("\\s+");
                    if (Filter.Run(new InterAction(new ChrRegion(new String[]{str[0], str[1], str[2]}), new ChrRegion(new String[]{str[3], str[4], str[5]})))) {
                        out.write(line + "\n");
                    }
                }
                break;
            default:
                System.err.println("Error format");
                System.exit(1);
        }
        out.close();
    }
}
