package Component.Statistic;

import Component.File.SamFile;
import Component.unit.LinkerSequence;

/**
 * Created by 浩 on 2019/3/2.
 */
public class AlignmentStat extends AbstractStat {
    public LinkerSequence[] Linkers = new LinkerSequence[0];
    public long[] InputNum = new long[0];
    public long[] LinkerR1Mapped = new long[0], LinkerR1Unmapped = new long[0], LinkerR1MultiMapped = new long[0];
    public long[] LinkerR2Mapped = new long[0], LinkerR2Unmapped = new long[0], LinkerR2MultiMapped = new long[0];
    public long R1Mapped, R1Unmapped, R1MultiMapped;
    public long R2Mapped, R2Unmapped, R2MultiMapped;

    @Override
    public void Stat() {

    }

    @Override
    public String Show() {
        UpDate();
        return null;
    }

    @Override
    protected void UpDate() {
        R1Mapped = Stat.sum(LinkerR1Mapped);
        R1Unmapped = Stat.sum(LinkerR1Unmapped);
        R1MultiMapped = Stat.sum(LinkerR1MultiMapped);
        R2Mapped = Stat.sum(LinkerR2Mapped);
        R2Unmapped = Stat.sum(LinkerR2Unmapped);
        R2MultiMapped = Stat.sum(LinkerR2MultiMapped);
    }

    @Override
    public void Init() {
        InputNum = new long[Linkers.length];
        LinkerR1Mapped = new long[Linkers.length];
        LinkerR1Unmapped = new long[Linkers.length];
        LinkerR1MultiMapped = new long[Linkers.length];
        LinkerR2Mapped = new long[Linkers.length];
        LinkerR2Unmapped = new long[Linkers.length];
        LinkerR2MultiMapped = new long[Linkers.length];
    }
}
