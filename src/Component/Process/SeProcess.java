package Component.Process;

import java.io.*;
import java.util.Date;
import java.util.Hashtable;

import Component.File.*;
import Component.tool.Tools;
import Component.unit.Configure;
import Component.unit.Opts;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import Utils.SamFilter;

/**
 * Created by snowf on 2019/2/17.
 */

public class SeProcess {
    //========================================================================
    private FastqFile FastqFile;//Fastq文件
    private File IndexPrefix;//比对索引前缀
    private File GenomeFile;
    private String Prefix = Configure.Prefix;
    private File OutPath = new File("./");//输出路径
    private Opts.FileFormat ReadsType = Opts.FileFormat.Undefine;//reads类型Long or Short
    private int MinQuality;//最小比对质量
    private int MisMatchNum = Configure.AlignMisMatch;//错配数，bwa中使用
    private boolean Iteration = Configure.Iteration;//是否进行迭代比对
    public int Threads = 1;//线程数
    //========================================================================
    private File IterationDir;
    private SamFile SamFile;//Sam文件
    private SamFile UniqSamFile;//唯一比对的Sam文件
    private SamFile UnMapSamFile;//未比对上的Sam文件
    private SamFile MultiSamFile;//多比对文件
    private BedFile BedFile;//Bed文件
    private BedFile SortBedFile;//排序后的bed文件

    //================================================================
    private long RawNum;
    private long UniqueMappedNum;
    private long MultiMappedNum;
    private long UnMappedNum;


    public SeProcess(FastqFile fastqfile, File index, int mismatch, int minquality, File outpath, String prefix, Opts.FileFormat readstype) throws IOException {
        FastqFile = fastqfile;
        IndexPrefix = index;
        MisMatchNum = mismatch;
        MinQuality = minquality;
        OutPath = outpath;
        Prefix = prefix;
        ReadsType = readstype;
        Init();
    }

