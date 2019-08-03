package Component.File.FileManager;

import Component.File.FastQFile.FastqFile;
import Component.FragmentDigested.RestrictionEnzyme;
import Component.Software.Bwa;
import Component.Software.MAFFT;
import Component.unit.Opts;

import java.io.File;

/**
 * Created by snowf on 2019/7/10.
 */

public class FileCenter {
    enum OutDir {
        PreDir("01.PreProcess"), AlignDir("02.Alignment"), NoiseDir("03.NoiseReduce"), MatrixDir("04.CreateMatrix"), ReportDir("05.Report"), EnzymeFragDir("0a.EnzymeFragment"), IndexDir("0b.Index");

        private String Str;

        OutDir(String s) {
            this.Str = s;
        }

        @Override
        public String toString() {
            return this.Str;
        }
    }

    public static File InterActionDistanceDisPng;
    public static File LinkerScoreDisPng;
    //-----------------------------------------------------------
    public static FastqFile InputFile;
    public static File GenomeFile;
    //-----------------------------------------------------------
    public static RestrictionEnzyme Restriction;
    public static String[] HalfLinker;
    public static File OutPath = new File("./");
    public static String OutPrefix = "out";
    public static File Index;
    public static Component.unit.Chromosome[] Chromosome;
    public static String[] AdapterSeq;
    public static int[] Resolution = new int[]{1000000};
    public static int[] DrawResolution = new int[]{1000000};
    public static int Thread = 8;
    public static String Step = "-";
    //-----------------------------------------------------------------
    public static Component.Software.Python Python;
    public static Component.Software.Bwa Bwa;//
    public static MAFFT Mafft;
    public static int MatchScore = 1;
    public static int MisMatchScore = -1;
    public static int InDelScore = -1;
    public static int MinLinkerLen;
    public static int MinReadsLen = 16;
    public static int MaxReadsLen = 20;
    public static int AlignThread = 1;
    public static String AlignType = "Short";
    public static int AlignMisMatch = 0;
    public static int MinUniqueScore;
    public static boolean Iteration = false;
    public static int DeBugLevel = 0;
}
