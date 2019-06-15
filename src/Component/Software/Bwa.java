package Component.Software;


import Component.File.CommonFile;
import Component.tool.Tools;
import Component.unit.Configure;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by snowf on 2019/3/10.
 */

public class Bwa extends AbstractSoftware implements Comparable<Bwa> {
    public Bwa(String exe) {
        super(exe);
    }

    @Override
    protected void Init() {
        if (Execution.trim().equals("")) {
            System.err.println("[bwa]\tNo execute file");
        } else {
            getVersion();
            getPath();
        }
    }

    @Override
    protected String getVersion() {
        CommonFile temporaryFile = new CommonFile(Configure.OutPath + "/bwa.version.tmp");
        try {
            Component.System.CommandLine.run(Execution, null, new PrintWriter(temporaryFile));
            ArrayList<char[]> tempLines = temporaryFile.Read();
            for (char[] tempLine : tempLines) {
                String[] s = String.valueOf(tempLine).split("\\s*:\\s*");
                if (s[0].compareToIgnoreCase("Version") == 0) {
                    Version = s[1];
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            Valid = false;
        }
        temporaryFile.delete();
        return Version;
    }

    /**
     * Usage:   bwa index [options] <in.fasta>
     * <p>
     * Options: -a STR    BWT construction algorithm: bwtsw, is or rb2 [auto]
     * -p STR    prefix of the index [same as fasta name]
     * -b INT    block size for the bwtsw algorithm (effective with -a bwtsw) [10000000]
     * -6        index files named as <in.fasta>.64.* instead of <in.fasta>.*
     *
     * @return command string
     */
    public String index(File fastaFile, File prefix) {
        return Execution + " index -p " + prefix + " " + fastaFile;
    }

    /**
     * Usage: bwa mem [options] <idxbase> <in1.fq> [in2.fq]
     * -t INT        number of threads [1]
     *
     * @return command string
     */
    public String mem(File index, File fastqFile, int thread) {
        return Execution + " mem -t" + thread + " " + index + " " + fastqFile;
    }

    /**
     * Usage:   bwa aln [options] <prefix> <in.fq>
     * -n NUM    max #diff (int) or missing prob under 0.02 err rate (float) [0.04]
     * -t INT    number of threads [1]
     * -f FILE   file to write output to instead of stdout
     *
     * @return command string
     */
    public String aln(File index, File fastqFile, File saiFile, int maxDiff, int thread) {
        return Execution + " aln -t " + thread + " -n " + maxDiff + " -f " + saiFile + " " + index + " " + fastqFile;
    }

    /**
     * Usage: bwa samse [-n max_occ] [-f out.sam] [-r RG_line] <prefix> <in.sai> <in.fq>
     *
     * @param samFile   out.sam
     * @param index     prefix
     * @param saiFile   in.sai
     * @param fastqFile in.fq
     * @return command string
     */

    public String samse(File samFile, File index, File saiFile, File fastqFile) {
        return Execution + " samse -f " + samFile + " " + index + " " + saiFile + " " + fastqFile;
    }

    @Override
    public int compareTo(Bwa o) {
        return 0;
    }
}
