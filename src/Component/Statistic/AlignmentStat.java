package Component.Statistic;

import Component.Software.AbstractSoftware;
import Component.unit.LinkerSequence;
import Component.unit.Opts;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by 浩 on 2019/3/2.
 */
public class AlignmentStat extends AbstractStat {
    private long InputNum;
    public LinkerSequence[] Linkers = new LinkerSequence[0];
    public long[] LinkerInputNum = new long[0];
    public int Threshold;
    public AbstractSoftware AlignmentSoftware;
    public File GenomeFile;
    public File GenomeIndex;
    public File OutDir;
    public long[] LinkerR1Mapped = new long[0], LinkerR1Unmapped = new long[0], LinkerR1MultiMapped = new long[0];
    public long[] LinkerR2Mapped = new long[0], LinkerR2Unmapped = new long[0], LinkerR2MultiMapped = new long[0];
    private long R1Mapped, R1Unmapped, R1MultiMapped;
    private long R2Mapped, R2Unmapped, R2MultiMapped;


    @Override
    public void Stat() {

    }

    @Override
    public String Show() {
        UpDate();
        StringBuilder show = new StringBuilder();
        show.append("##===================================Alignment Statistic========================================\n");
        show.append("Alignment software: ").append(AlignmentSoftware).append("\n");
        show.append("Genome file: \t").append(GenomeFile).append("\n");
        show.append("Genome index:\t").append(GenomeIndex).append("\n");
        show.append("Minimum unique mapped quality:\t").append(Threshold).append("\n");
        show.append("Output directory:\t").append(OutDir).append("\n");
        show.append("------------------------------------------------------------------------------------------------\n");
        for (int i = 0; i < Linkers.length; i++) {
            show.append("Linker \t").append(Linkers[i].getType()).append(":\t").append(Linkers[i].getSeq()).append("\n");
            show.append("Input:\t").append(new DecimalFormat("#,###").format(LinkerInputNum[i])).append("\n");
            show.append("R1:\t").append("UniqueMapped: ").append(new DecimalFormat("#,###").format(LinkerR1Mapped[i])).append(" ").append(String.format("%.2f", (double) LinkerR1Mapped[i] / LinkerInputNum[i] * 100)).append("%").append("\t");
            show.append("Unmapped: ").append(new DecimalFormat("#,###").format(LinkerR1Unmapped[i])).append(" ").append(String.format("%.2f", (double) LinkerR1Unmapped[i] / LinkerInputNum[i] * 100)).append("%").append("\t");
            show.append("MultiMapped: ").append(new DecimalFormat("#,###").format(LinkerR1MultiMapped[i])).append(" ").append(String.format("%.2f", (double) LinkerR1MultiMapped[i] / LinkerInputNum[i] * 100)).append("%").append("\n");
            show.append("R2:\t").append("UniqueMapped: ").append(new DecimalFormat("#,###").format(LinkerR2Mapped[i])).append(" ").append(String.format("%.2f", (double) LinkerR2Mapped[i] / LinkerInputNum[i] * 100)).append("%").append("\t");
            show.append("Unmapped: ").append(new DecimalFormat("#,###").format(LinkerR2Unmapped[i])).append(" ").append(String.format("%.2f", (double) LinkerR2Unmapped[i] / LinkerInputNum[i] * 100)).append("%").append("\t");
            show.append("MultiMapped: ").append(new DecimalFormat("#,###").format(LinkerR2MultiMapped[i])).append(" ").append(String.format("%.2f", (double) LinkerR2MultiMapped[i] / LinkerInputNum[i] * 100)).append("%").append("\n");
            show.append("\n");
        }
        show.append("Total:\n");
        show.append("Input:\t").append(new DecimalFormat("#,###").format(InputNum)).append("\n");
        show.append("R1:\t").append("UniqueMapped: ").append(new DecimalFormat("#,###").format(R1Mapped)).append(" ").append(String.format("%.2f", (double) R1Mapped / InputNum * 100)).append("%").append("\t");
        show.append("Unmapped: ").append(new DecimalFormat("#,###").format(R1Unmapped)).append(" ").append(String.format("%.2f", (double) R1Unmapped / InputNum * 100)).append("%").append("\t");
        show.append("MultiMapped: ").append(new DecimalFormat("#,###").format(R1MultiMapped)).append(" ").append(String.format("%.2f", (double) R1MultiMapped / InputNum * 100)).append("%").append("\n");
        show.append("R2:\t").append("UniqueMapped: ").append(new DecimalFormat("#,###").format(R2Mapped)).append(" ").append(String.format("%.2f", (double) R2Mapped / InputNum * 100)).append("%").append("\t");
        show.append("Unmapped: ").append(new DecimalFormat("#,###").format(R2Unmapped)).append(" ").append(String.format("%.2f", (double) R2Unmapped / InputNum * 100)).append("%").append("\t");
        show.append("MultiMapped: ").append(new DecimalFormat("#,###").format(R2MultiMapped)).append(" ").append(String.format("%.2f", (double) R2MultiMapped / InputNum * 100)).append("%").append("\n");

        return show.toString();
    }

    @Override
    protected void UpDate() {
        R1Mapped = StatUtil.sum(LinkerR1Mapped);
        R1Unmapped = StatUtil.sum(LinkerR1Unmapped);
        R1MultiMapped = StatUtil.sum(LinkerR1MultiMapped);
        R2Mapped = StatUtil.sum(LinkerR2Mapped);
        R2Unmapped = StatUtil.sum(LinkerR2Unmapped);
        R2MultiMapped = StatUtil.sum(LinkerR2MultiMapped);
        Opts.OVStat.AlignmentNum = InputNum = StatUtil.sum(LinkerInputNum);
    }

    @Override
    public void Init() {
        LinkerInputNum = new long[Linkers.length];
        LinkerR1Mapped = new long[Linkers.length];
        LinkerR1Unmapped = new long[Linkers.length];
        LinkerR1MultiMapped = new long[Linkers.length];
        LinkerR2Mapped = new long[Linkers.length];
        LinkerR2Unmapped = new long[Linkers.length];
        LinkerR2MultiMapped = new long[Linkers.length];
    }
}
