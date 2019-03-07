import java.io.*;
//import java.lang.management.ManagementFactory;
//import java.lang.management.MemoryUsage;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.*;

import Bin.*;
import Component.File.*;
import Component.Process.BedpeProcess;
import Component.Process.PreProcess;
import Component.Process.SeProcess;
import Component.tool.*;
import Component.tool.FindRestrictionSite;
import Component.unit.*;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import script.PetCluster;

/**
 * Created by snowf on 2019/2/17.
 */

public class Main {

    //===================================================================
    private Chromosome[] Chromosomes;
    private LinkerSequence[] LinkerSeq, ValidLinkerSeq;
    private String LinkerA, LinkerB;
    private String Restriction;
    private String Prefix;
    private FastqFile InputFile;
    private File GenomeFile;
    private File LinkerFile;
    private File AdapterFile;
    private String[] AdapterSeq;
    private File IndexPrefix;
    private int MatchScore, MisMatchScore, InDelScore;
    private Opts.FileFormat ReadsType;
    private int AlignMisMatch;
    private int MinUniqueScore;
    private int MaxReadsLength;
    private int[] Resolution, DrawResolution;
    private int LinkerLength;
    private boolean Iteration = true;
    private int Threads;
    private ArrayList<String> Step = new ArrayList<>();
    private ArrayList<Thread> SThread = new ArrayList<>();//统计线程队列
    //===================================================================
    private int MinLinkerFilterQuality;
    private File EnzyPath;//酶切位点文件目录
    private String EnzyFilePrefix;//酶切位点文件前缀
    private CommonFile[] ChrEnzyFile;//每条染色体的酶切位点位置文件
    private File PreProcessDir;//预处理输出目录
    private File SeProcessDir;//单端处理输出目录
    private File BedpeProcessDir;//bedpe处理输出目录
    private File MakeMatrixDir;//建立矩阵输出目录
    //    private File TransLocationDir;//染色体易位输出目录
    private File ReportDir;//生成报告目录
    private Report Stat;
    private File OutPath;
    private int DeBugLevel;
    private String Bwa = Configure.Bwa;
    private String Python = Configure.Python;

