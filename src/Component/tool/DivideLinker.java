package Component.tool;

import Component.unit.Default;
import Component.unit.LinkerSequence;
import Component.unit.Opts;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.Date;
/**
 * Created by snowf on 2019/2/17.
 *
 */
public class DivideLinker {
    public enum Format {
        First, Second, All
    }

    private File PastFile;
    private String Prefix;
    private LinkerSequence[] LinkerList;
    private long[] LinkerCount;
    private File[] R1FastqFile;
    private File[] R2FastqFile;
    private int CutOffLen;
    private int MaxReadsLength;
    private int MinLinkerMappingScore;
    private String Restriction;
    private Opts.FileFormat Phred;
    private Format Type = Format.All;
    private String[] MatchSeq = new String[2];//要匹配的酶切序列
    private String[] AppendSeq = new String[2];//要延长的序列
    private String[] AppendQuality = new String[2];//要延长的质量
    private int Threads = 1;

    public DivideLinker(File past_file, String prefix, LinkerSequence[] linker_list, String restriction, int min_score, Format type) {
        this(past_file, prefix, linker_list, restriction, type, Default.MaxReadsLen, min_score, Opts.FileFormat.Phred33);
    }

    public DivideLinker(File past_file, String prefix, LinkerSequence[] linker_list, String restriction, Format type, int max_reads_length, int min_score, Opts.FileFormat phred) {
        PastFile = past_file;
        Prefix = prefix;
        LinkerList = linker_list;
        Restriction = restriction;
        Type = type;
        MaxReadsLength = max_reads_length;
        MinLinkerMappingScore = min_score;
        Phred = phred;
        Init();
    }

