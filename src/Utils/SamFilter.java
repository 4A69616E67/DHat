package Utils;

import Component.tool.Tools;
import Component.unit.Opts;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.Date;

public class SamFilter {
    private File InputSamFile, OutPath = new File("./");
    private File UniqSamFile, UnmapSamFile, MultiSamFile;
    private String Prefix = "SamFilter";
    private Integer MinQuality = 20;
    private Integer Threads = 1;

    public SamFilter(File inputSamFile) {
        this(inputSamFile, "SamFilter");
    }

    public SamFilter(File inputSamFile, String prefix) {
        this(inputSamFile, new File("./"), prefix);
    }

    public SamFilter(File inputSamFile, File outPath, String prefix) {
        this(inputSamFile, outPath, prefix, 20);
    }

    public SamFilter(File inputSamFile, File outPath, String prefix, Integer minQuality) {
        this(inputSamFile, outPath, prefix, minQuality, 1);
    }

    public SamFilter(File inputSamFile, File outPath, String prefix, Integer minQuality, Integer threads) {
        InputSamFile = inputSamFile;
        OutPath = outPath;
        Prefix = prefix;
        MinQuality = minQuality;
        Threads = threads;
        Init();
    }

    public SamFilter(String[] args) throws ParseException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("i").hasArg().argName("file").desc("input sam file").required().build());
        Argument.addOption(Option.builder("o").hasArg().argName("path").desc("output path (default " + OutPath + ")").build());
        Argument.addOption(Option.builder("q").hasArg().argName("int").desc("minimum quality value (default " + MinQuality + ")").build());
        Argument.addOption(Option.builder("p").hasArg().argName("string").desc("out prefix (default " + Prefix + ")").build());
        Argument.addOption(Option.builder("t").hasArg().argName("int").desc("process threads (default " + Threads + ")").build());
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getName() + " " + SamFilter.class.getName(), Argument, true);
            System.exit(1);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        InputSamFile = Opts.GetFileOpt(ComLine, "i", null);
        OutPath = Opts.GetFileOpt(ComLine, "o", OutPath);
        Prefix = Opts.GetStringOpt(ComLine, "p", Prefix);
        MinQuality = Opts.GetIntOpt(ComLine, "q", MinQuality);
        Threads = Opts.GetIntOpt(ComLine, "t", Threads);
        Init();

    }

    public static void main(String[] args) throws ParseException, IOException {
        SamFilter samFilter = new SamFilter(args);
        samFilter.Run();
    }

    private void Init() {
        if (InputSamFile == null) {
            System.err.println(SamFilter.class.getName() + "\tError! No Input File");
            System.exit(1);
        }
        if (!OutPath.isDirectory() && !OutPath.mkdir()) {
            System.err.println(SamFilter.class.getName() + "\tError! Can't create output directory:\t" + OutPath);
            System.exit(1);
        }
        UniqSamFile = new File(OutPath + "/" + Prefix + ".uniq.sam");
        UnmapSamFile = new File(OutPath + "/" + Prefix + ".unmap.sam");
        MultiSamFile = new File(OutPath + "/" + Prefix + ".multi.sam");
    }

    public static long[] Execute(File SamFile, File UniqSamFile, File UnMapSamFile, File MultiSamFile, int MinQuality, int Threads) throws IOException {
        long[] Count = new long[]{0, 0, 0};
        BufferedReader sam_read = new BufferedReader(new FileReader(SamFile));
        BufferedWriter uniq_write = new BufferedWriter(new FileWriter(UniqSamFile));
        BufferedWriter unmap_write = new BufferedWriter(new FileWriter(UnMapSamFile));
        BufferedWriter multi_write = new BufferedWriter(new FileWriter(MultiSamFile));
        System.out.println(new Date() + "\tBegin to sam filter\t" + SamFile.getName());
        Thread[] t = new Thread[Threads];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                try {
                    String line;
                    String[] str;
                    while ((line = sam_read.readLine()) != null) {
                        str = line.split("\\s+");
                        if (str[0].matches("^@.+")) {
                            continue;
                        }
                        if (Integer.parseInt(str[4]) >= MinQuality) {
                            synchronized (uniq_write) {
                                Count[0]++;
                                uniq_write.write(line + "\n");
                            }
                        } else if (str[2].equals("*")) {
                            synchronized (unmap_write) {
                                Count[1]++;
                                unmap_write.write(line + "\n");
                            }
                        } else {
                            synchronized (multi_write) {
                                Count[2]++;
                                multi_write.write(line + "\n");
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        sam_read.close();
        uniq_write.close();
        unmap_write.close();
        multi_write.close();
        System.out.println(new Date() + "\tEnd to sam filter\t" + SamFile);
        return Count;
    }

    public void Run() throws IOException {
        Execute(InputSamFile, UniqSamFile, UnmapSamFile, MultiSamFile, MinQuality, Threads);
    }

    public File getMultiSamFile() {
        return MultiSamFile;
    }

    public File getUniqSamFile() {
        return UniqSamFile;
    }

    public File getUnmapSamFile() {
        return UnmapSamFile;
    }
}
