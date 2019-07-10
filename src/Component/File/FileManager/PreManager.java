package Component.File.FileManager;

import Component.File.FastQFile.FastqFile;
import Component.tool.LinkerFiltering;
import Component.unit.LinkerSequence;

import java.io.File;
import java.io.IOException;

/**
 * Created by snowf on 2019/7/10.
 */

public class PreManager {
    public FastqFile InputFile;
    public Component.File.FastQFile.FastqFile FastqFile;//Fastq文件
    //=================================================================
    public File LinkerFilterOutFile;
    public FastqFile[] FastqR1File, FastqR2File;


    public PreManager Generate(File outPath, String prefix, LinkerSequence[] linkers, File linkerFile) throws IOException {
        FastqR1File = new FastqFile[linkers.length];
        FastqR2File = new FastqFile[linkers.length];
        for (int i = 0; i < linkers.length; i++) {
            FastqR1File[i] = new FastqFile(outPath + "/" + prefix + "." + linkers[i].getType() + ".R1.fastq");
            FastqR2File[i] = new FastqFile(outPath + "/" + prefix + "." + linkers[i].getType() + ".R2.fastq");
        }
        String LinkerFilterOutPrefix;//linker过滤输出前缀
        LinkerFilterOutPrefix = outPath + "/" + prefix + ".linkerfilter";
        LinkerFilterOutFile = new LinkerFiltering(FastqFile, linkerFile, LinkerFilterOutPrefix, 1).getOutFile();
        return this;
    }
}