    private Main(String[] args) throws IOException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("i").hasArg().argName("file").desc("input file").build());//输入文件
        Argument.addOption(Option.builder("conf").hasArg().argName("file").desc("Configure file").build());//配置文件
        Argument.addOption(Option.builder("p").hasArg().argName("string").desc("Prefix").build());//输出前缀(不需要包括路径)
        Argument.addOption(Option.builder("adv").hasArg().argName("file").desc("Advanced configure file").build());//高级配置文件(一般不用修改)
        Argument.addOption(Option.builder("o").longOpt("out").hasArg().argName("dir").desc("Out put directory").build());//输出路径
        Argument.addOption(Option.builder("r").longOpt("res").hasArgs().argName("ints").desc("resolution").build());//分辨率
        Argument.addOption(Option.builder("s").longOpt("step").hasArgs().argName("string").desc("same as \"Step\" in configure file").build());//运行步骤
        Argument.addOption(Option.builder("t").longOpt("thread").hasArg().argName("int").desc("number of threads").build());//线程数
        Argument.addOption(Option.builder("D").longOpt("Debug").hasArg().argName("int").desc("Debug Level (default " + DeBugLevel + ")").build());
        Argument.addOption(Option.builder("pbs").hasArg(false).desc("running bug pbs").build());
        final String helpHeader = "Version: " + Opts.Version + "\nAuthor: " + Opts.Author + "\nContact: " + Opts.Email;
        final String helpFooter = "Note: use \"java -jar " + Opts.JarFile.getName() + " install\" when you first use!\n      JVM can get " + String.format("%.2f", Opts.MaxMemory / Math.pow(10, 9)) + "G memory";
        if (args.length == 0) {
            //没有参数时打印帮助信息
            new HelpFormatter().printHelp("java -jar Path/" + Opts.JarFile.getName(), helpHeader, Argument, helpFooter, true);
            System.exit(1);
        }
        CommandLine ComLine = null;
        try {
            ComLine = new DefaultParser().parse(Argument, args);
        } catch (ParseException e) {
            //缺少参数时打印帮助信息
            System.err.println(e.getMessage());
            new HelpFormatter().printHelp("java -jar Path/" + Opts.JarFile.getName(), helpHeader, Argument, helpFooter, true);
            System.exit(1);
        }
        //获取配置文件和高级配置文件
        File ConfigureFile = ComLine.hasOption("conf") ? new File(ComLine.getOptionValue("conf")) : Opts.ConfigFile;
        File AdvConfigFile = ComLine.hasOption("adv") ? new File(ComLine.getOptionValue("adv")) : Opts.AdvConfigFile;
        Configure.GetOption(ConfigureFile, AdvConfigFile);//读取配置信息
        //获取命令行参数信息
        if (ComLine.hasOption("i"))
            Configure.InputFile = new FastqFile(ComLine.getOptionValue("i"));
        if (ComLine.hasOption("p"))
            Configure.Prefix = ComLine.getOptionValue("p");
        if (ComLine.hasOption("o"))
            Configure.OutPath = new File(ComLine.getOptionValue("o"));
        if (ComLine.hasOption("s"))
            Configure.Step = String.join(" ", ComLine.getOptionValues("s"));
        if (ComLine.hasOption("t"))
            Configure.Thread = Integer.parseInt(ComLine.getOptionValue("t"));
        if (ComLine.hasOption("r"))
            Configure.Resolution = StringArrays.toInteger(ComLine.getOptionValues("r"));
        if (ComLine.hasOption("D"))
            Configure.DeBugLevel = Integer.parseInt(ComLine.getOptionValue("D"));
        if (ComLine.hasOption("pbs")) {
            try {
                String comline = "java -jar " + Opts.JarFile + " " + String.join(" ", args).replace("-pbs", "");
                CommonFile PbsFile = new CommonFile("SubmitScript.sh");
                if (!PbsFile.clean()) {
                    System.err.println("can't clean " + PbsFile.getName());
                    System.exit(1);
                }
                PbsFile.Append(comline);
                Tools.ExecuteCommandStr("qsub -d ./ -l nodes=1:ppn=" + Configure.Thread + " -N " + Configure.Prefix + " " + PbsFile, new PrintWriter(System.out), new PrintWriter(System.err));
                System.exit(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Init();//变量初始化
    }

    public Main() throws IOException {
        Init();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        //==============================================测试区==========================================================

//        MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
//        long maxMemorySize = memoryUsage.getMax();
//        long usedMemorySize = memoryUsage.getUsed();

        //================================================初始化========================================================
        if (args.length >= 1 && args[0].equals("install")) {
            if (!Opts.ResourceDir.isDirectory() & !Opts.ResourceDir.mkdir())
                System.err.println(new Date() + ":\tCan't Create " + Opts.ResourceDir.getName());
            if (!Opts.ScriptDir.isDirectory() & !Opts.ScriptDir.mkdir())
                System.err.println(new Date() + ":\tCan't Create " + Opts.ScriptDir.getName());
            for (String f : Opts.ScriptFile) {
                System.out.println("Extract " + Opts.ScriptDir + "/" + f);
                FileTool.ExtractFile("/Archive/" + f, new File(Opts.ScriptDir + "/" + f));
            }
            for (String f : Opts.ResourceFile) {
                System.out.println("Extract " + Opts.ResourceDir + "/" + f);
                FileTool.ExtractFile("/resource/" + f, new File(Opts.ResourceDir + "/" + f));
            }
            System.out.println("Extract " + Opts.JarFile.getParent() + "/ReadMe.txt");
            FileTool.ExtractFile(Opts.ReadMeFile.getPath(), new File(Opts.JarFile.getParent() + "/ReadMe.txt"));
            System.out.println("Install finish!");
            System.exit(0);
        }
        //==============================================================================================================
        Main main = new Main(args);
        main.ShowParameter();//显示参数
        main.Run();
    }

    public void Run() throws IOException, InterruptedException {
        //============================================print system information==========================================
        System.out.println("===============Welcome to use " + Opts.JarFile.getName() + "===================");
        System.out.println("Version:\t" + Opts.Version);
        System.out.println("Author:\t" + Opts.Author);
        System.out.println("Max Memory:\t" + String.format("%.2f", Opts.MaxMemory / Math.pow(10, 9)) + "G");
        System.out.println("-------------------------------------------------------------------------------");
        //===========================================初始化输出文件======================================================
        FastqFile[] LinkerFastqFileR1, UseLinkerFasqFileR1 = new FastqFile[ValidLinkerSeq.length];
        FastqFile[] LinkerFastqFileR2, UseLinkerFasqFileR2 = new FastqFile[ValidLinkerSeq.length];
        //==============================================================================================================
        Stat.RunTime.StartTime = DateFormat.getDateTimeInstance().format(new Date());
        Stat.ComInfor.HalfLinkerA = LinkerA;
        Stat.ComInfor.HalfLinkerB = LinkerB;
        Stat.ComInfor.MatchScore = MatchScore;
        Stat.ComInfor.MisMatchScore = MisMatchScore;
        Stat.ComInfor.InDelScore = InDelScore;
        Stat.ComInfor.Restriction = Restriction;
        Stat.ComInfor.IndexPrefix = IndexPrefix;
        Stat.MinUniqueScore = MinUniqueScore;
        Stat.ComInfor.MaxReadsLen = MaxReadsLength;
        Stat.ComInfor.Resolution = Resolution;
        Stat.ComInfor.Thread = Threads;
        Stat.LinkerInit(LinkerSeq.length);
        Stat.LinkerClassInit(ValidLinkerSeq.length);
        Stat.PreDir = PreProcessDir;
        Stat.SeDir = SeProcessDir;
        Stat.BedpeDir = BedpeProcessDir;
        Stat.MatrixDir = MakeMatrixDir;
        for (Component.unit.Chromosome Chromosome : Chromosomes) {
            Stat.Chromosome.add(Chromosome.Name);
        }
        for (int i = 0; i < ValidLinkerSeq.length; i++) {
            Stat.UseLinker[i].LinkerType = ValidLinkerSeq[i].getType();
            Stat.UseLinker[i].SeProcessOutDir = SeProcessDir;
        }
        //==============================================================================================================
        AbstractFile.delete(Opts.CommandOutFile);
        AbstractFile.delete(Opts.StatisticFile);
        Thread ST;//统计线程
        Thread[] STS;//统计线程
        //=========================================linker filter==linker 过滤===========================================
        Date preTime = new Date();
        //-----------------------------------Adapter序列处理------------------------------
        if (AdapterSeq != null) {
            //若Adapter序列不为空
            if (AdapterSeq[0].compareToIgnoreCase("auto") == 0) {
                //标记为自动识别Adapter
                AdapterSeq = new String[1];
                AdapterSeq[0] = InputFile.AdapterDetect(new File(PreProcessDir + "/" + Prefix), LinkerLength + MaxReadsLength);
                System.out.println(new Date() + "\tDetected adapter seq:\t" + AdapterSeq[0]);
            }
            //将Adapter序列输出到文件中
            FileUtils.write(AdapterFile, String.join("\n", AdapterSeq), StandardCharsets.UTF_8);
            Stat.AdapterSequence = String.join(" ", AdapterSeq);
        }
        //---------------------------------保存linker序列--------------------------------
        FileUtils.touch(LinkerFile);
        for (LinkerSequence linkerSeq : LinkerSeq) {
            FileUtils.write(LinkerFile, linkerSeq.getSeq() + "\t" + linkerSeq.getType() + "\n", StandardCharsets.UTF_8, true);
        }
        //-----------------------------------------------------------------------------
        PreProcess preprocess = new PreProcess(PreProcessDir, Prefix, InputFile, LinkerFile, AdapterFile, Restriction);
        preprocess.setMinLinkerMappingScore(MinLinkerFilterQuality);
        Opts.LFStat.Threshold = MinLinkerFilterQuality;
        if (StepCheck(Opts.Step.PreProcess.toString())) {
            preprocess.run();//运行预处理部分
            //----------------------------------------------------------------------
            Stat.RawDataReadsNum = InputFile.getItemNum();
            Opts.StatisticFile.Append(InputFile.getName() + ":\t" + new DecimalFormat("#,###").format(InputFile.ItemNum) + "\n");
            Opts.StatisticFile.Append(Opts.LFStat.Show() + "\n");
            for (int i = 0; i < LinkerSeq.length; i++) {
                Stat.Linkers[i].Name = LinkerSeq[i].getType();
                Stat.Linkers[i].Num = Opts.LFStat.LinkerMatchableNum[i];
            }
        }
        File PastFile = preprocess.getLinkerFilterOutFile();//获取past文件位置
        //=================================================统计信息=====================================================
        ST = new Thread(() -> {
            try {
                CommonFile LinkerDisFile = new CommonFile(Stat.getDataDir() + "/LinkerScoreDis.data");
                Opts.LFStat.WriteLinkerScoreDis(LinkerDisFile);
                Configure.LinkerScoreDisPng = new File(Stat.getImageDir() + "/" + LinkerDisFile.getName().replace(".data", ".png"));
                String ComLine = Python + " " + Opts.StatisticPlotFile + " -i " + LinkerDisFile + " -t bar -o " + Configure.LinkerScoreDisPng;
                Opts.CommandOutFile.Append(ComLine + "\n");
                Tools.ExecuteCommandStr(ComLine, null, new PrintWriter(System.err));
                CommonFile[] ReadsLenDisFile = new CommonFile[LinkerSeq.length];
                Stat.ReadsLengthDisBase64 = new String[LinkerSeq.length];
                for (int i = 0; i < ReadsLenDisFile.length; i++) {
                    ReadsLenDisFile[i] = new CommonFile(Stat.getDataDir() + "/" + Prefix + "." + LinkerSeq[i].getType() + ".reads_length_distribution.data");
                }
                Opts.LFStat.WriteReadsLengthDis(ReadsLenDisFile);
                for (int i = 0; i < ReadsLenDisFile.length; i++) {
                    File PngFile = new File(Stat.getImageDir() + "/" + ReadsLenDisFile[i].getName().replace(".data", ".png"));
                    ComLine = Python + " " + Opts.StatisticPlotFile + " -t bar -y Count --title " + LinkerSeq[i].getType() + " -i " + ReadsLenDisFile[i] + " -o " + PngFile;
                    Opts.CommandOutFile.Append(ComLine + "\n");
                    Tools.ExecuteCommandStr(ComLine, null, new PrintWriter(System.err));
                    Stat.ReadsLengthDisBase64[i] = Stat.GetBase64(PngFile);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        ST.start();
        SThread.add(ST);
        //==============================================================================================================
        LinkerFastqFileR1 = preprocess.getFastqR1File();
        LinkerFastqFileR2 = preprocess.getFastqR2File();
        Stat.ReadsLengthDisBase64 = new String[LinkerSeq.length];
        //==============================================================================================================
        for (int i = 0; i < ValidLinkerSeq.length; i++) {
            for (int j = 0; j < LinkerSeq.length; j++) {
                if (LinkerSeq[j].getType().equals(ValidLinkerSeq[i].getType())) {
                    UseLinkerFasqFileR1[i] = LinkerFastqFileR1[j];
                    UseLinkerFasqFileR2[i] = LinkerFastqFileR2[j];
                    Stat.UseLinker[i].FastqFileR1 = UseLinkerFasqFileR1[i];
                    Stat.UseLinker[i].FastqFileR2 = UseLinkerFasqFileR2[i];
                }
            }
        }
        //=======================================Se Process===单端处理==================================================
        Date seTime = new Date();
        System.err.println("Linker filter: " + preTime + " - " + seTime);
        //--------------------------------------------------------------------------------------------------------------
        for (int i = 0; i < ValidLinkerSeq.length; i++) {
            if (StepCheck(Opts.Step.SeProcess.toString())) {
                System.out.println(new Date() + "\tStart SeProcess");
                //==========================================Create Index========================================================
                if (IndexPrefix == null || IndexPrefix.getName().equals("")) {
                    CreateIndex(GenomeFile);
                }
                SeProcess(UseLinkerFasqFileR1[i], UseLinkerFasqFileR1[i].getName().replace(".fastq", ""));
                SeProcess(UseLinkerFasqFileR2[i], UseLinkerFasqFileR2[i].getName().replace(".fastq", ""));
            }
        }
        //=============================================获取排序好的bed文件===============================================
        BedFile[] R1SortBedFile = new BedFile[ValidLinkerSeq.length];
        BedFile[] R2SortBedFile = new BedFile[ValidLinkerSeq.length];
        BedpeFile[] SeBedpeFile = new BedpeFile[ValidLinkerSeq.length];
        for (int i = 0; i < ValidLinkerSeq.length; i++) {
            R1SortBedFile[i] = new SeProcess(UseLinkerFasqFileR1[i], IndexPrefix, AlignMisMatch, MinUniqueScore, SeProcessDir, UseLinkerFasqFileR1[i].getName().replace(".fastq", ""), ReadsType).getSortBedFile();
            R2SortBedFile[i] = new SeProcess(UseLinkerFasqFileR2[i], IndexPrefix, AlignMisMatch, MinUniqueScore, SeProcessDir, UseLinkerFasqFileR2[i].getName().replace(".fastq", ""), ReadsType).getSortBedFile();
            SeBedpeFile[i] = new BedpeFile(SeProcessDir + "/" + Prefix + "." + ValidLinkerSeq[i].getType() + ".bedpe");
            if (StepCheck(Opts.Step.Bed2BedPe.toString())) {
                System.out.println(new Date() + ":\t" + R1SortBedFile[i].getName() + " " + R2SortBedFile[i].getName() + " to " + SeBedpeFile[i].getName());
                SeBedpeFile[i].BedToBedpe(R1SortBedFile[i], R2SortBedFile[i]);//合并左右端bed文件，输出bedpe文件
            }
            //==========================================================================================================
            Stat.UseLinker[i].UniqMapFileR1 = R1SortBedFile[i];
            Stat.UseLinker[i].UniqMapFileR2 = R2SortBedFile[i];
            Stat.UseLinker[i].RawBedpeFile = SeBedpeFile[i];
            int finalI = i;
            ST = new Thread(() -> {
                try {
                    Stat.UseLinker[finalI].FastqNumR1 = (double) Stat.UseLinker[finalI].FastqFileR1.getItemNum();
                    Opts.StatisticFile.Append(Stat.UseLinker[finalI].FastqFileR1.getName() + ":\t" + new DecimalFormat("#,###").format(Stat.UseLinker[finalI].FastqNumR1) + "\n");
                    Stat.UseLinker[finalI].FastqNumR2 = (double) Stat.UseLinker[finalI].FastqFileR2.getItemNum();
                    Opts.StatisticFile.Append(Stat.UseLinker[finalI].FastqFileR2.getName() + ":\t" + new DecimalFormat("#,###").format(Stat.UseLinker[finalI].FastqNumR2) + "\n");
                    Stat.UseLinker[finalI].UniqMapNumR1 = Stat.UseLinker[finalI].UniqMapFileR1.getItemNum();
                    Opts.StatisticFile.Append(Stat.UseLinker[finalI].UniqMapFileR1.getName() + ":\t" + new DecimalFormat("#,###").format(Stat.UseLinker[finalI].UniqMapNumR1) + "\n");
                    Stat.UseLinker[finalI].UniqMapNumR2 = Stat.UseLinker[finalI].UniqMapFileR2.getItemNum();
                    Opts.StatisticFile.Append(Stat.UseLinker[finalI].UniqMapFileR2.getName() + ":\t" + new DecimalFormat("#,###").format(Stat.UseLinker[finalI].UniqMapNumR2) + "\n");
                    Stat.UseLinker[finalI].RawBedpeNum = Stat.UseLinker[finalI].RawBedpeFile.getItemNum();
                    Opts.StatisticFile.Append(Stat.UseLinker[finalI].RawBedpeFile.getName() + ":\t" + new DecimalFormat("#,###").format(Stat.UseLinker[finalI].RawBedpeNum) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            ST.start();
            SThread.add(ST);
        }
        //=======================================bedpe process init=====================================================
        BedpeFile FinalBedpeFile = new BedpeFile(BedpeProcessDir + "/" + Prefix + ".clean.bedpe");
        BedpeFile SameBedpeFile = new BedpeFile(BedpeProcessDir + "/" + Prefix + ".same.clean.bedpe");
        BedpeFile DiffBedpeFile = new BedpeFile(BedpeProcessDir + "/" + Prefix + ".diff.clean.bedpe");
        BedpeFile[] ChrBedpeFile = new BedpeFile[Chromosomes.length];
        BedpeFile[][] LinkerChrSameCleanBedpeFile = new BedpeFile[ValidLinkerSeq.length][];
        BedpeFile[] LinkerFinalSameCleanBedpeFile = new BedpeFile[ValidLinkerSeq.length];
        BedpeFile[] LinkerFinalDiffCleanBedpeFile = new BedpeFile[ValidLinkerSeq.length];
        BedpeFile[] FinalLinkerBedpe = new BedpeFile[ValidLinkerSeq.length];//有效的bedpe文件,每种linker一个文件
        for (int i = 0; i < Chromosomes.length; i++) {
            ChrBedpeFile[i] = new BedpeFile(BedpeProcessDir + "/" + Prefix + "." + Chromosomes[i].Name + ".same.clean.bedpe");
        }
        BedpeFile InterBedpeFile = new BedpeFile(BedpeProcessDir + "/" + Prefix + ".inter.clean.bedpe");
        Date bedpeTime = new Date();
        System.err.println("Alignment: " + seTime + " - " + bedpeTime);
        Thread findenzy = FindRestrictionFragment();
        if (StepCheck(Opts.Step.BedPeProcess.toString())) {
            //==========================================获取酶切片段和染色体大小=============================================
            findenzy.start();
            findenzy.join();
            //==============================================BedpeFile Process====bedpe 处理=================================
            Thread[] LinkerProcess = new Thread[ValidLinkerSeq.length];//不同linker类型并行
            for (int i = 0; i < LinkerProcess.length; i++) {
                LinkerProcess[i] = BedpeProcess(ValidLinkerSeq[i].getType(), SeBedpeFile[i], Math.max(1, Threads / ValidLinkerSeq.length));
                LinkerProcess[i].start();
                BedpeProcess Temp = new BedpeProcess(new File(BedpeProcessDir + "/" + ValidLinkerSeq[i].getType()), Prefix + "." + ValidLinkerSeq[i].getType(), Chromosomes, ChrEnzyFile, SeBedpeFile[i]);
                FinalLinkerBedpe[i] = Temp.getFinalFile();
                LinkerFinalSameCleanBedpeFile[i] = Temp.getSameNoDumpFile();
                LinkerFinalDiffCleanBedpeFile[i] = Temp.getDiffNoDumpFile();
                LinkerChrSameCleanBedpeFile[i] = Temp.getChrSameNoDumpFile();
            }
            Tools.ThreadsWait(LinkerProcess);
            Thread t1 = new Thread(() -> {
                try {
                    SameBedpeFile.Merge(LinkerFinalSameCleanBedpeFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t1.start();
            Thread t2 = new Thread(() -> {
                try {
                    DiffBedpeFile.Merge(LinkerFinalDiffCleanBedpeFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t2.start();
            Thread t3 = new Thread(() -> {
                try {
                    FinalBedpeFile.Merge(FinalLinkerBedpe);//合并不同linker类型的bedpe文件
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t3.start();
            //合并不同linker的染色体内的交互，作为构建矩阵的输入文件
            Thread t4 = new Thread(() -> {
                try {
                    for (int i = 0; i < Chromosomes.length; i++) {
                        for (int j = 0; j < ValidLinkerSeq.length; j++) {
                            ChrBedpeFile[i].Append(LinkerChrSameCleanBedpeFile[j][i]);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t4.start();
            t1.join();
            t2.join();
            t3.join();
            t4.join();
            //==============================================================================================================
        }
        STS = new Thread[ValidLinkerSeq.length];
        for (int i = 0; i < ValidLinkerSeq.length; i++) {
            int finalI = i;
            STS[i] = new Thread(() -> {
                BedpeProcess Temp = new BedpeProcess(new File(BedpeProcessDir + "/" + ValidLinkerSeq[finalI].getType()), Prefix + "." + ValidLinkerSeq[finalI].getType(), Chromosomes, ChrEnzyFile, SeBedpeFile[finalI]);
                Stat.UseLinker[finalI].BedpeProcessOutDir = new File(BedpeProcessDir + "/" + ValidLinkerSeq[finalI].getType());
                Stat.UseLinker[finalI].SelfLigationFile = Temp.getSelfLigationFile();
                Stat.UseLinker[finalI].RelLigationFile = Temp.getReLigationFile();
                Stat.UseLinker[finalI].SameValidFile = Temp.getValidFile();
                Stat.UseLinker[finalI].RawSameBedpeFile = Temp.getSameFile();
                Stat.UseLinker[finalI].RawDiffBedpeFile = Temp.getDiffFile();
                Stat.UseLinker[finalI].SameCleanFile = Temp.getSameNoDumpFile();
                Stat.UseLinker[finalI].DiffCleanFile = Temp.getDiffNoDumpFile();
                Stat.UseLinker[finalI].MergeCleanFile = Temp.getFinalFile();
                Stat.UseLinker[finalI].SelfLigationNum = Temp.getSelfLigationFile().getItemNum();
                Stat.UseLinker[finalI].RelLigationNum = Temp.getReLigationFile().getItemNum();
                Stat.UseLinker[finalI].SameValidNum = Temp.getValidFile().getItemNum();
                Stat.UseLinker[finalI].RawSameBedpeNum = Stat.UseLinker[finalI].SelfLigationNum + Stat.UseLinker[finalI].RelLigationNum + Stat.UseLinker[finalI].SameValidNum;
                Stat.UseLinker[finalI].RawDiffBedpeNum = Temp.getDiffFile().getItemNum();
                Stat.UseLinker[finalI].SameCleanNum = Temp.getSameNoDumpFile().getItemNum();
                Stat.UseLinker[finalI].DiffCleanNum = Temp.getDiffNoDumpFile().getItemNum();
                try {
                    Opts.StatisticFile.Append(Stat.UseLinker[finalI].SelfLigationFile.getName() + ":\t" + new DecimalFormat("#,###").format(Stat.UseLinker[finalI].SelfLigationNum) + "\n");
                    Opts.StatisticFile.Append(Stat.UseLinker[finalI].RelLigationFile.getName() + ":\t" + new DecimalFormat("#,###").format(Stat.UseLinker[finalI].RelLigationNum) + "\n");
                    Opts.StatisticFile.Append(Stat.UseLinker[finalI].SameValidFile.getName() + ":\t" + new DecimalFormat("#,###").format(Stat.UseLinker[finalI].SameValidNum) + "\n");
                    Opts.StatisticFile.Append(Stat.UseLinker[finalI].SameCleanFile.getName() + ":\t" + new DecimalFormat("#,###").format(Stat.UseLinker[finalI].SameCleanNum) + "\n");
                    Opts.StatisticFile.Append(Stat.UseLinker[finalI].RawDiffBedpeFile.getName() + ":\t" + new DecimalFormat("#,###").format(Stat.UseLinker[finalI].RawDiffBedpeNum) + "\n");
                    Opts.StatisticFile.Append(Stat.UseLinker[finalI].DiffCleanFile.getName() + ":\t" + new DecimalFormat("#,###").format(Stat.UseLinker[finalI].DiffCleanNum) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            STS[i].start();
            SThread.add(STS[i]);
        }
        //=================================================BedpeFile To Inter===========================================

        //--------------------------------------------------------------------------------------------------------------
        if (StepCheck(Opts.Step.BedPe2Inter.toString())) {
            new BedpeToInter(FinalBedpeFile.getPath(), InterBedpeFile.getPath());//将交互区间转换成交互点
        }
        //==============================================================================================================
        ST = new Thread(() -> {
            try {
                Stat.InterAction.FinalBedpeFile = new BedpeFile(FinalBedpeFile);
                Stat.InterAction.FinalBedpeNum = Stat.InterAction.FinalBedpeFile.getItemNum();
                Stat.InterAction.IntraActionNum = new BedpeFile(SameBedpeFile).getItemNum();
                Stat.InterAction.InterActionNum = Stat.InterAction.FinalBedpeNum - Stat.InterAction.IntraActionNum;
                if (Stat.ComInfor.Restriction.replace("^", "").length() <= 4) {
                    Stat.InterAction.ShortRegionNum = SameBedpeFile.DistanceCount(0, 5000, 1);
                } else {
                    Stat.InterAction.ShortRegionNum = SameBedpeFile.DistanceCount(0, 20000, 1);
                }
                Stat.InterAction.LongRegionNum = Stat.InterAction.IntraActionNum - Stat.InterAction.ShortRegionNum;
                File InterActionLengthDisData = new File(Stat.getDataDir() + "/InterActionLengthDistribution.data");
                Statistic.PowerLaw(SameBedpeFile, 1000000, InterActionLengthDisData);
                File InterActionLengthDisPng = new File(Stat.getImageDir() + "/" + InterActionLengthDisData.getName().replace(".data", ".png"));
                String ComLine = Python + " " + Opts.StatisticPlotFile + " -t point --title Interaction_distance_distribution -i " + InterActionLengthDisData + " -o " + InterActionLengthDisPng;
                Opts.CommandOutFile.Append(ComLine + "\n");
                Tools.ExecuteCommandStr(ComLine, null, new PrintWriter(System.err));
                Configure.InterActionDistanceDisPng = InterActionLengthDisPng;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        ST.start();
        SThread.add(ST);
        //=================================================Make Matrix==================================================
        Date matrixTime = new Date();
        System.err.println("Noise reducing: " + bedpeTime + " - " + matrixTime);
        if (StepCheck(Opts.Step.MakeMatrix.toString())) {
            for (Chromosome s : Chromosomes) {
                if (s.Size == 0) {
                    findenzy.start();
                    findenzy.join();
                }
            }
            Thread[] mmt = new Thread[Resolution.length];
            for (int i = 0; i < Resolution.length; i++) {
                int finalI = i;
                mmt[i] = new Thread(() -> {
                    try {
                        MakeMatrix matrix = new MakeMatrix(new File(MakeMatrixDir + "/" + Resolution[finalI]), Prefix, new BedpeFile(FinalBedpeFile), BedpeFile.Copy(ChrBedpeFile), Chromosomes, Resolution[finalI], Threads);//生成交互矩阵类
                        matrix.Run();//运行
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                mmt[i].start();
            }
            for (int i = 0; i < Resolution.length; i++) {
                mmt[i].join();
            }
            //------------------------------------------------------画热图-----------------------------------------
            for (int aDrawResolution : DrawResolution) {
                File OutDir = new File(MakeMatrixDir + "/img_" + Tools.UnitTrans(aDrawResolution, "B", "M") + "M");
                if (!OutDir.isDirectory() && !OutDir.mkdir()) {
                    System.err.println(new Date() + "\tWarning! Can't Create " + OutDir);
                }
                MakeMatrix matrix = new MakeMatrix(new File(MakeMatrixDir + "/" + aDrawResolution), Prefix, InterBedpeFile, ChrBedpeFile, Chromosomes, aDrawResolution, Threads);//生成交互矩阵类
                if (!new File(MakeMatrixDir + "/" + aDrawResolution).isDirectory()) {
                    matrix.Run();
                }
                if (matrix.getDenseMatrixFile().PlotHeatMap(matrix.getBinSizeFile(), aDrawResolution, new File(OutDir + "/" + Prefix + ".interaction." + Tools.UnitTrans(aDrawResolution, "B", "M") + "M.png")) != 0) {
                    System.err.println("There are some worried in plot heatmap : " + matrix.getDenseMatrixFile());
                }

                MatrixFile[] DenseMatrixFile = matrix.getChrDenseMatrixFile();
                for (int j = 0; j < Chromosomes.length; j++) {
                    if (DenseMatrixFile[j].PlotHeatMap(new String[]{Chromosomes[j].Name + ":0", Chromosomes[j].Name + ":0"}, aDrawResolution, new File(OutDir + "/" + Prefix + "." + Chromosomes[j].Name + "." + Tools.UnitTrans(aDrawResolution, "B", "M") + "M.png")) != 0) {
                        System.err.println("There are some worried in plot heatmap : " + DenseMatrixFile[j]);
                    }
                }
            }
        }
        //==============================================================================================================
        ST = new Thread(() -> {
            Stat.HeatMapInit(DrawResolution.length);
            for (int i = 0; i < DrawResolution.length; i++) {
                Stat.DrawHeatMap[i].Resolution = DrawResolution[i];
                try {
                    Stat.DrawHeatMap[i].Figure = Stat.GetBase64(new File(MakeMatrixDir + "/img_" + Tools.UnitTrans(DrawResolution[i], "B", "M") + "M/" + Prefix + ".interaction." + Tools.UnitTrans(DrawResolution[i], "B", "M") + "M.png"));
                } catch (IOException e) {
                    Stat.DrawHeatMap[i].Figure = "";
                }
            }
        });
        ST.start();
        SThread.add(ST);
        //==============================================================================================================
        Date endTime = new Date();
        System.err.println("Create matrix: " + matrixTime + " - " + endTime);
        Stat.RunTime.LinkerFilter = Tools.DateFormat((seTime.getTime() - preTime.getTime()) / 1000);
        Stat.RunTime.Mapping = Tools.DateFormat((bedpeTime.getTime() - seTime.getTime()) / 1000);
        Stat.RunTime.LigationFilter = Tools.DateFormat((matrixTime.getTime() - bedpeTime.getTime()) / 1000);
        Stat.RunTime.MakeMatrix = Tools.DateFormat((endTime.getTime() - matrixTime.getTime()) / 1000);
        Stat.RunTime.Total = Tools.DateFormat((endTime.getTime() - preTime.getTime()) / 1000);
        System.out.println("\n-------------------------------Time----------------------------------------");
        System.out.println("PreProcess:\t" + Stat.RunTime.LinkerFilter);
        System.out.println("SeProcess:\t" + Stat.RunTime.Mapping);
        System.out.println("BedpeProcess:\t" + Stat.RunTime.LigationFilter);
        System.out.println("MakeMatrix:\t" + Stat.RunTime.MakeMatrix);
        System.out.println("Total:\t" + Stat.RunTime.Total);
        //===================================Report=====================================================================

        for (Thread t : SThread) {
            t.join();
        }
        Stat.Show();
        Stat.ReportHtml(new File(ReportDir + "/Test.index.html"));
        Tools.RemoveEmptyFile(OutPath);
    }

    /**
     * Create reference genome index
     *
     * @param genomefile genome file
     */
    private void CreateIndex(File genomefile) {
        File IndexDir = new File(genomefile.getParent() + "/" + Opts.OutDir.IndexDir);
        if (!IndexDir.isDirectory() && !IndexDir.mkdir()) {
            System.out.println("Create " + IndexDir + " false");
            System.exit(1);
        }
        IndexPrefix = new File(IndexDir + "/" + genomefile.getName());
        Stat.ComInfor.IndexPrefix = IndexPrefix;
        SeProcess.CreateIndex(genomefile, IndexPrefix, Threads);
    }

    /**
     * <p>创建酶切片段文件，获取染色体大小</p>
     *
     * @return 线程句柄
     */
    private Thread FindRestrictionFragment() {
        return new Thread(() -> {
            try {
                System.out.println(new Date() + "\tStart to find restriction fragment");
                if (!EnzyPath.isDirectory() && !EnzyPath.mkdir()) {
                    System.err.println(new Date() + "\tCreate " + EnzyPath + " false !");
                }
                FindRestrictionSite fr = new FindRestrictionSite(GenomeFile, EnzyPath, Restriction, EnzyFilePrefix);
                ArrayList<Chromosome> TempChrs = fr.Run();//取出基因组文件中所有染色体的大小信息
                File[] TempChrEnzyFile = fr.getChrFragmentFile();
//                    ChrSizeFile = fr.getChrSizeFile();
                //
                for (int i = 0; i < Chromosomes.length; i++) {
                    boolean flag = false;
                    for (int j = 0; j < TempChrs.size(); j++) {
                        if (TempChrs.get(j).Name.equals(Chromosomes[i].Name)) {
                            Chromosomes[i] = TempChrs.get(j);
                            ChrEnzyFile[i] = new CommonFile(TempChrEnzyFile[j]);
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        System.err.println(new Date() + "\t[Main:FindRestrictionFragment]\tWarning! No " + Chromosomes[i].Name + " in genomic file");
                    }
                }
                System.out.println(new Date() + "\tEnd find restriction fragment");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    private void SeProcess(FastqFile fastqFile, String Prefix) throws IOException, InterruptedException {
        if (fastqFile.ItemNum <= 0) {
            fastqFile.ItemNum = fastqFile.getItemNum();
        }
        long splitnum = (long) Math.ceil((double) fastqFile.ItemNum / Threads * 4);
        splitnum = splitnum + (4 - splitnum % 4) % 4;
        ArrayList<CommonFile> SplitFastqFile = fastqFile.SplitFile(SeProcessDir + "/" + fastqFile.getName(), splitnum);//1亿行作为一个单位拆分
        SeProcess se = new SeProcess(fastqFile, IndexPrefix, AlignMisMatch, MinUniqueScore, SeProcessDir, Prefix, ReadsType);
        SamFile SamFile = se.getSamFile();
        SamFile UniqSamFile = se.getUniqSamFile();
        BedFile SortBedFile = se.getSortBedFile();
        SamFile[] SplitSamFile = new SamFile[SplitFastqFile.size()];
        SamFile[] SplitFilterSamFile = new SamFile[SplitFastqFile.size()];
        BedFile[] SplitSortBedFile = new BedFile[SplitFastqFile.size()];
        Thread[] t2 = new Thread[Threads];
        int[] Index = new int[]{0};
        for (int i = 0; i < t2.length; i++) {
            t2[i] = new Thread(() -> {
                int finalI;
                FastqFile InFile;
                while (Index[0] < SplitFastqFile.size()) {
                    synchronized (t2) {
                        try {
                            finalI = Index[0];
                            InFile = new FastqFile(SplitFastqFile.get(finalI));
                            Index[0]++;
                        } catch (IndexOutOfBoundsException e) {
                            break;
                        }
                    }
                    try {
                        SeProcess ssp = new SeProcess(InFile, IndexPrefix, AlignMisMatch, MinUniqueScore, SeProcessDir, Prefix + ".split" + finalI, ReadsType);//单端处理类
//                            ssp.Threads = 1;//设置线程数
                        ssp.setIteration(Iteration);
                        ssp.Run();
                        SplitSamFile[finalI] = ssp.getSamFile();
                        SplitFilterSamFile[finalI] = ssp.getUniqSamFile();
                        SplitSortBedFile[finalI] = ssp.getSortBedFile();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            t2[i].start();
        }
        Tools.ThreadsWait(t2);
        for (File s : SplitFastqFile) {
            if (DeBugLevel < 1) {
                AbstractFile.delete(s);
            }
        }
        new Thread(() -> {
            try {
                FileTool.MergeSamFile(SplitSamFile, SamFile);
                FileTool.MergeSamFile(SplitFilterSamFile, UniqSamFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (DeBugLevel < 1) {
                for (int i = 0; i < SplitFastqFile.size(); i++) {
                    AbstractFile.delete(SplitFastqFile.get(i));
                    AbstractFile.delete(SplitSamFile[i]);
                    AbstractFile.delete(SplitFilterSamFile[i]);
                }
            }
        }).start();
        Thread t3 = new Thread(() -> {
            try {
                SortBedFile.MergeSortFile(SplitSortBedFile);
                for (File s : SplitSortBedFile) {
                    if (DeBugLevel < 1) {
                        AbstractFile.delete(s);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t3.start();
        t3.join();
    }

    private Thread BedpeProcess(String UseLinker, BedpeFile SeBedpeFile, int threads) {
        return new Thread(() -> {
            try {
                BedpeProcess bedpe = new BedpeProcess(new File(BedpeProcessDir + "/" + UseLinker), Prefix + "." + UseLinker, Chromosomes, ChrEnzyFile, SeBedpeFile);//bedpe文件处理类
                bedpe.Threads = threads;//设置线程数
                bedpe.Run();//运行
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    //==============================

    private void Init() throws IOException {
        for (Configure.Require opt : Configure.Require.values()) {
            if (opt.Value == null || opt.Value.toString().trim().equals("")) {
                System.err.println("Error ! no " + opt);
                System.exit(1);
            }
        }
        //----------------------------------------必要参数赋值-----------------------------------------------------------
        String[] tempstrs;
        InputFile = Configure.InputFile;
        GenomeFile = Configure.GenomeFile;
        Restriction = Configure.Restriction;
        Opts.LFStat.EnzymeCuttingSite = Restriction;
        String[] halfLinker = Configure.HalfLinker;
        LinkerA = halfLinker[0];
        LinkerLength = LinkerA.length();
        if (halfLinker.length > 1) {
            LinkerB = halfLinker[1];
            LinkerLength += LinkerB.length();
        } else {
            LinkerLength += LinkerA.length();
        }
        //-----------------------------------------可选参数赋值----------------------------------------------------------
        OutPath = Configure.OutPath;
        Prefix = Configure.Prefix;
        Resolution = Configure.Resolution;
        Threads = Configure.Thread;
        DrawResolution = Configure.DrawResolution;
        Step.addAll(Arrays.asList(Configure.Step.trim().split("\\s+")));
        if (Configure.AdapterSeq != null && Configure.AdapterSeq.length > 0) {
            AdapterSeq = Configure.AdapterSeq;
        }
        if (Configure.Index != null && !Configure.Index.toString().trim().equals("")) {
            IndexPrefix = Configure.Index;
        }
        if (Configure.Chromosome != null && Configure.Chromosome.length > 0) {
            Chromosomes = Configure.Chromosome;
        } else {
            System.out.println(new Date() + "\tCalculate chromosome ......");
            Chromosomes = Tools.CheckChromosome(Chromosomes, GenomeFile);
            Configure.Chromosome = Chromosomes;
        }
        ChrEnzyFile = new CommonFile[Chromosomes.length];

        //-------------------------------------------高级参数赋值--------------------------------------------------------
        Bwa = Configure.Bwa;
        Python = Configure.Python;
        MatchScore = Configure.MatchScore;
        MisMatchScore = Configure.MisMatchScore;
        InDelScore = Configure.InDelScore;
        MaxReadsLength = Configure.MaxReadsLen;
        AlignMisMatch = Configure.AlignMisMatch;
        Iteration = Configure.Iteration;
        ReadsType = Configure.AlignType.equals("Short") ? Opts.FileFormat.ShortReads : Configure.AlignType.equals("Long") ? Opts.FileFormat.LongReads : Opts.FileFormat.ErrorFormat;
        DeBugLevel = Configure.DeBugLevel;
        //设置唯一比对分数
        if (ReadsType == Opts.FileFormat.ShortReads) {
            MinUniqueScore = 20;
        } else if (ReadsType == Opts.FileFormat.LongReads) {
            MinUniqueScore = 30;
        } else {
            System.err.println("Error reads type " + Configure.AlignType);
        }
        Configure.MinUniqueScore = MinUniqueScore;
        if (Configure.MinLinkerLen == 0) {
            Configure.MinLinkerLen = (int) (LinkerLength * 0.9);
        }
        int minLinkerLength = Configure.MinLinkerLen;
        //================================================
        if (!OutPath.isDirectory()) {
            System.err.println("Error, " + OutPath + " is not a directory");
            System.exit(1);
        }
        if (!GenomeFile.isFile()) {
            System.err.println("Error, " + GenomeFile + " is not a file");
            System.exit(1);
        }
        if (!InputFile.isFile()) {
            System.err.println("Error, " + InputFile + " is not a file");
            System.exit(1);
        }
        //=======================================================================;
        PreProcessDir = new File(OutPath + "/" + Opts.OutDir.PreDir);
        SeProcessDir = new File(OutPath + "/" + Opts.OutDir.SeDir);
        BedpeProcessDir = new File(OutPath + "/" + Opts.OutDir.BedpeDir);
        MakeMatrixDir = new File(OutPath + "/" + Opts.OutDir.MatrixDir);
        ReportDir = new File(OutPath + "/" + Opts.OutDir.ReportDir);
        EnzyPath = new File(OutPath + "/" + Opts.OutDir.EnzyFragDir);
        File[] CheckDir = new File[]{PreProcessDir, SeProcessDir, BedpeProcessDir, MakeMatrixDir, EnzyPath, ReportDir};
        for (File s : CheckDir) {
            if (!s.isDirectory() && !s.mkdir()) {
                System.err.println("Can't create " + s);
                System.exit(1);
            }
        }

        tempstrs = new String[]{"A", "B", "C", "D", "E", "F", "G"};
        //构建Linker序列
        if (halfLinker.length > tempstrs.length) {
            System.err.println("Error! too many half-linker:\t" + halfLinker.length);
            System.exit(1);
        }
        LinkerSeq = new LinkerSequence[halfLinker.length * halfLinker.length];
        Opts.LFStat.Linkers = LinkerSeq;
        Opts.LFStat.Init();
        ValidLinkerSeq = new LinkerSequence[halfLinker.length];
        for (int i = 0; i < halfLinker.length; i++) {
            for (int j = 0; j < halfLinker.length; j++) {
                if (i == j) {
                    LinkerSeq[i * halfLinker.length + j] = new LinkerSequence(halfLinker[i] + Tools.ReverseComple(halfLinker[j]), tempstrs[i] + tempstrs[j], true);
                    ValidLinkerSeq[i] = new LinkerSequence(halfLinker[i] + Tools.ReverseComple(halfLinker[j]), tempstrs[i] + tempstrs[j], true);
                } else {
                    LinkerSeq[i * halfLinker.length + j] = new LinkerSequence(halfLinker[i] + Tools.ReverseComple(halfLinker[j]), tempstrs[i] + tempstrs[j]);
                }
            }
        }
        MinLinkerFilterQuality = minLinkerLength * MatchScore + (LinkerLength - minLinkerLength) * MisMatchScore;//设置linkerfilter最小分数
        Opts.LFStat.Threshold = MinLinkerFilterQuality;
        EnzyFilePrefix = Prefix + "." + Restriction.replace("^", "");
        LinkerFile = new File(PreProcessDir + "/" + Prefix + ".linker");
        AdapterFile = new File(PreProcessDir + "/" + Prefix + ".adapter");
        //清空原来的Adapter文件
        File[] CleanFile = new File[]{LinkerFile, AdapterFile};
        for (File f : CleanFile) {
            if (!AbstractFile.clean(f)) {
                System.err.println("Error! can't clean " + f.getName());
                System.exit(1);
            }
        }
        Stat = new Report(ReportDir);
    }

    private boolean StepCheck(String step) {
        if (Step.size() > 0) {
            if (Step.get(0).equals(step)) {
                Step.remove(0);
                return true;
            } else if (Step.get(0).equals("-") && Step.size() > 1) {
                if (Step.get(1).equals(step)) {
                    Step.remove(0);
                    Step.remove(0);
                }
                return true;
            } else return Step.get(0).equals("-") && Step.size() == 1;
        }
        return false;
    }

    private void ShowParameter() {
        System.out.println(Configure.ShowParameter());
    }


}
