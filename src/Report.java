import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import Component.File.BedpeFile;
import Component.File.FileTool;
import Component.unit.Configure;
import Component.File.AbstractFile;
import Component.unit.Opts;
import org.apache.commons.io.FileUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import sun.misc.BASE64Encoder;

/**
 * Created by snowf on 2019/2/17.
 *
 */
public class Report {
    private File ReportOutPath;
    public CommonInfor ComInfor = new CommonInfor();
    public ActionInfor InterAction = new ActionInfor();
    public RunTime RunTime = new RunTime();
    public Linker[] Linkers;
    public LinkerClass[] UseLinker;
    public HeatMap[] DrawHeatMap;
    public String[] ReadsLengthDisBase64;
    public long RawDataReadsNum;
    public String AdapterSequence = "";
    public File PreDir, SeDir, BedpeDir, MatrixDir, TransDir;
    public File LinkerFile = new File("");
    public File GenomeIndex = new File("");
    public File AdapterFile = new File("");

    public ArrayList<String> Chromosome = new ArrayList<>();
    public BedpeFile FinalBedpeName = new BedpeFile("");
    public int MinUniqueScore;
    public int[] Resolution;
    public int Thread;
    private File DataDir;
    private File ImageDir;

    public Report(File OutPath) {
        ReportOutPath = OutPath;
        DataDir = new File(ReportOutPath + "/data");
        ImageDir = new File(ReportOutPath + "/image");
        File[] CheckFile = new File[]{ReportOutPath, DataDir, ImageDir};
        for (File f : CheckFile) {
            if (!f.isDirectory() && !f.mkdir()) {
                System.err.println(new Date() + ":\tCan't create " + f);
                System.exit(1);
            }
        }
    }

    public Report() {

    }

