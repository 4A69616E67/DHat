package Component.tool;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *
 * Input:
 * 1) sequence file in text format
 * 2) linker sequences
 * 3) threshold
 *
 * Assumptions:
 * 1) there are ?? possible linker sequences
 * 2) all the linkers have the same length
 *
 */

import Component.File.FileTool;
import Component.unit.Configure;
import Component.unit.LinkerSequence;
import Component.unit.LocalAlignment;
import Component.unit.Opts;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;


public class LinkerFiltering {

    private File FastqFile;
    private File LinkerFile;
    private File AdapterFile;
    private String OutputPrefix;
    //-------------------------------------
    private File OutFile;
    private File DistributionFile;
    private LinkerSequence[] linkers;
    private String[] Adapters;
    private String[] AdapterAlignment;
    private float MinAdapterPercent = 0.7f;
    private int AdapterAlignmentLen = 30;
    private int[] scoreHist;
    private int[] secondBestScoreDiffHist;
    private int maxTagLength = 300;
    private int[] tagLengthDistribution = new int[maxTagLength];
    private int linkerLength;
    private int Threads;
    private int MatchScore = 1;
    private int MisMatchScore = -1;
    private int InDelScore = -1;
    private int CutOff;

    public LinkerFiltering(File fastqFile, File linkerFile, String outputPrefix, int threads) throws IOException {
        this(fastqFile, linkerFile, null, outputPrefix, threads);
    }

    private LinkerFiltering(File fastqFile, File linkerFile, File adapterFile, String outputPrefix, int threads) throws IOException {
        FastqFile = fastqFile;
        LinkerFile = linkerFile;
        AdapterFile = adapterFile;
        OutputPrefix = outputPrefix;
        Threads = threads;
        Init();
    }

    private LinkerFiltering(File fastqFile, File linkerFile, String outputPrefix, int matchscore, int mismatchscore, int indelscore, int threads) throws IOException {
        this(fastqFile, linkerFile, null, outputPrefix, matchscore, mismatchscore, indelscore, threads);
    }

    public LinkerFiltering(File fastqFile, File linkerFile, File adapterFile, String outputPrefix, int matchscore, int mismatchscore, int indelscore, int threads) throws IOException {
        FastqFile = fastqFile;
        LinkerFile = linkerFile;
        AdapterFile = adapterFile;
        OutputPrefix = outputPrefix;
        Threads = threads;
        MatchScore = matchscore;
        MisMatchScore = mismatchscore;
        InDelScore = indelscore;
        Init();
    }

    private void Init() throws IOException {
        int LinkersNum = ReadLinkers();
        if (LinkersNum <= 0) {
            System.err.println("No linker sequence information. Stop!!!");
            System.exit(1);
        }
        if (LinkersNum > 100) {
            System.err.println("Too many linkers. Please check!!!");
            System.exit(1);
        }
        if (AdapterFile != null && AdapterFile.isFile()) {
            int AdapterNum = ReadAdapter();
            if (AdapterNum <= 0) {
                Adapters = null;
            } else if (AdapterNum > 100) {
                System.err.println("Too many adapters. Please check!!!");
                System.exit(1);
            }
        }
        if (Adapters != null) {
            AdapterAlignment = new String[Adapters.length];
            for (int i = 0; i < Adapters.length; i++) {
                AdapterAlignment[i] = Adapters[i].substring(0, Math.min(AdapterAlignmentLen, Adapters[i].length()));
            }
        }
        OutFile = new File(OutputPrefix + ".output.txt");
        DistributionFile = new File(OutputPrefix + ".ScoreDistribution.txt");
    }