    SeProcess(String[] args) throws ParseException, IOException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("in").argName("file").hasArg().required().desc("fastq file (require)").build());
        Argument.addOption(Option.builder("index").argName("string").hasArg().desc("genome index (require if don't set 'g')").build());
        Argument.addOption(Option.builder("g").argName("file").hasArg().desc("genome file (require if don't set 'index')").build());
        Argument.addOption(Option.builder("out").argName("dir").hasArg().desc("output directory (default '" + OutPath + "')").build());
        Argument.addOption(Option.builder("p").argName("string").hasArg().desc("output prefix (default '" + Prefix + "')").build());
        Argument.addOption(Option.builder("d").argName("int").hasArg().desc("max edit distance (default '0')").build());
        Argument.addOption(Option.builder("q").argName("int").hasArg().desc("min alignment quality (20 for short reads, 30 for long reads)").build());
        Argument.addOption(Option.builder("r").hasArg(false).desc("Iterative alignment (default 'false')").build());
        Argument.addOption(Option.builder("t").argName("int").hasArg().desc("ThreadNum of alignment (default 1)").build());
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getName() + " " + SeProcess.class.getName(), Argument);
            System.exit(1);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        FastqFile = new FastqFile(ComLine.getOptionValue("in"));
        IndexPrefix = ComLine.hasOption("index") ? new File(ComLine.getOptionValue("index")) : IndexPrefix;
        GenomeFile = ComLine.hasOption("g") ? new File(ComLine.getOptionValue("g")) : GenomeFile;
        OutPath = ComLine.hasOption("out") ? new File(ComLine.getOptionValue("out")) : OutPath;
        Prefix = ComLine.hasOption("p") ? ComLine.getOptionValue("p") : Prefix;
        MisMatchNum = ComLine.hasOption("d") ? Integer.parseInt(ComLine.getOptionValue("d")) : MisMatchNum;
        MinQuality = ComLine.hasOption("q") ? Integer.parseInt(ComLine.getOptionValue("q")) : MinQuality;
        Iteration = ComLine.hasOption("r");
        Threads = ComLine.hasOption("t") ? Integer.parseInt(ComLine.getOptionValue("t")) : Threads;
        Init();
    }

    /**
     * <p>单端数据处理</p>
     * <p>1. 比对</p>
     * <p>2. sam文件过滤</p>
     * <p>3. sam转bed</p>
     * <p>4. bed文件排序</p>
     */
    public void Run() throws IOException, InterruptedException {
        //========================================================================================
        if (IndexPrefix == null) {
            CreateIndex(GenomeFile, GenomeFile, Threads);
        }
        //比对
        Align(FastqFile, SamFile, ReadsType);
        //Sam文件过滤
        SamFilter.Execute(SamFile, UniqSamFile, UnMapSamFile, MultiSamFile, MinQuality, Threads);
        if (Iteration) {
            BufferedReader sam_read = new BufferedReader(new FileReader(UnMapSamFile));
            String Line;
            String[] Str;
            Hashtable<String, char[]> ReadsList = new Hashtable<>();
            while ((Line = sam_read.readLine()) != null) {
                Str = Line.split("\\s+");
                ReadsList.put(Str[0], Str[9].toCharArray());
            }
            sam_read.close();
            SamFile[] TempFile = IterationAlignment(ReadsList, Prefix, 1);
            UniqSamFile.Append(TempFile[0]);
            MultiSamFile.Append(TempFile[1]);
        }
        //Sam文件转bed
        UniqSamFile.ToBedFile(BedFile);
        BedFile.SplitSortFile(SortBedFile);
        System.out.println(new Date() + "\tDelete " + BedFile.getName());
        if (Configure.DeBugLevel < 1) {
            BedFile.delete();
        }
//        Tools.RemoveEmptyFile(OutPath);//不能放在这里
    }

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        SeProcess se = new SeProcess(args);
        se.Run();
    }

    /**
     * <p>类的初始化</p>
     * <p>检测输出路径，判断单端类型（哪一端），构建输出文件</p>
     */
    private void Init() throws IOException {
        //=============================================================================================
        IterationDir = new File(OutPath + "/Iteration");
        synchronized (SeProcess.class) {
            if (!OutPath.isDirectory() && !OutPath.mkdirs()) {
                System.err.println("Can't create " + OutPath);
                System.exit(1);
            }
            if (!IterationDir.isDirectory() && !IterationDir.mkdirs()) {
                System.err.println("Can't create Directory " + IterationDir);
                System.exit(1);
            }
            if (IndexPrefix == null && GenomeFile == null) {
                System.err.println("Error! No Genome file or index!");
                System.exit(1);
            }
        }
        if (ReadsType == Opts.FileFormat.Undefine) {
            ReadsType = FileTool.ReadsType(FastqFile);
            if (ReadsType == Opts.FileFormat.ErrorFormat) {
                System.err.println("Error format");
                System.exit(1);
            }
        }
        switch (ReadsType) {
            case ShortReads:
                MinQuality = 20;
                break;
            case LongReads:
                MinQuality = 30;
                break;
        }
        File FilePrefix = new File(OutPath + "/" + Prefix + "." + MisMatchNum);
        SamFile = new SamFile(FilePrefix.getPath() + ".sam");
        UniqSamFile = new SamFile(FilePrefix + ".uniq.sam");
        UnMapSamFile = new SamFile(FilePrefix + ".unmap.sam");
        MultiSamFile = new SamFile(FilePrefix + ".multi.sam");
        BedFile = new BedFile(FilePrefix + ".bed");
        SortBedFile = new BedFile(FilePrefix + ".sort.bed");
    }

    public static void CreateIndex(File genomeFile, File prefix, int threads) {
        System.out.println("Create index ......");
        String s = "";
        if (Configure.Bwa != null && Configure.Bwa.isValid()) {
            s = Configure.Bwa.index(genomeFile, prefix);
        } else if (Configure.Bowtie != null && !Configure.Bowtie.equals("")) {
            s = Configure.Bowtie + "-build --threads " + threads + " " + genomeFile + " " + prefix;
        } else {
            System.err.println(new Date() + ":[Create Index]\tError! no alignment tools");
            System.exit(1);
        }
        try {
            if (Configure.DeBugLevel < 1) {
                Tools.ExecuteCommandStr(s, null, null);
            } else {
                Tools.ExecuteCommandStr(s, null, new PrintWriter(System.err));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void Align(FastqFile fastqFile, File samFile, Opts.FileFormat ReadsType) throws IOException, InterruptedException {
        //比对
        String CommandStr;
        System.out.println(new Date() + "\tBegin to align\t" + fastqFile.getName());
        if (Configure.Bwa != null && Configure.Bwa.isValid()) {
            if (ReadsType == Opts.FileFormat.ShortReads) {
                File SaiFile = new File(fastqFile + ".sai");
                CommandStr = Configure.Bwa.aln(IndexPrefix, fastqFile, SaiFile, MisMatchNum, Threads);
//                CommandStr = Configure.Bwa + " aln -t " + Threads + " -n " + MisMatchNum + " -f " + SaiFile + " " + IndexPrefix + " " + FastqFile;
                Opts.CommandOutFile.Append(CommandStr + "\n");
                if (Configure.DeBugLevel < 1) {
                    Tools.ExecuteCommandStr(CommandStr);//执行命令行
                } else {
                    Tools.ExecuteCommandStr(CommandStr, new PrintWriter(System.out), new PrintWriter(System.err));//执行命令行
                }
                System.out.println(new Date() + "\tsai to sam\t" + fastqFile.getName());
                CommandStr = Configure.Bwa.samse(samFile, IndexPrefix, SaiFile, fastqFile);
//                CommandStr = Configure.Bwa + " samse -f " + samFile + " " + IndexPrefix + " " + SaiFile + " " + fastqFile;
                Opts.CommandOutFile.Append(CommandStr + "\n");
                if (Configure.DeBugLevel < 1) {
                    Tools.ExecuteCommandStr(CommandStr, null, null);//执行命令行
                } else {
                    Tools.ExecuteCommandStr(CommandStr, new PrintWriter(System.out), new PrintWriter(System.err));//执行命令行
                }
                if (Configure.DeBugLevel < 1) {
                    System.out.println(new Date() + "\tDelete " + SaiFile.getName());
                    SaiFile.delete();//删除sai文件
                }
            } else if (ReadsType == Opts.FileFormat.LongReads) {
                CommandStr = Configure.Bwa.mem(IndexPrefix, fastqFile, Threads);
//                CommandStr = Configure.Bwa + " mem -t " + Threads + " " + IndexPrefix + " " + fastqFile;
                Opts.CommandOutFile.Append(CommandStr + "\n");
                PrintWriter sam = new PrintWriter(samFile);
                if (Configure.DeBugLevel < 1) {
                    Tools.ExecuteCommandStr(CommandStr, sam, null);//执行命令
                } else {
                    Tools.ExecuteCommandStr(CommandStr, sam, new PrintWriter(System.err));//执行命令
                }
                sam.close();
            } else {
                System.err.println("Error reads type:" + ReadsType + " reads type should set Short or Long");
                System.exit(1);
            }
        } else if (Configure.Bowtie != null && !Configure.Bowtie.equals("")) {
            CommandStr = Configure.Bowtie + " " + (fastqFile.FastqPhred() == Opts.FileFormat.Phred33 ? "--phred33" : "--phred64") + " -p " + Threads + " -x " + IndexPrefix + " -U " + fastqFile + " -S " + samFile;
            Opts.CommandOutFile.Append(CommandStr + "\n");
            if (Configure.DeBugLevel < 1) {
                Tools.ExecuteCommandStr(CommandStr, null, null);//执行命令行
            } else {
                Tools.ExecuteCommandStr(CommandStr, new PrintWriter(System.out), new PrintWriter(System.err));//执行命令行
            }
        } else {
            System.err.println(new Date() + ":\tError! No alignment software");
            System.exit(1);
        }
        System.out.println(new Date() + "\tEnd align\t" + fastqFile.getName());
    }

    /**
     * @param ReadsList 序列列表
     * @param Prefix    前缀
     * @param Num       序号
     * @return <>samfile of unique map and multi map</p>
     */
    private SamFile[] IterationAlignment(Hashtable<String, char[]> ReadsList, String Prefix, int Num) throws IOException, InterruptedException {
        System.out.println(new Date() + "\tIteration align start " + Num);
        SamFile UniqSamFile = new SamFile(IterationDir + "/" + Prefix + ".iteration" + Num + ".uniq.sam");
        SamFile UnMapSamFile = new SamFile(IterationDir + "/" + Prefix + ".iteration" + Num + ".unmap.sam");
        SamFile MultiSamFile = new SamFile(IterationDir + "/" + Prefix + ".iteration" + Num + ".multi.sam");
        FastqFile FastaFile = new FastqFile(IterationDir + "/" + Prefix + ".iteration" + Num + ".fasta");
        BufferedWriter fasta_write = new BufferedWriter(new FileWriter(FastaFile));
        String Line;
        String[] Str;
        //------------------------------------------fasta file write----------------------------------------------------
        for (String title : ReadsList.keySet()) {
            char[] Seq = ReadsList.get(title);
            String[] KSeq = Tools.GetKmer(String.valueOf(Seq), Num);
            for (String aKSeq : KSeq) {
                fasta_write.write(">" + title + "\n");
                fasta_write.write(aKSeq + "\n");
            }
        }
        fasta_write.close();
        //--------------------------------------------------------------------------------------------------------------
        File TempSamFile = new File(IterationDir + "/" + Prefix + ".iteration" + Num + ".sam.temp");
        try {
            Align(FastaFile, TempSamFile, ReadsType);// align
            SamFilter.Execute(TempSamFile, UniqSamFile, UnMapSamFile, MultiSamFile, MinQuality, Threads);//filter
        } catch (IOException e) {
            //不知道为什么有时在sam文件过滤时找不到sam文件，这是暂时的解决办法
            System.err.println(e.getMessage());
            FileUtils.touch(UniqSamFile);
            FileUtils.touch(MultiSamFile);
            ReadsList.clear();
            return new SamFile[]{UniqSamFile, MultiSamFile};
        }
        //delete useless file
        if (Configure.DeBugLevel < 1) {
            FastaFile.delete();
            UnMapSamFile.delete();
            TempSamFile.delete();
        }
        //remove uniq and multi map reads name
        BufferedReader sam_reader = new BufferedReader(new FileReader(UniqSamFile));
        while ((Line = sam_reader.readLine()) != null) {
            Str = Line.split("\\s+");
            ReadsList.remove(Str[0]);
        }
        sam_reader.close();
        sam_reader = new BufferedReader(new FileReader(MultiSamFile));
        while ((Line = sam_reader.readLine()) != null) {
            Str = Line.split("\\s+");
            ReadsList.remove(Str[0]);
        }
        sam_reader.close();
        //------------------------------------remove multi unique map-----------------------------------------------------
        UniqSamFile.SortFile(new SamFile(UniqSamFile + ".sort"));
        sam_reader = new BufferedReader(new FileReader(UniqSamFile + ".sort"));
        Hashtable<String, Integer> TempHash = new Hashtable<>();
        while ((Line = sam_reader.readLine()) != null) {
            Str = Line.split("\\s+");
            TempHash.put(Str[0], TempHash.getOrDefault(Str[0], 0) + 1);
        }
        sam_reader = new BufferedReader(new FileReader(UniqSamFile + ".sort"));
        BufferedWriter writer = new BufferedWriter(new FileWriter(UniqSamFile));
        while ((Line = sam_reader.readLine()) != null) {
            Str = Line.split("\\s+");
            if (TempHash.get(Str[0]) == 1) {
                writer.write(Line + "\n");
            }
        }
        TempHash.clear();
        writer.close();
        sam_reader.close();
        new File(UniqSamFile + ".sort").delete();
        //--------------------------------------------------------------------------------------------------------------
        if (ReadsList.keySet().size() == 0) {
            return new SamFile[]{UniqSamFile, MultiSamFile};
        }
        SamFile[] TempFile = IterationAlignment(ReadsList, Prefix, ++Num);
        UniqSamFile.Append(TempFile[0]);
        MultiSamFile.Append(TempFile[1]);
        if (Configure.DeBugLevel < 1) {
            TempFile[0].delete();
            TempFile[1].delete();
        }
        return new SamFile[]{UniqSamFile, MultiSamFile};
    }

    public File getBedFile() {
        return BedFile;
    }

    public SamFile getSamFile() {
        return SamFile;
    }

    public SamFile getUniqSamFile() {
        return UniqSamFile;
    }

    public BedFile getSortBedFile() {
        return SortBedFile;
    }

    public void setIteration(boolean iteration) {
        Iteration = iteration;
    }
}
