package Component.unit;

import Component.File.CommonFile;
import org.apache.commons.cli.CommandLine;

import java.io.File;
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
        PreProcess("PreProcess"), SeProcess("SeProcess"), Bed2BedPe("Bed2BedPe"), BedPeProcess("BedPeProcess"), BedPe2Inter("BedPe2Inter"), MakeMatrix("MakeMatrix");

        private String Str;

        Step(String s) {
            this.Str = s;
        }

        @Override
        public String toString() {
            return this.Str;
        }
    }

    public enum OutDir {
        PreDir("PreProcess"), SeDir("SeProcess"), BedpeDir("BedpeProcess"), MatrixDir("MakeMatrix"), EnzyFragDir("EnzymeFragment"), IndexDir("Index"), ReportDir("Report");

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


    public static final int MaxBinNum = 1000000;//最大bin的数目
    public static final File JarFile = new File(Opts.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    public static final File ScriptDir = new File(JarFile.getParent() + "/Script");//脚本文件存放的位置
    public static final File ResourceDir = new File(JarFile.getParent() + "/Resource");//资源文件存放的位置
    public static final CommonFile CommandOutFile = new CommonFile("./Command.log");
    public static final CommonFile StatisticFile = new CommonFile("./Statistic.txt");
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
    public static final File ReadMeFile = new File("/resource/ReadMe.txt");
    public static final Float Version = 1.0F;
    public static final String Author = "Snowflakes";
    public static final String Email = "john-jh@foxmail.com";
    public static final long MaxMemory = Runtime.getRuntime().maxMemory();//java能获取的最大内存
    public static Hashtable<String, Integer> ChrSize = new Hashtable<>();
    //==================================================================================================================

}



