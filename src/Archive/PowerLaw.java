package Archive;

import Component.File.BedpeFile;
import Component.unit.Opts;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import Component.tool.Tools;
import Component.unit.IntegerArrays;

public class PowerLaw {
    private BedpeFile BedpeFile;
    private int StepLength;
    private String OutFile;


    PowerLaw(BedpeFile bedpe, int length) {
        BedpeFile = bedpe;
        StepLength = length;
    }

    public static void main(String[] args) throws IOException, ParseException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("f").argName("file").hasArg().required().desc("[required] bedpe file").build());
        Argument.addOption(Option.builder("l").longOpt("step").argName("int").hasArg().required().desc("[required] step length").build());
        Argument.addOption(Option.builder("o").longOpt("out").argName("file").hasArg().desc("out file").build());
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp DLO-HIC-AnalysisTools.jar Archive.PowerLaw [option]", Argument);
            System.exit(1);
        }
        CommandLine line = new DefaultParser().parse(Argument, args);
        BedpeFile BedpeFile = new BedpeFile(line.getOptionValue("f"));
        int StepLength = Integer.parseInt(line.getOptionValue("l"));
        String OutFile = line.getOptionValue("o");
        if (OutFile == null) {
            new PowerLaw(BedpeFile, StepLength).Create();
        } else {
            new PowerLaw(BedpeFile, StepLength).Create(OutFile);
        }

    }

    public ArrayList<int[]> Create() throws IOException {
        ArrayList<int[]> List = new ArrayList<>();
        List.add(new int[]{0, StepLength, 0});
        BufferedReader infile = new BufferedReader(new FileReader(BedpeFile));
        String line;
        String[] str;
        int distant;
        if (BedpeFile.BedpeDetect() == Opts.FileFormat.BedpePointFormat) {
            while ((line = infile.readLine()) != null) {
                str = line.split("\\s+");
                distant = Math.abs(Integer.parseInt(str[1]) - Integer.parseInt(str[3]));
                int i = 0;
                while (i < List.size()) {
                    if (distant > List.get(i)[1]) {
                        i++;
                    } else {
                        List.get(i)[2]++;
                        break;
                    }
                }
                if (i == List.size()) {
                    List.add(new int[]{List.get(i - 1)[1] + 1, List.get(i - 1)[1] + StepLength, 0});
                    while (List.get(i)[1] < distant) {
                        i++;
                        List.add(new int[]{List.get(i - 1)[1] + 1, List.get(i - 1)[1] + StepLength, 0});
                    }
                    List.get(i)[2]++;
                }
            }
        } else if (BedpeFile.BedpeDetect() == Opts.FileFormat.BedpeRegionFormat) {
            while ((line = infile.readLine()) != null) {
                str = line.split("\\s+");
                distant = Math.abs(Integer.parseInt(str[5]) + Integer.parseInt(str[4]) - Integer.parseInt(str[2]) - Integer.parseInt(str[1])) / 2;
                int i = 0;
                while (i < List.size()) {
                    if (distant > List.get(i)[1]) {
                        i++;
                    } else {
                        List.get(i)[2]++;
                        break;
                    }
                }
                if (i == List.size()) {
                    List.add(new int[]{List.get(i - 1)[1] + 1, List.get(i - 1)[1] + StepLength, 0});
                    while (List.get(i)[1] < distant) {
                        i++;
                        List.add(new int[]{List.get(i - 1)[1] + 1, List.get(i - 1)[1] + StepLength, 0});
                    }
                    List.get(i)[2]++;
                }
            }
        } else {
            System.err.println("Error format!");
            System.exit(0);
        }
        return List;
    }

    public void Create(String outfile) throws IOException {
        ArrayList<int[]> List = new ArrayList<>();
        List.add(new int[]{0, StepLength, 0});
        BufferedReader infile = new BufferedReader(new FileReader(BedpeFile));
        String line;
        String[] str;
        int distant;
        if (BedpeFile.BedpeDetect() == Opts.FileFormat.BedpePointFormat) {
            while ((line = infile.readLine()) != null) {
                str = line.split("\\s+");
                distant = Math.abs(Integer.parseInt(str[1]) - Integer.parseInt(str[3]));
                int i = 0;
                while (i < List.size()) {
                    if (distant > List.get(i)[1]) {
                        i++;
                    } else {
                        List.get(i)[2]++;
                        break;
                    }
                }
                if (i == List.size()) {
                    List.add(new int[]{List.get(i - 1)[1] + 1, List.get(i - 1)[1] + StepLength, 0});
                    while (List.get(i)[1] < distant) {
                        i++;
                        List.add(new int[]{List.get(i - 1)[1] + 1, List.get(i - 1)[1] + StepLength, 0});
                    }
                    List.get(i)[2]++;
                }
            }
        } else if (BedpeFile.BedpeDetect() == Opts.FileFormat.BedpeRegionFormat) {
            while ((line = infile.readLine()) != null) {
                str = line.split("\\s+");
                distant = Math.abs(Integer.parseInt(str[5]) + Integer.parseInt(str[4]) - Integer.parseInt(str[2]) - Integer.parseInt(str[1])) / 2;
                int i = 0;
                while (i < List.size()) {
                    if (distant > List.get(i)[1]) {
                        i++;
                    } else {
                        List.get(i)[2]++;
                        break;
                    }
                }
                if (i == List.size()) {
                    List.add(new int[]{List.get(i - 1)[1] + 1, List.get(i - 1)[1] + StepLength, 0});
                    while (List.get(i)[1] < distant) {
                        i++;
                        List.add(new int[]{List.get(i - 1)[1] + 1, List.get(i - 1)[1] + StepLength, 0});
                    }
                    List.get(i)[2]++;
                }
            }
        } else {
            System.err.println("Error format!");
            System.exit(0);
        }
        ArrayList<String> templist = new ArrayList<>();
        for (int[] anlist : List) {
            templist.add(String.join("\t", IntegerArrays.toString(anlist)));
        }
        Tools.PrintList(templist, new File(outfile));

    }

}
