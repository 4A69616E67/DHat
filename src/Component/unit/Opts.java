package Component.unit;

import Component.File.CommonFile;
import Component.Statistic.*;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by snowf on 2019/2/17.
 */
public class Opts {
    /**
     * 文件类型枚举类
     */
    public enum FileFormat {
        ErrorFormat, EmptyFile, BedpePointFormat, BedpeRegionFormat, Phred33, Phred64, ShortReads, LongReads, Undefine
    }

    /**
     * 断点枚举类
     */
    public enum Step {
        PreProcess("PreProcess"), SeProcess("Alignment"), BedPeProcess("NoiseReduce"), BedPe2Inter("BedPe2Inter"), MakeMatrix("MakeMatrix"), CreateIndex("Index"), FindEnzymeFragment("Fragment"), Statistic("Stat");

        private String Str;
        public boolean Execute = false;//是否需要执行

        Step(String s) {
            this.Str = s;
        }

        public String getStr() {
            return Str;
        }

        @Override
        public String toString() {
            return this.Str + ":" + Execute;
        }
    }

    public enum OutDir {
        PreDir("01.PreProcess"), SeDir("02.Alignment"), BedpeDir("03.NoiseReduce"), MatrixDir("04.MakeMatrix"), ReportDir("05.Report"), EnzyFragDir("0a.EnzymeFragment"), IndexDir("0b.Index");

        private String Str;

        OutDir(String s) {
            this.Str = s;
        }

        @Override
        public String toString() {
            return this.Str;
        }
    }

    public static String GetStringOpt(CommandLine commandLine, String opt_string, String default_string) {
        return commandLine.hasOption(opt_string) ? commandLine.getOptionValue(opt_string) : default_string;
    }

    public static File GetFileOpt(CommandLine commandLine, String opt_string, File default_file) {
        return commandLine.hasOption(opt_string) ? new File(commandLine.getOptionValue(opt_string)) : default_file;
    }

    public static int GetIntOpt(CommandLine commandLine, String opt_string, int default_int) {
        return commandLine.hasOption(opt_string) ? Integer.parseInt(commandLine.getOptionValue(opt_string)) : default_int;
    }

    public static float GetFloatOpt(CommandLine commandLine, String opt_string, float default_float) {
        return commandLine.hasOption(opt_string) ? Float.parseFloat(commandLine.getOptionValue(opt_string)) : default_float;
    }

    public static String[] GetStringOpts(CommandLine commandLine, String opt_string, String[] default_string) {
        return commandLine.hasOption(opt_string) ? commandLine.getOptionValues(opt_string) : default_string;
    }

    public static File[] GetFileOpts(CommandLine commandLine, String opt_string, File[] default_file) {
        if (commandLine.hasOption(opt_string)) {
            return StringArrays.toFile(commandLine.getOptionValues(opt_string));
        } else {
            return default_file;
        }
    }

    public static int[] GetIntOpts(CommandLine commandLine, String opt_string, int[] default_string) {
        if (commandLine.hasOption(opt_string)) {
            return StringArrays.toInteger(commandLine.getOptionValues(opt_string));
        } else {
            return default_string;
        }
    }

    public static void StepCheck(String s) {
        String Connector = "-";
        for (Step p : Step.values()) {
            p.Execute = false;
        }
        if (s == null || s.trim().equals("")) {
            return;
        }
        String[] str = s.split("\\s+");
        ArrayList<String> s_list = new ArrayList<>();
        s_list.add("");
        for (String aStr : str) {
            if (aStr.equals(Connector)) {
                s_list.set(s_list.size() - 1, s_list.get(s_list.size() - 1) + Connector);
            } else if (s_list.get(s_list.size() - 1).matches(".*" + Connector)) {
                s_list.set(s_list.size() - 1, s_list.get(s_list.size() - 1) + aStr);
            } else {
                s_list.add(aStr);
            }
        }
        for (String l : s_list) {
            if (l.matches(".*" + Connector + ".*")) {
                Step start = null, end = null;
                if (l.matches(".+" + Connector)) {
                    l += Step.MakeMatrix.Str;
                } else if (l.matches(Connector + ".+")) {
                    l = Step.PreProcess.Str + l;
                } else if (l.equals(Connector)) {
                    l = Step.PreProcess.Str + l;
                    l += Step.MakeMatrix.Str;
                }
                String[] ll = l.split(Connector);
                for (Step t : Step.values()) {
                    if (t.Str.equals(ll[0])) {
                        start = t;
                    }
                    if (t.Str.equals(ll[1])) {
                        end = t;
                    }
                }
                if (start != null && end != null) {
                    for (Step t : Step.values()) {
                        if (t.compareTo(start) >= 0 && t.compareTo(end) <= 0) {
                            t.Execute = true;
                        }
                    }
                }
            } else {
                for (Step t : Step.values()) {
                    if (t.Str.equals(l)) {
                        t.Execute = true;
                        break;
                    }
                }
            }
        }
    }


    public static final int MaxBinNum = 1000000;//最大bin的数目
    public static final File JarFile = new File(Opts.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    public static final File ScriptDir = new File(JarFile.getParent() + "/Script");//脚本文件存放的位置
    public static final File ResourceDir = new File(JarFile.getParent() + "/Resource");//资源文件存放的位置
    public static CommonFile CommandOutFile = new CommonFile(Configure.OutPath + "/Command.log");
    public static CommonFile StatisticFile = new CommonFile(Configure.OutPath + "/Statistic.txt");
    public static CommonFile ResourceStatFile = new CommonFile(Configure.OutPath + "/JVM_stat.txt");
    public static final String[] ResourceFile = new String[]{"default.conf", "default_adv.conf"};
    public static final String[] ScriptFile = new String[]{"PlotHeatMap.py", "StatisticPlot.py", "RegionPlot.py"};
    public static final CommonFile ConfigFile = new CommonFile(ResourceDir + "/" + ResourceFile[0]);
    public static final CommonFile AdvConfigFile = new CommonFile(ResourceDir + "/" + ResourceFile[1]);
    public static final File PlotHeatMapScriptFile = new File(ScriptDir + "/" + ScriptFile[0]);
    public static final File StatisticPlotFile = new File(ScriptDir + "/" + ScriptFile[1]);
    public static final File StyleCss = new File("/resource/style.css");
    public static final File JqueryJs = new File("/resource/jquery.min.js");
    public static final File ScriptJs = new File("/resource/script.js");
    public static final File TemplateReportHtml = new File("/resource/Report.html");
    public static final File ReadMeFile = new File("/resource/ReadMe.md");
    public static final Float Version = 1.0F;
    public static final String Author = "Snowflakes";
    public static final String Email = "john-jh@foxmail.com";
    public static final long MaxMemory = Runtime.getRuntime().maxMemory();//java能获取的最大内存
    public static Hashtable<String, Integer> ChrSize = new Hashtable<>();
    //==================================================================================================================
    public static final LinkerFilterStat LFStat = new LinkerFilterStat();
    public static final AlignmentStat ALStat = new AlignmentStat();
    public static final NoiseReduceStat NRStat = new NoiseReduceStat();
    public static final MakeMatrixStat MMStat = new MakeMatrixStat();
    public static final OverviewStat OVStat = new OverviewStat();
    public static final ResourceStat RSStat = new ResourceStat();

}