    public void Show() {
        System.out.println("\n--------------------------------Statistic----------------------------------");
        System.out.print("Raw data name:\t" + Configure.Require.InputFile.Value.toString() + "\t");
        System.out.println("Raw reads number:\t" + new DecimalFormat("#,###").format(RawDataReadsNum));
        System.out.println();
        System.out.println("\n-----------------------------------------\nLinkers type\tReads number\tPercent");
        for (int i = 0; i < Linkers.length; i++) {
            System.out.println(Linkers[i].Name + "\t" + new DecimalFormat("#,###").format(Linkers[i].Num) + "\t" + String.format("%.2f", Linkers[i].Num / RawDataReadsNum * 100) + "%");
        }
        System.out.println("\n-----------------------------------------\nFastq file\tReads number\tFastq file\tReads number");
        for (int i = 0; i < UseLinker.length; i++) {
            System.out.println(UseLinker[i].FastqFileR1.getName() + "\t" + new DecimalFormat("#,###").format(UseLinker[i].FastqNumR1) + "\t" + UseLinker[i].FastqFileR2.getName() + "\t" + new DecimalFormat("#,###").format(UseLinker[i].FastqNumR2));
        }
        System.out.println("\n-----------------------------------------\nBed file\tUniq reads number\tPercent\tBed file\tUniq reads number\tPercent");
        for (int i = 0; i < UseLinker.length; i++) {
            System.out.println(UseLinker[i].UniqMapFileR1.getName() + "\t" + new DecimalFormat("#,###").format(UseLinker[i].UniqMapNumR1) + "\t" + String.format("%.2f", UseLinker[i].UniqMapNumR1 / UseLinker[i].FastqNumR1 * 100) + "%" + "\t" + UseLinker[i].UniqMapFileR2.getName() + "\t" + new DecimalFormat("#,###").format(UseLinker[i].UniqMapNumR2) + "\t" + String.format("%.2f", UseLinker[i].UniqMapNumR2 / UseLinker[i].FastqNumR2 * 100) + "%");
        }
        double sum = 0;
        System.out.println("\n-----------------------------------------\nBedpe file\tReads number\tPercent");
        for (int i = 0; i < UseLinker.length; i++) {
            sum = sum + UseLinker[i].SameCleanNum + UseLinker[i].DiffCleanNum;
            System.out.println("UniqMap\t" + new DecimalFormat("#,###").format(UseLinker[i].RawBedpeNum));
            System.out.println(UseLinker[i].SelfLigationFile.getName() + "\t" + new DecimalFormat("#,###").format(UseLinker[i].SelfLigationNum) + "\t" + String.format("%.2f", UseLinker[i].SelfLigationNum / UseLinker[i].RawBedpeNum * 100) + "%");
            System.out.println(UseLinker[i].RelLigationFile.getName() + "\t" + new DecimalFormat("#,###").format(UseLinker[i].RelLigationNum) + "\t" + String.format("%.2f", UseLinker[i].RelLigationNum / UseLinker[i].RawBedpeNum * 100) + "%");
            System.out.println(UseLinker[i].SameValidFile.getName() + "\t" + new DecimalFormat("#,###").format(UseLinker[i].SameValidNum) + "\t" + String.format("%.2f", UseLinker[i].SameValidNum / UseLinker[i].RawBedpeNum * 100) + "%");
            System.out.println(UseLinker[i].RawDiffBedpeFile.getName() + "\t" + new DecimalFormat("#,###").format(UseLinker[i].RawDiffBedpeNum) + "\t" + String.format("%.2f", UseLinker[i].RawDiffBedpeNum / UseLinker[i].RawBedpeNum * 100) + "%");
        }
        System.out.println("\n-------------------------------------------------------------");
        System.out.println("Total action number:\t" + new DecimalFormat("#,###").format(InterAction.FinalBedpeNum) + "\t" + String.format("%.2f", InterAction.FinalBedpeNum / RawDataReadsNum * 100) + "%");
        System.out.println("Inter action number:\t" + new DecimalFormat("#,###").format(InterAction.InterActionNum) + "\t" + String.format("%.2f", InterAction.InterActionNum / InterAction.FinalBedpeNum * 100) + "%");
        System.out.println("Intra action number:\t" + new DecimalFormat("#,###").format(InterAction.IntraActionNum) + "\t" + String.format("%.2f", InterAction.IntraActionNum / InterAction.FinalBedpeNum * 100) + "%");
        System.out.println("\n-------------------------------------------------------------");
        if (ComInfor.Restriction.replace("^", "").length() <= 4) {
            System.out.println("Short region <= 5k :\t" + new DecimalFormat("#,###").format(InterAction.ShortRegionNum) + "\t" + String.format("%.2f", InterAction.ShortRegionNum / InterAction.IntraActionNum * 100) + "%");
            System.out.println("Long region > 5k :\t" + new DecimalFormat("#,###").format(InterAction.LongRegionNum) + "\t" + String.format("%.2f", InterAction.LongRegionNum / InterAction.IntraActionNum * 100) + "%");
        } else {
            System.out.println("Short region <= 20k :\t" + new DecimalFormat("#,###").format(InterAction.ShortRegionNum) + "\t" + String.format("%.2f", InterAction.ShortRegionNum / InterAction.IntraActionNum * 100) + "%");
            System.out.println("Long region > 20k :\t" + new DecimalFormat("#,###").format(InterAction.LongRegionNum) + "\t" + String.format("%.2f", InterAction.LongRegionNum / InterAction.IntraActionNum * 100) + "%");
        }
    }

    public void ReportHtml(File outfile) throws IOException {
        //======================复制相关文件=============================
        for (File f : new File[]{Opts.JqueryJs, Opts.ScriptJs, Opts.StyleCss}) {
            BufferedReader reader = new BufferedReader(FileTool.GetFileStream(f.getPath()));
            BufferedWriter writer = new BufferedWriter(new FileWriter(ReportOutPath + "/" + f.getName()));
            String Line;
            while ((Line = reader.readLine()) != null) {
                writer.write(Line + "\n");
            }
            writer.close();
        }
        //=====================================================================
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
//        resolver.setPrefix("resource/");
        resolver.setPrefix(Opts.TemplateReportHtml.getParent() + "/");
        resolver.setSuffix(".html");
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        Context context = new Context();
        context.setVariable("Date", DateFormat.getDateTimeInstance().format(new Date()));
        ComInfor.InputFile = new BedpeFile(Configure.Require.InputFile.Value.toString());
        ComInfor.OutPutDir = Configure.OutPath;
        ComInfor.OutPutPrefix = Configure.Prefix;
        ComInfor.GenomeFile = new BedpeFile(Configure.Require.GenomeFile.Value.toString());
        context.setVariable("AdapterSeq", AdapterSequence.replaceAll("\\s+", "<br/>"));
        context.setVariable("TotalReads", RawDataReadsNum);
        context.setVariable("ComInformation", ComInfor);
        long Ambiguous = RawDataReadsNum;
        for (int i = 0; i < Linkers.length; i++) {
//            context.setVariable(LinkersType.get(i) + "LinkerNum", LinkersNum[i]);
            Ambiguous -= Linkers[i].Num;
        }
        context.setVariable("Linkers", Linkers);
        context.setVariable("AmbiguousLinkerNum", ThousandFormat(Ambiguous));
        context.setVariable("AmbiguousLinkerPercent", PercentFormat((double) Ambiguous / RawDataReadsNum * 100) + "%");
        context.setVariable("PreDir", PreDir.getPath());
        context.setVariable("LinkerClass", UseLinker);
        context.setVariable("Inter", InterAction);
        context.setVariable("RunTime", RunTime);
        context.setVariable("LinkerAliScoreDis", GetBase64(Configure.LinkerScoreDisPng));
        context.setVariable("ReadsLenDiss", ReadsLengthDisBase64);
        context.setVariable("InteractionDistanceDis", GetBase64(Configure.InterActionDistanceDisPng));
        context.setVariable("DrawResolutionHeatMap", DrawHeatMap);


        //========================================test=============================


//==================================
        String html = templateEngine.process(Opts.TemplateReportHtml.getName().replace(".html", ""), context);
        FileUtils.writeStringToFile(outfile, html, StandardCharsets.UTF_8, false);
    }