    public void Run() throws IOException, InterruptedException {
        BufferedReader infile = new BufferedReader(new FileReader(FastqFile));
        BufferedWriter outfile = new BufferedWriter(new FileWriter(OutFile));
        final int[] Count = new int[]{0};
        LocalAlignment[] local = new LocalAlignment[Threads];
        Thread[] Process = new Thread[Threads];
        for (int i = 0; i < Threads; i++) {
            int finalI = i;
            Process[i] = new Thread(() -> {
                String[] Lines;
                local[finalI] = new LocalAlignment(MatchScore, MisMatchScore, InDelScore);
                synchronized (Process) {
                    Lines = FileTool.Read4Line(infile);
                }
                while (Lines[3] != null) {
                    String[] Str = Execute(Lines[1], local[finalI], linkers, AdapterAlignment, CutOff, MinAdapterPercent);
                    Str[7] = Lines[0];
                    Str[9] = Lines[2];
                    Str[10] = Lines[3];
                    //=====================================================输出结果==========================================
                    synchronized (Process) {
                        try {
                            outfile.write(String.join("\t", Str) + "\n");
                            Count[0]++;
                            if (Count[0] % 1000000 == 0) {
                                System.out.println(new Date() + "\t" + (Count[0] / 1000000) + " Million reads processed");
                            }
                            Lines = FileTool.Read4Line(infile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //=============================================================================================
                }
            });
            Process[i].start();
        }
        for (int i = 0; i < Threads; i++) {
            Process[i].join();
        }
        System.out.println(new Date() + "\t" + Count[0] + " reads processed in total.");
        infile.close();
        outfile.close();
    }

    public static String[] Execute(String line, LocalAlignment local, LinkerSequence[] linkers, String[] adapters, int cutOff, float minAdapterPercent) {
        String[] Str;
        float MaxScore = 0;
        String LinkerIndex = "*";
        int MaxIndex = 0;
        int MinIndex = 0;
        int AdapterIndex = 0;
        if (adapters != null) {
            for (String adapter : adapters) {
                local.CreateMatrix(line, adapter);
                local.FindMaxIndex();
                float score = (float) local.getMaxScore() / (adapter.length() * Configure.MatchScore);
                if (score > MaxScore) {
                    local.FindMinIndex();
                    MaxScore = score;
                    AdapterIndex = local.getMinIndex()[0];
                }
            }
            if (MaxScore > minAdapterPercent) {
                line = line.substring(0, AdapterIndex - 1);
            } else {
                AdapterIndex = 0;
            }
            MaxScore = 0;
        }
        for (LinkerSequence linker : linkers) {
            local.CreateMatrix(line, linker.getSeq());
            local.FindMaxIndex();
            int score = local.getMaxScore();
            if (score > MaxScore) {
                local.FindMinIndex();
                MaxScore = score;
                LinkerIndex = linker.getType();
                MaxIndex = local.getMaxIndex()[0];
                MinIndex = local.getMinIndex()[0];
            }
        }
        String Col1, Col4;
        if (cutOff > 0) {
            Col1 = line.substring(Math.max(0, MinIndex - 1 - cutOff), Math.max(0, MinIndex - 1));
            Col4 = line.substring(MaxIndex, Math.min(MaxIndex + cutOff, line.length()));
        } else {
            Col1 = line.substring(0, Math.max(0, MinIndex - 1));
            Col4 = line.substring(MaxIndex);
        }
        String str = AdapterIndex == 0 ? "*" : String.valueOf(AdapterIndex);
        Col1 = Col1.equals("") ? "*" : Col1;
        Col4 = Col4.equals("") ? "*" : Col4;
        //=====================================================输出结果==========================================
        Str = new String[]{Col1, String.valueOf(MinIndex), String.valueOf(MaxIndex), Col4, str, String.valueOf(LinkerIndex), String.valueOf((int) MaxScore), "", line, "", ""};
        return Str;
    }

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("i").hasArg().argName("file").required().desc("fastq file").build());
        Argument.addOption(Option.builder("L").hasArg().argName("file").required().desc("linker file").build());
        Argument.addOption(Option.builder("A").hasArg().argName("file").desc("adapter file").build());
        Argument.addOption(Option.builder("p").hasArg().argName("string").desc("prefix").build());
        Argument.addOption(Option.builder("t").hasArg().argName("int").desc("ThreadNum").build());
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getName() + " " + LinkerFiltering.class.getName(), Argument, true);
            System.exit(1);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        File InputFile = Opts.GetFileOpt(ComLine, "i", null);
        File LinkerFile = Opts.GetFileOpt(ComLine, "L", null);
        File AdapterFile = Opts.GetFileOpt(ComLine, "A", null);
        String Prefix = Opts.GetStringOpt(ComLine, "p", InputFile + ".linkerfilter");
        int Thread = Opts.GetIntOpt(ComLine, "t", 1);
        new LinkerFiltering(InputFile, LinkerFile, AdapterFile, Prefix, Thread).Run();
    }

