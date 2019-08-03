package Component.Statistic.NoiseReduce;

import Component.File.BedPeFile.BedpeFile;
import Component.unit.LinkerSequence;

/**
 * Created by snowf on 2019/3/10.
 */
public class Stat {
    public LinkerSequence Linker;
    public long RawDataNum;
    public long SelfLigationNum, ReLigationNum, DuplicateNum;
    public long SameCleanNum, DiffCleanNum, CleanNum;
    public long ShortRangeNum, LongRangeNum;
    public BedpeFile InputFile, SelfLigationFile, ReLigationFile, DuplicateFile, SameCleanFile, DiffCleanFile, CleanFile;

    public void clear() {
        Linker = null;
        RawDataNum = SelfLigationNum = ReLigationNum = DuplicateNum = SameCleanNum = DiffCleanNum = CleanNum = ShortRangeNum = LongRangeNum = 0;
    }
}