    private String ThousandFormat(Number n) {
        return new DecimalFormat("#,###").format(n);
    }

    private String PercentFormat(Number n) {
        return String.format("%.2f", n);
    }

    public void LinkerClassInit(int i) {
        UseLinker = new LinkerClass[i];
        for (int j = 0; j < i; j++) {
            UseLinker[j] = new LinkerClass();
        }
    }

    public void HeatMapInit(int i) {
        DrawHeatMap = new HeatMap[i];
        for (int j = 0; j < i; j++) {
            DrawHeatMap[j] = new HeatMap();
        }
    }

    public void LinkerInit(int i) {
        Linkers = new Linker[i];
        for (int j = 0; j < i; j++) {
            Linkers[j] = new Linker();
        }
    }


    public String GetBase64(File f) throws IOException {
        FileInputStream image = new FileInputStream(f);
        byte[] data = new byte[image.available()];
        image.read(data);
        image.close();
        return new BASE64Encoder().encode(data);
    }

    public File getDataDir() {
        return DataDir;
    }

    public File getImageDir() {
        return ImageDir;
    }
}

class LinkerClass {
    public String LinkerType, LinkerSequence;
    public File SeProcessOutDir, BedpeProcessOutDir;
    public AbstractFile FastqFileR1, FastqFileR2;
    public AbstractFile UniqMapFileR1, UniqMapFileR2;
    public AbstractFile RawBedpeFile, RawSameBedpeFile, RawDiffBedpeFile;
    public AbstractFile SelfLigationFile, RelLigationFile, SameValidFile;
    public AbstractFile SameCleanFile, DiffCleanFile;
    public AbstractFile MergeCleanFile;
    public double FastqNumR1, FastqNumR2;
    public double UniqMapNumR1, UniqMapNumR2;
    public double RawBedpeNum, RawSameBedpeNum, RawDiffBedpeNum;
    public double SelfLigationNum, RelLigationNum, SameValidNum;
    public double SameCleanNum, DiffCleanNum;

    LinkerClass(String s) {
        LinkerType = s;
    }

    LinkerClass() {
    }
}

class CommonInfor {
    public AbstractFile InputFile;
    public AbstractFile GenomeFile;
    public File OutPutDir;
    public String OutPutPrefix;
    public int Thread;
    public String HalfLinkerA = "", HalfLinkerB = "";
    public int MatchScore, MisMatchScore, InDelScore;
    public File IndexPrefix = new File("");
    public String Restriction = "";
    public int[] Resolution;
    public int MinReadsLen;
    public int MaxReadsLen;
    public ArrayList<String> Chromosome = new ArrayList<>();
}

class ActionInfor {
    public AbstractFile FinalBedpeFile = new BedpeFile("");
    public double FinalBedpeNum;
    public double IntraActionNum;
    public double InterActionNum;
    public double LongRegionNum;
    public double ShortRegionNum;
}

class RunTime {
    public String StartTime;
    public String LinkerFilter;
    public String Mapping;
    public String LigationFilter;
    public String MakeMatrix;
    public String TransLocation;
    public String Total;
}

class HeatMap {
    public int Resolution;
    public String Figure;
}

class Linker {
    public String Name;
    public double Num;
}