    private void printDistribution() throws IOException {
        PrintWriter fileOut = new PrintWriter(new FileOutputStream(DistributionFile));

        fileOut.println("schoreHist");
        for (int i = 0; i < scoreHist.length; i++) {
            fileOut.println(i + "\t" + scoreHist[i]);
        }

        fileOut.println("\nsecondBestScoreDiffHist");
        for (int i = 0; i < secondBestScoreDiffHist.length; i++) {
            fileOut.println(i + "\t" + secondBestScoreDiffHist[i]);
        }

        fileOut.println("\ntagLengthDistribution");
        for (int i = 0; i < tagLengthDistribution.length; i++) {
            fileOut.println(i + "\t" + tagLengthDistribution[i]);
        }
        fileOut.close();
    }

    private int ReadLinkers() throws IOException {
        this.linkers = ReadLinkers(LinkerFile);
        this.linkerLength = 0;
        for (LinkerSequence linker : linkers) {
            if (this.linkerLength < linker.getSeq().length()) {
                this.linkerLength = linker.getSeq().length();
            }
        }
        return linkers.length;
    }

    public static LinkerSequence[] ReadLinkers(File linkerFile) throws IOException {
        BufferedReader fileIn = new BufferedReader(new InputStreamReader(new FileInputStream(linkerFile)));
        ArrayList<String> tempLinkers = new ArrayList<>();
        String line;
        while ((line = fileIn.readLine()) != null && !line.trim().equals("")) {
            tempLinkers.add(line.trim());
        }
        fileIn.close();
        LinkerSequence[] linkers = new LinkerSequence[tempLinkers.size()];
        for (int i = 0; i < linkers.length; i++) {
            String[] str = tempLinkers.get(i).split("\\s+");
            linkers[i] = new LinkerSequence(str[0], str[1]);
        }
        return linkers;
    }

    private int ReadAdapter() throws IOException {
        Adapters = ReadAdapter(AdapterFile);
        return Adapters.length;
    }

    public static String[] ReadAdapter(File adapterFile) throws IOException {
        BufferedReader adapterfile = new BufferedReader(new FileReader(adapterFile));
        String line;
        ArrayList<String> templist = new ArrayList<>();
        while ((line = adapterfile.readLine()) != null && !line.trim().equals("")) {
            templist.add(line.trim());
        }
        adapterfile.close();
        String[] Adapters = new String[templist.size()];
        for (int i = 0; i < Adapters.length; i++) {
            Adapters[i] = templist.get(i);
            if (Adapters[i].length() < 10) {
                System.err.println("Waring! adapter sequence maybe too short:\t" + Adapters[i]);
            }
        }
        return Adapters;
    }

    private static char[] complTable = new char[255];

    static {
        complTable['A'] = 'T';
        complTable['C'] = 'G';
        complTable['G'] = 'C';
        complTable['T'] = 'A';
        complTable['N'] = 'N';

        complTable['a'] = 't';
        complTable['c'] = 'g';
        complTable['g'] = 'c';
        complTable['t'] = 'a';
        complTable['n'] = 'n';
    }

    private static String revComplement(String seq) {
        StringBuilder result = new StringBuilder(seq);
        result.reverse();
        for (int i = seq.length() - 1; i >= 0; i--) {
            switch (result.charAt(i)) {
                case 'A':
                case 'C':
                case 'G':
                case 'T':
                case 'N':
                case 'a':
                case 'c':
                case 'g':
                case 't':
                case 'n':
                    result.setCharAt(i, complTable[result.charAt(i)]);
                    break;
                default:
                    break;
            }
        }
        return result.toString();
    }

    public File getDistributionFile() {
        return DistributionFile;
    }

    public File getOutFile() {
        return OutFile;
    }

    public void setCutOff(int cutOff) {
        CutOff = cutOff;
    }
}
