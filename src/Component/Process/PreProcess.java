package Component.Process;

import Component.File.FastqFile;
import Component.File.FileTool;
import Component.tool.DivideLinker;
import Component.tool.LinkerFiltering;
import Component.tool.Tools;
import Component.unit.*;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.Date;

public class PreProcess extends AbstractProcess {

    private File OutPath = new File("./");//输出目录
    private String Prefix = Configure.Prefix;//输出前缀
    private Component.File.FastqFile FastqFile;//Fastq文件
    private File LinkerFile;//linker文件
    private File AdapterFile;//Adapter文件
    private String Restriction = "^";//酶切序列（未处理）
    private int MaxReadsLen = Configure.MaxReadsLen;
    private float MinAdapterPercent = 0.7f;
    private int MinLinkerMappingScore = 30;
    private int MatchScore = Configure.MatchScore;//匹配分数
    private int MisMatchScore = Configure.MisMatchScore;//错配分数
    private int InDelScore = Configure.InDelScore;//插入缺失分数
    private int Threads = Configure.Thread;//线程数
    private int CutOff = 0;
    //=================================================================
    private long[] LinkerCout;
    private LinkerSequence[] Linkers;
    private String[] Adapters;
    private File LinkerFilterOutFile;
    private FastqFile[] FastqR1File, FastqR2File;
    private String[] MatchSeq = new String[2];//要匹配的酶切序列
    private String[] AppendSeq = new String[2];//要延长的序列
    private String[] AppendQuality = new String[2];//要延长的质量
    private String LinkerFilterOutPrefix;//linker过滤输出前缀


