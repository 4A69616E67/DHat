package Component.Statistic.Alignment;

import Component.File.BedPeFile.BedpeFile;
import Component.File.FastQFile.FastqFile;
import Component.File.SamFile.SamFile;
import Component.unit.LinkerSequence;

public class Stat {
    public LinkerSequence Linker;
    public long InputNum;
    public long R1Mapped, R1Unmapped, R1MultiMapped;
    public long R2Mapped, R2Unmapped, R2MultiMapped;
    public long MergeNum;
    public FastqFile InputFile;
    public SamFile UniqueSamFile1, MultiSamFile1, UnmappedSamFile1;
    public SamFile UniqueSamFile2, MultiSamFile2, UnmappedSamFile2;
    public BedpeFile BedPeFile;
    public final String[] lock = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};

    public void clear() {
        Linker = null;
        InputNum = R1Mapped = R1Unmapped = R1MultiMapped = R2Mapped = R2Unmapped = R2MultiMapped = MergeNum = 0;
    }
}
