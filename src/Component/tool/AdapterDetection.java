package Component.tool;

import Component.unit.Configure;
import Component.unit.Opts;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by snowf on 2019/2/17.
 */
public class AdapterDetection {
    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("i").hasArg().desc("input file").required().build());
        Argument.addOption(Option.builder("p").hasArg().desc("prefix").build());
        Argument.addOption(Option.builder("c").hasArg().desc("cutoff position").build());
        Argument.addOption(Option.builder("q").hasArg(false).desc("quiet mode").build());
        Argument.addOption(Option.builder("n").hasArg(false).desc("sequence number use to processing").build());
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getName() + " " + AdapterDetection.class.getName(), Argument, true);
            System.exit(1);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        File InPutFile = Opts.GetFileOpt(ComLine, "i", null);
        String Prefix = Opts.GetStringOpt(ComLine, "p", Configure.Prefix);
        int SubIndex = Opts.GetIntOpt(ComLine, "c", 0);
        int SeqNum = Opts.GetIntOpt(ComLine, "n", 100);
        StringBuilder Adapter = new StringBuilder();
        ArrayList<char[]> MsaStat = new ArrayList<>();
        int[] CountArrays = new int[255];
        File HeadFile = new File(Prefix + ".head" + SeqNum);
        File MsaFile = new File(Prefix + ".msa");
        BufferedReader reader = new BufferedReader(new FileReader(InPutFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(HeadFile));
        String line;
        int linenumber = 0;
        while ((line = reader.readLine()) != null && ++linenumber <= SeqNum * 4) {
            if (linenumber % 4 == 1) {
                writer.write(line.replace("@", ">") + "\n");
            } else if (linenumber % 4 == 2) {
                writer.write(line.substring(SubIndex, line.length()) + "\n");
            }
        }
        reader.close();
        writer.close();
        String s = "mafft " + HeadFile.getPath();
        PrintWriter msa = new PrintWriter(MsaFile);
        if (ComLine.hasOption("q")) {
            Tools.ExecuteCommandStr(s, msa, null);
        } else {
            Tools.ExecuteCommandStr(s, msa, new PrintWriter(System.err));
        }
        msa.close();
        HeadFile.delete();
        reader = new BufferedReader(new FileReader(MsaFile));
        reader.readLine();
        while ((line = reader.readLine()) != null) {
            if (line.matches("^>.*")) {
                MsaStat.add(Adapter.toString().toCharArray());
                Adapter.setLength(0);
                continue;
            }
            Adapter.append(line);
        }
        MsaStat.add(Adapter.toString().toCharArray());
        Adapter.setLength(0);
        reader.close();
        for (int i = 0; i < MsaStat.get(0).length; i++) {
            CountArrays['A'] = 0;
            CountArrays['T'] = 0;
            CountArrays['C'] = 0;
            CountArrays['G'] = 0;
            CountArrays['-'] = 0;
            for (int j = 0; j < MsaStat.size(); j++) {
                CountArrays[Character.toUpperCase(MsaStat.get(j)[i])]++;
            }
            int MaxValue = 0;
            char MaxBase = '-';
            for (char base : new char[]{'A', 'T', 'C', 'G', '-'}) {
                if (CountArrays[base] > MaxValue) {
                    MaxValue = CountArrays[base];
                    MaxBase = base;
                }
            }
            if (MaxValue > SeqNum / 2) {
                Adapter.append(MaxBase);
            } else {
                Adapter.append('N');
            }
        }
        String[] SplitAdapter = Adapter.toString().replace("-", "").split("N+");
        int MaxValue = 0;
        writer = new BufferedWriter(new FileWriter(Prefix + ".potential.adapter"));
        for (int i = 0; i < SplitAdapter.length; i++) {
            writer.write(SplitAdapter[i] + "\n");
            if (SplitAdapter[i].length() > MaxValue) {
                MaxValue = SplitAdapter[i].length();
                Adapter = new StringBuilder(SplitAdapter[i]);
            }
        }
        writer.close();
        System.out.println("Adapter:\t" + Adapter);
    }
}
