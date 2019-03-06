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
        StringBuilder show = new StringBuilder();
        show.append("##===================================Alignment Statistic========================================\n");
        for (int i = 0; i < Linkers.length; i++) {
            show.append("Linker \t").append(Linkers[i].getType()).append(":\t").append(Linkers[i].getSeq()).append("\n");
            show.append("Input:\t").append(InputNum[i]).append("\n");
            show.append("R1:\t").append("Mapped: ").append(LinkerR1Mapped[i]).append(" ").append(String.format("%.2f", (double) LinkerR1Mapped[i] / InputNum[i])).append("%").append("\t");
            show.append("Unmapped: ").append(LinkerR1Unmapped[i]).append(" ").append(String.format("%.2f", (double) LinkerR1Unmapped[i] / InputNum[i])).append("\t");
            show.append("MultiMapped: ").append(LinkerR1MultiMapped[i]).append(" ").append(String.format("%.2f", (double) LinkerR1MultiMapped[i] / InputNum[i])).append("\n");
            show.append("R2:\t").append("Mapped: ").append(LinkerR2Mapped[i]).append(" ").append(String.format("%.2f", (double) LinkerR2Mapped[i] / InputNum[i])).append("%").append("\t");
            show.append("Unmapped: ").append(LinkerR2Unmapped[i]).append(" ").append(String.format("%.2f", (double) LinkerR2Unmapped[i] / InputNum[i])).append("\t");
            show.append("MultiMapped: ").append(LinkerR2MultiMapped[i]).append(" ").append(String.format("%.2f", (double) LinkerR2MultiMapped[i] / InputNum[i])).append("\n");
        }
        show.append("Total:\n");
        show.append("R1:\t").append("Mapped: ").append(R1Mapped).append(" ").append(String.format("%.2f", (double) R1Mapped / Stat.sum(InputNum))).append("%").append("\t");
        show.append("Unmapped: ").append(R1Unmapped).append(" ").append(String.format("%.2f", (double) R1Unmapped / Stat.sum(InputNum))).append("\t");
        show.append("MultiMapped: ").append(R1MultiMapped).append(" ").append(String.format("%.2f", (double) R1MultiMapped / Stat.sum(InputNum))).append("\n");
        show.append("R2:\t").append("Mapped: ").append(R2Mapped).append(" ").append(String.format("%.2f", (double) R2Mapped / Stat.sum(InputNum))).append("%").append("\t");
        show.append("Unmapped: ").append(R2Unmapped).append(" ").append(String.format("%.2f", (double) R2Unmapped / Stat.sum(InputNum))).append("\t");
        show.append("MultiMapped: ").append(R2MultiMapped).append(" ").append(String.format("%.2f", (double) R2MultiMapped / Stat.sum(InputNum))).append("\n");

        return show.toString();
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
