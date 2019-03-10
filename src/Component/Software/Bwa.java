package Component.Software;


import java.io.File;

/**
 * Created by snowf on 2019/3/10.
 */

public class Bwa extends AbstractSoftware {
    Bwa(String exe) {
        super(exe);
    }

    @Override
    protected void Init() {

    }

    @Override
    protected float getVersion() {
        return 0;
    }

    @Override
    protected File getPath() {
        return new File(Execution);
    }

    /**
     * Usage: bwa mem [options] <idxbase> <in1.fq> [in2.fq]
     * -t INT        number of threads [1]
     *
     * @return command string
     */
    public String mem(String index, File fastqFile, int thread) {
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
    public String aln(String index, File fastqFile, File saiFile, int maxDiff, int thread) {
        return Execution + " aln -t " + thread + " -n " + maxDiff + " -f " + saiFile + " " + index + " " + fastqFile;
    }

    public String samse(File samFile, String index, File saiFile, File fastqFile) {
        return Execution + " samse -f " + samFile + " " + index + " " + saiFile + " " + fastqFile;
    }
}