    public static void main(String[] args) throws ParseException, InterruptedException, IOException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("i").hasArg().argName("file").desc("input file").required().build());
        Argument.addOption(Option.builder("p").hasArg().argName("string").desc("prefix").build());
        Argument.addOption(Option.builder("l").hasArgs().argName("strings").desc("linker alias list (such as AA BB)").required().build());
        Argument.addOption(Option.builder("r").hasArg().argName("string").desc("restriction string (such as T^TAA)").build());
        Argument.addOption(Option.builder("m").hasArg().argName("int").desc("minimum linker mapping score").build());
        Argument.addOption(Option.builder("t").hasArg().argName("int").desc("ThreadNum").build());
        if (args.length <= 0) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getName() + " " + DivideLinker.class.getName(), Argument, true);
            System.exit(1);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        File InPutFile = Opts.GetFileOpt(ComLine, "i", null);
        String Prefix = Opts.GetStringOpt(ComLine, "p", "test");
        String[] tempstr = Opts.GetStringOpts(ComLine, "l", null);
        LinkerSequence[] LinkerList = new LinkerSequence[tempstr.length];
        for (int i = 0; i < LinkerList.length; i++) {
            LinkerList[i] = new LinkerSequence("", tempstr[i], true);
        }
        String Restriction = Opts.GetStringOpt(ComLine, "r", "^");
        int MinScore = Opts.GetIntOpt(ComLine, "m", 30);
        int Thread = Opts.GetIntOpt(ComLine, "t", 1);
        DivideLinker div = new DivideLinker(InPutFile, Prefix, LinkerList, Restriction, MinScore, Format.All);
        div.setThreads(Thread);
        div.Run();
    }

    private void Init() {
        R1FastqFile = new File[LinkerList.length];
        R2FastqFile = new File[LinkerList.length];
        LinkerCount = new long[LinkerList.length];
        for (int i = 0; i < LinkerList.length; i++) {
            R1FastqFile[i] = new File(Prefix + "." + LinkerList[i].getType() + ".R1.fastq");
            R2FastqFile[i] = new File(Prefix + "." + LinkerList[i].getType() + ".R2.fastq");
        }
        int[] length = new int[]{Restriction.indexOf("^"), Restriction.replace("^", "").length() - Restriction.indexOf("^")};
        MatchSeq[0] = Restriction.replace("^", "").substring(0, Math.max(length[0], length[1]));
        MatchSeq[1] = Restriction.replace("^", "").substring(Math.min(length[0], length[1]));
        AppendSeq[0] = Restriction.replace("^", "").substring(Math.max(length[0], length[1]));
        AppendSeq[1] = Restriction.replace("^", "").substring(0, Math.min(length[0], length[1]));
        switch (Phred) {
            case Phred33:
                AppendQuality[0] = AppendSeq[0].replaceAll(".", "I");
                AppendQuality[1] = AppendSeq[1].replaceAll(".", "I");
                break;
            case Phred64:
                AppendQuality[0] = AppendSeq[0].replaceAll(".", "h");
                AppendQuality[1] = AppendSeq[1].replaceAll(".", "h");
                break;
            default:
                System.err.println("Error Phred:\t" + Phred);
                System.exit(1);
        }
    }

    public void Run() throws IOException {
        if (!PastFile.isFile()) {
            System.err.println(DivideLinker.class.getName() + ":\tNo such file " + PastFile);
            System.exit(1);
        }
        BufferedReader reader = new BufferedReader(new FileReader(PastFile));
        BufferedWriter[] r1_writer = new BufferedWriter[R1FastqFile.length];//R1 output file list
        BufferedWriter[] r2_writer = new BufferedWriter[R2FastqFile.length];//R2 output file list
        switch (Type) {
            case First://only output R1
                for (int i = 0; i < R1FastqFile.length; i++) {
                    r1_writer[i] = new BufferedWriter(new FileWriter(R1FastqFile[i]));
                }
                break;
            case Second://only output R2
                for (int i = 0; i < R2FastqFile.length; i++) {
                    r2_writer[i] = new BufferedWriter(new FileWriter(R2FastqFile[i]));
                }
                break;
            case All://output R1 and R2
                for (int i = 0; i < R1FastqFile.length; i++) {
                    r1_writer[i] = new BufferedWriter(new FileWriter(R1FastqFile[i]));
                }
                for (int i = 0; i < R2FastqFile.length; i++) {
                    r2_writer[i] = new BufferedWriter(new FileWriter(R2FastqFile[i]));
                }
                break;
            default:
                System.out.println("Error Type:\t" + Type);
                System.exit(1);
        }
        System.out.println(new Date() + "\tBegin to divide linker");
        System.out.print(new Date() + "\tLinker type:\t");
        for (LinkerSequence aLinkerList : LinkerList) {
            System.out.print(aLinkerList.getType() + "\t");
        }
        System.out.println();
        Thread[] Process = new Thread[Threads];//multi-ThreadNum
        for (int i = 0; i < Process.length; i++) {
            Process[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    String Line;
                    String[] Str, OutString;
                    try {
                        while ((Line = reader.readLine()) != null) {
                            Str = Line.split("\\t");
                            for (int j = 0; j < LinkerList.length; j++) {
                                //find out which kind of linker belong to
                                if (Str[5].equals(LinkerList[j].getType()) && Integer.parseInt(Str[6]) >= MinLinkerMappingScore) {
                                    synchronized (LinkerList[j]) {
                                        LinkerCount[j]++;
                                    }
                                    OutString = Parse(Str, MatchSeq, AppendSeq, AppendQuality, MaxReadsLength, Type);
                                    switch (Type) {
                                        case First:
                                            if (OutString[0] != null) {
                                                synchronized (LinkerList[j]) {
                                                    r1_writer[j].write(OutString[0]);
                                                }
                                            }
                                            break;
                                        case Second:
                                            if (OutString[1] != null) {
                                                synchronized (LinkerList[j]) {
                                                    r2_writer[j].write(OutString[1]);
                                                }
                                            }
                                            break;
                                        case All:
                                            if (OutString[0] != null && OutString[1] != null) {
                                                synchronized (LinkerList[j]) {
                                                    r1_writer[j].write(OutString[0]);
                                                    r2_writer[j].write(OutString[1]);
                                                }
                                            }
                                            break;
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            Process[i].start();
        }
        Tools.ThreadsWait(Process);
        switch (Type) {
            case First://only output R1
                for (int i = 0; i < R1FastqFile.length; i++) {
                    r1_writer[i].close();
                }
                break;
            case Second://only output R2
                for (int i = 0; i < R2FastqFile.length; i++) {
                    r2_writer[i].close();
                }
                break;
            case All://output R1 and R2
                for (int i = 0; i < R1FastqFile.length; i++) {
                    r1_writer[i].close();
                }
                for (int i = 0; i < R2FastqFile.length; i++) {
                    r2_writer[i].close();
                }
                break;
        }
        System.out.println(new Date() + "\tDivide " + PastFile + " end");
    }

    public static String[] Parse(String[] items, String[] matchSeq, String[] appendSeq, String[] appendQuality, int maxReadsLength, Format format) {
        String OutString1 = null, OutString2 = null;
        switch (format) {
            case All:
                if (!items[0].equals("*") && !items[3].equals("*")) {
                    OutString1 = ParseFirst(items, matchSeq[0], appendSeq[0], appendQuality[0], maxReadsLength);
                    OutString2 = ParseSecond(items, matchSeq[1], appendSeq[1], appendQuality[1], maxReadsLength);
                }
                break;
            case First:
                OutString1 = ParseFirst(items, matchSeq[0], appendSeq[0], appendQuality[0], maxReadsLength);
                break;
            case Second:
                OutString2 = ParseSecond(items, matchSeq[1], appendSeq[1], appendQuality[1], maxReadsLength);
                break;
        }
        return new String[]{OutString1, OutString2};
    }

    private static String ParseFirst(String[] S, String MatchSeq, String AppendSeq, String AppendQuality, int MaxReadsLength) throws NumberFormatException, IndexOutOfBoundsException {
        if (S[0].equals("*")) {
            return null;
        }
        StringBuilder FastqString = new StringBuilder();
        int EndSite = Integer.parseInt(S[1]) - 1;
        String ReadsTitle = S[7];
        String ReadsSeq = S[8].substring(0, EndSite).replace("N", "");
        String Orientation = S[9];
        String Quality = S[10].substring(EndSite - ReadsSeq.length(), EndSite);
        //---------------------------------------------------
        if (!MatchSeq.equals("") && AppendBase(ReadsSeq, MatchSeq, Format.First)) {
            if (ReadsSeq.length() <= MaxReadsLength + 10) {
                ReadsSeq += AppendSeq;
                Quality += AppendQuality;
            } else {
                ReadsSeq = ReadsSeq.substring(ReadsSeq.length() - MaxReadsLength) + AppendSeq;
                Quality = Quality.substring(Quality.length() - MaxReadsLength) + AppendQuality;
            }
        } else {
            if (ReadsSeq.length() > MaxReadsLength + 10) {
                ReadsSeq = ReadsSeq.substring(ReadsSeq.length() - MaxReadsLength);
                Quality = Quality.substring(Quality.length() - MaxReadsLength);
            }
        }
        //reads title
        FastqString.append(ReadsTitle).append("\n").append(ReadsSeq).append("\n").append(Orientation).append("\n").append(Quality).append("\n");
        return FastqString.toString();
    }

    private static String ParseSecond(String[] S, String MatchSeq, String AppendSeq, String AppendQuality, int MaxReadsLength) throws NumberFormatException, IndexOutOfBoundsException {
        if (S[3].equals("*")) {
            return null;
        }
        StringBuilder FastqString = new StringBuilder();
        int StartSite = Integer.parseInt(S[2]) + 1;
        int EndSite = S[4].equals("*") ? S[8].length() : Integer.parseInt(S[4]) - 1;
        String ReadsTitle = S[7];
        String ReadsSeq = S[8].substring(StartSite - 1, EndSite).replace("N", "");
        String Orientation = S[9];
        String Quality = S[10].substring(StartSite - 1, StartSite - 1 + ReadsSeq.length());
        //------------------------------------------------------
        if (!MatchSeq.equals("") && AppendBase(ReadsSeq, MatchSeq, Format.Second)) {
            if (ReadsSeq.length() <= MaxReadsLength + 10) {
                ReadsSeq = AppendSeq + ReadsSeq;
                Quality = AppendQuality + Quality;
            } else {
                ReadsSeq = AppendSeq + ReadsSeq.substring(0, MaxReadsLength);
                Quality = AppendQuality + Quality.substring(0, MaxReadsLength);
            }
        } else {
            if (ReadsSeq.length() > MaxReadsLength + 10) {
                ReadsSeq = ReadsSeq.substring(0, MaxReadsLength);
                Quality = Quality.substring(0, MaxReadsLength);
            }
        }
        FastqString.append(ReadsTitle).append("\n").append(ReadsSeq).append("\n").append(Orientation).append("\n").append(Quality).append("\n");
        return FastqString.toString();
    }

    public void setThreads(int threads) {
        Threads = threads;
    }

    public long[] getLinkerCount() {
        return LinkerCount;
    }

    public File[] getR1FastqFile() {
        return R1FastqFile;
    }

    public File[] getR2FastqFile() {
        return R2FastqFile;
    }

    public String[] getMatchSeq() {
        return MatchSeq;
    }

    public String[] getAppendSeq() {
        return AppendSeq;
    }

    public String[] getAppendQuality() {
        return AppendQuality;
    }

    private static Boolean AppendBase(String Sequence, String Restriction, Format Type) {
        if (Sequence.length() < Restriction.length()) {
            return false;
        }
        switch (Type) {
            case First:
                return Sequence.substring(Sequence.length() - Restriction.length()).equals(Restriction);
            case Second:
                return Sequence.substring(0, Restriction.length()).equals(Restriction);
            default:
                System.err.println(new Date() + "\tError parameter in  append one base\t" + Type);
                System.exit(1);
                return false;
        }
    }//OK
}
