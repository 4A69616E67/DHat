package Component.tool;

import Component.unit.Opts;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.Date;

public class BedToBedpe {

    public static void main(String[] args) throws IOException {
        Options options = new Options();
        options.addOption(Option.builder("i").longOpt("in").hasArgs().argName("files").required().desc("bed files you want to merge").build());
        options.addOption(Option.builder("o").longOpt("out").hasArgs().argName("file").required().desc("bedpe files you want to create").build());
        options.addOption(Option.builder("l").longOpt("col").hasArgs().argName("int").desc("index column when merge (the column include reads name in common)").build());
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp path/" + Opts.JarFile.getName() + " " + BedToBedpe.class.getName(), options, true);
            System.exit(1);
        }
        CommandLine Comline = null;
        try {
            Comline = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        File infile1 = null, infile2 = null, outfile = null;
        int col;
        if (Comline.hasOption("i") && Comline.getOptionValues("i").length >= 2) {
            infile1 = new File(Comline.getOptionValues("i")[0]);
            infile2 = new File(Comline.getOptionValues("i")[1]);
        } else {
            System.err.println("No enough input file, at least 2 input file");
            System.exit(1);
        }
        outfile = new File(Comline.getOptionValue("o"));
        col = Comline.hasOption("l") ? Integer.parseInt(Comline.getOptionValue("l")) : 4;
        new BedToBedpe(infile1, infile2, outfile, col, "\\s+");
    }


    public BedToBedpe(File InFile1, File InFile2, File OutFile, int Row, String Regex) throws IOException {
        System.out.println(new Date() + "\tMerge " + InFile1.getName() + " and " + InFile2.getName() + " to " + OutFile.getName() + " start");
        BufferedReader infile1 = new BufferedReader(new FileReader(InFile1));
        BufferedReader infile2 = new BufferedReader(new FileReader(InFile2));
        BufferedWriter outfile = new BufferedWriter(new FileWriter(OutFile));
        String regex = Regex.isEmpty() ? "\\s+" : Regex;
        String line1, line2;
        String[] str1, str2;
        line1 = infile1.readLine();
        line2 = infile2.readLine();
        str1 = line1.split(regex);
        str2 = line2.split(regex);
        boolean Flage = true;
        while (line1 != null && line2 != null) {
            if (Flage) {
                str1 = line1.split(regex);
            } else {
                str2 = line2.split(regex);
            }
            if (str1[Row - 1].compareTo(str2[Row - 1]) < 0) {
                line1 = infile1.readLine();
                Flage = true;
            } else if (str1[Row - 1].compareTo(str2[Row - 1]) > 0) {
                line2 = infile2.readLine();
                Flage = false;
            } else {
                outfile.write(str1[0] + "\t" + str1[1] + "\t" + str1[2] + "\t" + str2[0] + "\t" + str2[1] + "\t" + str2[2] + "\t" + str1[Row - 1] + "\t" + str1[str1.length - 2] + "\t" + str2[str2.length - 2] + "\t" + str1[str1.length - 1] + "\t" + str2[str2.length - 1] + "\n");
                line1 = infile1.readLine();
                line2 = infile2.readLine();
                try {
                    str1 = line1.split(regex);
                    str2 = line2.split(regex);
                } catch (NullPointerException ignored) {

                }
            }
        }
        infile1.close();
        infile2.close();
        outfile.close();
        System.out.println(new Date() + "\tMerge " + InFile1 + " and " + InFile2 + " to " + OutFile + " end");
    }
}
