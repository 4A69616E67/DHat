import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import Component.File.BedPeFile.BedpeFile;
import Component.File.FileTool;
import Component.Statistic.StatUtil;
import Component.tool.Tools;
import Component.unit.Configure;
import Component.unit.Opts;
import org.apache.commons.io.FileUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import sun.misc.BASE64Encoder;

/**
 * Created by snowf on 2019/2/17.
 */
public class Report {
    private File ReportOutPath;
    public String[] ReadsLengthDisBase64;
    public long RawDataReadsNum;

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
        resolver.setPrefix(Opts.TemplateReportHtml.getParent() + "/");
        resolver.setSuffix(".html");
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        Context context = new Context();
        context.setVariable("Date", DateFormat.getDateTimeInstance().format(new Date()));
        Map<String, Object> map = new HashMap<>();
        map.put("Tool", new StatUtil());
        map.put("InputFile", Configure.InputFile.getPath());
        map.put("OutPath", Configure.OutPath.getPath());
        map.put("Prefix", Configure.Prefix);
        map.put("HalfLinkerA", Configure.HalfLinker[0]);
        map.put("HalfLinkerB", Configure.HalfLinker.length >= 2 ? Configure.HalfLinker[1] : "");
        map.put("Restriction", Configure.Restriction.toString());
        map.put("MinReadsLen", Configure.MinLinkerLen);
        map.put("MaxReadsLen", Configure.MaxReadsLen);
        map.put("MatchScore", Configure.MatchScore);
        map.put("MisMatchScore", Configure.MisMatchScore);
        map.put("InDelScore", Configure.InDelScore);
        map.put("Resolution", Configure.Resolution);
        map.put("Thread", Configure.Thread);
        map.put("StartTime", Opts.StartTime);
        map.put("LinkerFilterTime", Tools.DateFormat(Opts.LFStat.Time / 1000));
        map.put("MappingTime", Tools.DateFormat(Opts.ALStat.Time / 1000));
        map.put("NoiseReduceTime", Tools.DateFormat(Opts.NRStat.Time / 1000));
        map.put("MakeMatrixTime", Tools.DateFormat(Opts.MMStat.Time / 1000));
        map.put("TotalTime", Tools.DateFormat((Opts.LFStat.Time + Opts.ALStat.Time + Opts.NRStat.Time + Opts.MMStat.Time) / 1000));

        context.setVariable("Report", this);
        context.setVariable("LinkerFilterStat", Opts.LFStat);
        context.setVariable("AlignmentStat", Opts.ALStat);
        context.setVariable("NoiseReduceStat", Opts.NRStat);
        context.setVariable("OverViewStat", Opts.OVStat);
        context.setVariable("AdapterSeq", String.join(" ", Configure.AdapterSeq).replaceAll("\\s+", "<br/>"));
//        context.setVariable("AdapterDetectionDis", GetBase64(Opts.LFStat.AdapterBaseDisPng));
//        context.setVariable("TotalReads", RawDataReadsNum);
        context.setVariables(map);
//        context.setVariable("LinkerAliScoreDis", GetBase64(Opts.LFStat.LinkerScoreDisPng));
//        context.setVariable("ReadsLenDiss", ReadsLengthDisBase64);
//        context.setVariable("InteractionDistanceDis", GetBase64(Opts.NRStat.InteractionRangeDistributionPng));
//        context.setVariable("DrawResolutionHeatMap", DrawHeatMap);


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


    public String GetBase64(File f) throws IOException {
        if (f == null || !f.exists()) {
            return "";
        }
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