    public PreProcess(String[] args) throws IOException {
        super(PreProcess.class.getName() + "\t" + Thread.currentThread().getName());
        ArgumentInit();
//        Options Argument = new Options();
//        Argument.addOption(Option.builder("i").hasArg().argName("file").required().desc("Input fastq file").build());
//        Argument.addOption(Option.builder("p").hasArg().argName("string").desc("prefix (default " + Prefix + ")").build());
//        Argument.addOption(Option.builder("o").longOpt("out").hasArg().argName("path").desc("Out path (default " + OutPath + ")").build());
//        Argument.addOption(Option.builder("L").longOpt("Linker").hasArgs().argName("file").required().desc("Linker file").build());
//        Argument.addOption(Option.builder("A").longOpt("Adapter").hasArgs().argName("file").desc("Adapter file").build());
//        Argument.addOption(Option.builder("M").longOpt("MatchScore").hasArg().argName("int").desc("Match score (default " + MatchScore + ")").build());
//        Argument.addOption(Option.builder("Mi").longOpt("MisScore").hasArg().argName("int").desc("MisMatch Score (default " + MisMatchScore + ")").build());
//        Argument.addOption(Option.builder("I").longOpt("InDelScore").hasArg().argName("int").desc("Insert and Delete Score (default " + InDelScore + ")").build());
//        Argument.addOption(Option.builder("minc").hasArg().argName("int").desc("minimum linker mapping score (default " + MinLinkerMappingScore + ")").build());
//        Argument.addOption(Option.builder("minp").hasArg().argName("float").desc("minimum adapter mapping percentage (default " + MinAdapterPercent + ")").build());
//        Argument.addOption(Option.builder("maxl").hasArg().argName("int").desc("maximum reads length (default " + MaxReadsLen + ")").build());
//        Argument.addOption(Option.builder("c").hasArg().argName("int").desc("cutoff (default " + CutOff + ")").build());
//        Argument.addOption(Option.builder("r").hasArg().argName("string").desc("restriction").build());
//        Argument.addOption(Option.builder("t").hasArg().argName("int").desc("ThreadNum (default " + Threads + ")").build());
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getName() + " " + PreProcess.class.getName(), Argument, true);
            System.exit(1);
        }
        CommandLine ComLine = null;
        try {
            ComLine = new DefaultParser().parse(Argument, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        FastqFile = new FastqFile(Opts.GetFileOpt(ComLine, "i", FastqFile));
        Prefix = Opts.GetStringOpt(ComLine, "p", Prefix);
        OutPath = Opts.GetFileOpt(ComLine, "o", OutPath);
        LinkerFile = Opts.GetFileOpt(ComLine, "L", LinkerFile);
        AdapterFile = Opts.GetFileOpt(ComLine, "A", AdapterFile);
        MatchScore = Opts.GetIntOpt(ComLine, "M", MatchScore);
        MisMatchScore = Opts.GetIntOpt(ComLine, "Mi", MisMatchScore);
        InDelScore = Opts.GetIntOpt(ComLine, "I", InDelScore);
        MinLinkerMappingScore = Opts.GetIntOpt(ComLine, "minc", MinLinkerMappingScore);
        MinAdapterPercent = Opts.GetFloatOpt(ComLine, "minp", MinAdapterPercent);
        MaxReadsLen = Opts.GetIntOpt(ComLine, "maxl", MaxReadsLen);
        CutOff = Opts.GetIntOpt(ComLine, "c", CutOff);
        Restriction = Opts.GetStringOpt(ComLine, "r", Restriction);
        Threads = Opts.GetIntOpt(ComLine, "t", Threads);
        Init();
    }

    public PreProcess(File outpath, String prefix, FastqFile fastqFile, File linkerfile, File adapterfile, int matchscore, int mismatchscore, int indelscore, int threads) throws IOException {
        super(PreProcess.class.getName() + "\t" + fastqFile.getName());
        OutPath = outpath;
        Prefix = prefix;
        FastqFile = fastqFile;
        LinkerFile = linkerfile;
        AdapterFile = adapterfile;
        MatchScore = matchscore;
        MisMatchScore = mismatchscore;
        InDelScore = indelscore;
        Threads = threads;
        Init();
    }

    public PreProcess(File outPath, String prefix, FastqFile fastqFile, File linkerFile, File adapterFile, String restriction) throws IOException {
        super(PreProcess.class.getName() + "\t" + fastqFile.getName());
        OutPath = outPath;
        Prefix = prefix;
        FastqFile = fastqFile;
        LinkerFile = linkerFile;
        AdapterFile = adapterFile;
        Restriction = restriction;
        Init();
    }

    public static void main(String[] args) throws IOException {
        PreProcess pre = new PreProcess(args);
        pre.run();
    }

    @Override
    public int run() throws IOException {
        StartTime = new Date();
        System.out.println(new Date() + "\tStart to linker filter");
        //=========================================
        long[] Count = new long[]{0};
        BufferedReader infile = new BufferedReader(new FileReader(FastqFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(LinkerFilterOutFile));
        BufferedWriter[] writer1 = new BufferedWriter[Linkers.length];
        BufferedWriter[] writer2 = new BufferedWriter[Linkers.length];
        for (int i = 0; i < Linkers.length; i++) {
            writer1[i] = new BufferedWriter(new FileWriter(FastqR1File[i]));
            writer2[i] = new BufferedWriter(new FileWriter(FastqR2File[i]));
        }
        Thread[] t = new Thread[Threads];
        LocalAlignment[] local = new LocalAlignment[Threads];
        for (int i = 0; i < t.length; i++) {
            int finalI = i;
            t[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    String[] Lines;
                    local[finalI] = new LocalAlignment(MatchScore, MisMatchScore, InDelScore);
                    synchronized (infile) {
                        Lines = FileTool.Read4Line(infile);
                    }
                    while (Lines[3] != null) {
                        String[] filter_result = LinkerFiltering.Execute(Lines[1], local[finalI], Linkers, Adapters, CutOff, MinAdapterPercent);
                        filter_result[7] = Lines[0];
                        filter_result[9] = Lines[2];
                        filter_result[10] = Lines[3];
                        synchronized (writer) {
                            try {
                                writer.write(String.join("\t", filter_result) + "\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        String[] fastq_string;
                        for (int j = 0; j < Linkers.length; j++) {
                            if (filter_result[5].equals(Linkers[j].getType()) && Integer.parseInt(filter_result[6]) >= MinLinkerMappingScore) {
                                synchronized (Linkers[j]) {
                                    LinkerCout[j]++;
                                }
                                fastq_string = DivideLinker.Parse(filter_result, MatchSeq, AppendSeq, AppendQuality, MaxReadsLen, DivideLinker.Format.All);
                                if (fastq_string[0] != null && fastq_string[1] != null) {
                                    synchronized (Linkers[j]) {
                                        try {
                                            writer1[j].write(fastq_string[0]);
                                            writer2[j].write(fastq_string[1]);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                break;
                            }
                        }
                        synchronized (infile) {
                            Count[0]++;
                            if (Count[0] % 1000000 == 0) {
                                System.out.println(new Date() + "\t" + (Count[0] / 1000000) + " Million reads processed");
                            }
                            Lines = FileTool.Read4Line(infile);
                        }
                    }
                    FastqFile.ItemNum = Count[0];
                    for (int j = 0; j < Linkers.length; j++) {
                        FastqR1File[j].ItemNum = LinkerCout[j];
                        FastqR2File[j].ItemNum = LinkerCout[j];
                    }
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        writer.close();
        for (int i = 0; i < Linkers.length; i++) {
            writer1[i].close();
            writer2[i].close();
        }
        System.out.println(new Date() + "\t" + Count[0] + " reads processed in total.");
        EndTime = new Date();
        return 0;
    }

    @Override
    protected void Init() throws IOException {
        //===================================================================
        if (!FastqFile.isFile()) {
            System.err.println("Error! " + FastqFile + " is not a file");
            System.exit(1);
        }
        if (!LinkerFile.isFile()) {
            System.err.println("Error! " + LinkerFile + " is not a file");
            System.exit(1);
        }
        if (!OutPath.isDirectory()) {
            if (!OutPath.mkdir()) {
                System.err.println("Can't create " + OutPath);
                System.exit(1);
            }
        }
        if (Linkers == null) {
            Linkers = LinkerFiltering.ReadLinkers(LinkerFile);
        }
        FastqR1File = new FastqFile[Linkers.length];
        FastqR2File = new FastqFile[Linkers.length];
        LinkerCout = new long[Linkers.length];
        for (int i = 0; i < Linkers.length; i++) {
            FastqR1File[i] = new FastqFile(OutPath + "/" + Prefix + "." + Linkers[i].getType() + ".R1.fastq");
            FastqR2File[i] = new FastqFile(OutPath + "/" + Prefix + "." + Linkers[i].getType() + ".R2.fastq");
        }
        if (Adapters == null) {
            Adapters = LinkerFiltering.ReadAdapter(AdapterFile);
            for (int i = 0; i < Adapters.length; i++) {
                Adapters[i] = Adapters[i].substring(0, 30);
            }
        }
        LinkerFilterOutPrefix = OutPath + "/" + Prefix + ".linkerfilter";
        LinkerFilterOutFile = getPastFile();
        DivideLinker div = new DivideLinker(LinkerFilterOutFile, Prefix, Linkers, Restriction, DivideLinker.Format.All, MaxReadsLen, 30, new FastqFile(FastqFile).FastqPhred());
        this.MatchSeq = div.getMatchSeq();
        this.AppendSeq = div.getAppendSeq();
        this.AppendQuality = div.getAppendQuality();
    }

    @Override
    protected void ArgumentInit() {
        Argument.addOption(Option.builder("i").hasArg().argName("file").required().desc("Input fastq file").build());
        Argument.addOption(Option.builder("p").hasArg().argName("string").desc("prefix (default " + Prefix + ")").build());
        Argument.addOption(Option.builder("o").longOpt("out").hasArg().argName("path").desc("Out path (default " + OutPath + ")").build());
        Argument.addOption(Option.builder("L").longOpt("Linker").hasArgs().argName("file").required().desc("Linker file").build());
        Argument.addOption(Option.builder("A").longOpt("Adapter").hasArgs().argName("file").desc("Adapter file").build());
        Argument.addOption(Option.builder("M").longOpt("MatchScore").hasArg().argName("int").desc("Match score (default " + MatchScore + ")").build());
        Argument.addOption(Option.builder("Mi").longOpt("MisScore").hasArg().argName("int").desc("MisMatch Score (default " + MisMatchScore + ")").build());
        Argument.addOption(Option.builder("I").longOpt("InDelScore").hasArg().argName("int").desc("Insert and Delete Score (default " + InDelScore + ")").build());
        Argument.addOption(Option.builder("minc").hasArg().argName("int").desc("minimum linker mapping score (default " + MinLinkerMappingScore + ")").build());
        Argument.addOption(Option.builder("minp").hasArg().argName("float").desc("minimum adapter mapping percentage (default " + MinAdapterPercent + ")").build());
        Argument.addOption(Option.builder("maxl").hasArg().argName("int").desc("maximum reads length (default " + MaxReadsLen + ")").build());
        Argument.addOption(Option.builder("c").hasArg().argName("int").desc("cutoff (default " + CutOff + ")").build());
        Argument.addOption(Option.builder("r").hasArg().argName("string").desc("restriction").build());
        Argument.addOption(Option.builder("t").hasArg().argName("int").desc("ThreadNum (default " + Threads + ")").build());
    }

    private File getPastFile() throws IOException {
        return new LinkerFiltering(FastqFile, LinkerFile, LinkerFilterOutPrefix, 1).getOutFile();
    }

    public File getLinkerFilterOutFile() {
        return LinkerFilterOutFile;
    }

    public void setMinLinkerMappingScore(int minLinkerMappingScore) {
        MinLinkerMappingScore = minLinkerMappingScore;
    }

    public void setMinAdapterPercent(float minAdapterPercent) {
        MinAdapterPercent = minAdapterPercent;
    }

    public void setCutOff(int cutOff) {
        CutOff = cutOff;
    }

    public FastqFile[] getFastqR1File() {
        return FastqR1File;
    }

    public FastqFile[] getFastqR2File() {
        return FastqR2File;
    }

    public long[] getLinkerCout() {
        return LinkerCout;
    }
}
