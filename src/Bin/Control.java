package Bin;

import Component.unit.Chromosome;

import java.io.File;
/**
 * Created by snowf on 2019/2/17.
 *
 */
public class Control {
    private enum OutDir {
        Pre("PreProcess"), Se("SeProcess"), Bedpe("BedpeProcess"), CM("CreateMatrix"), Trans("TransLocation");
        private String Name;

        OutDir(String s) {
            this.Name = s;
        }

        @Override
        public String toString() {
            return this.Name;
        }
    }


    public static File OutPath;
    public static String Prefix;
    public static File GenomeFile;
    public static File IndexPrefix;
    public static Chromosome[] Chrs;
    public static String[] HalfLinker;
    public static String[] LinkerSeq;
    public static int MinUniqueScore;
    public static File R1FastqFile, R2FastqFile;
    public static File R1SamFile, R2SamFile;
    public static File R1BedFile, R2BedFile;
    public static File RawBedpeFile, RawSameBedpeFile, RawDiffBedpeFile, CleanSameBedpeFile, CleanDiffBedpeFile, CleanBedpeFile;
    public static File SelfLigation, RelLigation, ValidLigation;
    public static File[] ChrCleanSameBedpeFile;
    public static int ThreadNum = 1;
    public static int DebugLevel = 1;

    public static void OutFileInit() {
        if (HalfLinker != null) {
            LinkerSeq = new String[HalfLinker.length * HalfLinker.length];
            for (int i = 0; i < HalfLinker.length; i++) {
                for (int j = 0; j < HalfLinker.length; j++) {
                    LinkerSeq[i * HalfLinker.length + j] = HalfLinker[i] + new StringBuffer(HalfLinker[j]).reverse();
                }
            }
        }
    }
}
