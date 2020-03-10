package Component.Software;


import Component.SystemDhat.CommandLineDhat;
import Component.unit.Configure;
import Component.unit.Opts;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * Created by snowf on 2019/3/10.
 */

public class Bwa extends AbstractSoftware implements Comparable<Bwa> {
    public File IndexPrefix;
    public File GenomeFile;
    public Opts.FileFormat IndexCheck = Opts.FileFormat.Undefine;

    public Bwa(String exe) {
        super(exe);
    }

    @Override
    protected void Init() {
        if (Execution.trim().equals("")) {
            System.err.println("[bwa]\tNo execute file");
        } else {
            if (!Path.isDirectory()) {
                FindPath();
            }
            getVersion();
        }
    }

    @Override
    protected String getVersion() {
        try {
            StringWriter buffer = new StringWriter();
            new CommandLineDhat().run(FullExe().toString(), null, new PrintWriter(buffer));
            for (String tempLine : buffer.toString().split("\\n")) {
                String[] s = tempLine.split("\\s*:\\s*");
                if (s[0].compareToIgnoreCase("Version") == 0) {
                    Version = s[1];
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            Valid = false;
        }
        return Version;
    }

    /**
     * Usage:   bwa index [options] <in.fasta>
     * <p>
     * Options:
     * </p>
     * -a STR    BWT construction algorithm: bwtsw, is or rb2 [auto]
     * -p STR    prefix of the index [same as fasta name]
     * -b INT    block size for the bwtsw algorithm (effective with -a bwtsw) [10000000]
     * -6        index files named as <in.fasta>.64.* instead of <in.fasta>.*
     *
     * @return command string
     */
    public String index(File genomeFile, File prefix) {
        return FullExe() + " index -p " + prefix + " " + genomeFile;
    }

    /**
     * Usage: bwa mem [options] <idxbase> <in1.fq> [in2.fq]
     * -t INT        number of threads [1]
     *
     * @return command string
     */
    public String mem(File fastqFile, int thread) {
        return FullExe() + " mem -t" + thread + " " + IndexPrefix + " " + fastqFile;
    }

    /**
     * Usage:   bwa aln [options] <prefix> <in.fq>
     * -n NUM    max #diff (int) or missing prob under 0.02 err rate (float) [0.04]
     * -t INT    number of threads [1]
     * -f FILE   file to write output to instead of stdout
     *
     * @return command string
     */
    public String aln(File fastqFile, File saiFile, int maxDiff, int thread) {
        return FullExe() + " aln -t " + thread + " -n " + maxDiff + " -f " + saiFile + " " + IndexPrefix + " " + fastqFile;
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
        return FullExe() + " samse -f " + samFile + " " + index + " " + saiFile + " " + fastqFile;
    }

    public boolean IndexCheck() {
        File amb, ann, bwt, pac, sa;
        amb = new File(IndexPrefix + ".amb");
        ann = new File(IndexPrefix + ".ann");
        bwt = new File(IndexPrefix + ".bwt");
        pac = new File(IndexPrefix + ".pac");
        sa = new File(IndexPrefix + ".sa");
        File[] list = new File[]{amb, ann, bwt, pac, sa};
        System.out.println("[Check index]\tCheck index file ......");
        for (File l : list) {
            System.out.println("[Check index]\tCheck " + l);
            if (!l.isFile()) {
                System.err.println("[Check index]\tWarning! Can't find " + l);
                IndexCheck = Opts.FileFormat.ErrorFormat;
                return false;
            }
        }
        System.out.println("[Check index]\tComplete index file");
        IndexCheck = Opts.FileFormat.Valid;
        return true;
    }

    public synchronized void CreateIndex(File genomeFile, File prefix, int threads) {
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
                new CommandLineDhat().run(s, null, null);
            } else {
                new CommandLineDhat().run(s, null, new PrintWriter(System.err));
            }
            GenomeFile = genomeFile;
            IndexPrefix = prefix;
            IndexCheck = Opts.FileFormat.Valid;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int compareTo(Bwa o) {
        return 0;
    }
}
