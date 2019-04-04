package Component.Statistic;

import Component.File.BedpeFile;
import Component.File.FastqFile;
import Component.File.SamFile;
import Component.Software.AbstractSoftware;
import Component.tool.Tools;
import Component.unit.LinkerSequence;
import Component.unit.Opts;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by 浩 on 2019/3/2.
 */
public class AlignmentStat extends AbstractStat {
    private long InputNum;
    public LinkerSequence[] Linkers = new LinkerSequence[0];
    public long[] LinkerInputNum = new long[0];
    public long[] LinkerR1Mapped = new long[0], LinkerR1Unmapped = new long[0], LinkerR1MultiMapped = new long[0];
    public long[] LinkerR2Mapped = new long[0], LinkerR2Unmapped = new long[0], LinkerR2MultiMapped = new long[0];
    public long[] MergeNum = new long[0];
    public int Threshold;
    public AbstractSoftware AlignmentSoftware;
    public File GenomeFile;
    public File GenomeIndex;
    public File OutDir;

    //------------------------------------------------------
    public FastqFile[] InputFile;
    public SamFile[] UniqueSamFile1, MultiSamFile1, UnmappedSamFile1;
    public SamFile[] UniqueSamFile2, MultiSamFile2, UnmappedSamFile2;
    public BedpeFile[] BedPeFile;

    @Override
    public void Stat() {
        for (int i = 0; i < Linkers.length; i++) {
            if (Opts.LFStat.ValidPairNum == null || Opts.LFStat.ValidPairNum[i] <= 0) {
                LinkerInputNum[i] = InputFile[i].getItemNum();
            }
            LinkerR1Mapped[i] = UniqueSamFile1[i].getItemNum();
            LinkerR1Unmapped[i] = UnmappedSamFile1[i].getItemNum();
            LinkerR1MultiMapped[i] = MultiSamFile1[i].getItemNum();
            //----------------------------------------------------------------------------------
            LinkerR2Mapped[i] = UniqueSamFile2[i].getItemNum();
            LinkerR2Unmapped[i] = UnmappedSamFile2[i].getItemNum();
            LinkerR2MultiMapped[i] = MultiSamFile2[i].getItemNum();
        }
    }

    public void Stat(int thread) {
        Thread[] t = new Thread[8];
        t[0] = new Thread(() -> {
            for (int i = 0; i < Linkers.length; i++) {
                if (Opts.LFStat.ValidPairNum == null || Opts.LFStat.ValidPairNum[i] <= 0) {
                    System.out.println(new Date() + "[Alignment statistic]:\tCalculate item number, file name: " + InputFile[i].getName());
                    LinkerInputNum[i] = InputFile[i].getItemNum();
                }
            }
        });
        t[1] = new Thread(() -> {
            for (int i = 0; i < Linkers.length; i++) {
                System.out.println(new Date() + "[Alignment statistic]:\tCalculate item number, file name: " + UniqueSamFile1[i].getName());
                LinkerR1Mapped[i] = UniqueSamFile1[i].getItemNum();
            }
        });
        t[2] = new Thread(() -> {
            for (int i = 0; i < Linkers.length; i++) {
                System.out.println(new Date() + "[Alignment statistic]:\tCalculate item number, file name: " + UnmappedSamFile1[i].getName());
                LinkerR1Unmapped[i] = UnmappedSamFile1[i].getItemNum();
            }
        });
        t[3] = new Thread(() -> {
            for (int i = 0; i < Linkers.length; i++) {
                System.out.println(new Date() + "[Alignment statistic]:\tCalculate item number, file name: " + MultiSamFile1[i].getName());
                LinkerR1MultiMapped[i] = MultiSamFile1[i].getItemNum();
            }
        });
        t[4] = new Thread(() -> {
            for (int i = 0; i < Linkers.length; i++) {
                System.out.println(new Date() + "[Alignment statistic]:\tCalculate item number, file name: " + UniqueSamFile2[i].getName());
                LinkerR2Mapped[i] = UniqueSamFile2[i].getItemNum();
            }
        });
        t[5] = new Thread(() -> {
            for (int i = 0; i < Linkers.length; i++) {
                System.out.println(new Date() + "[Alignment statistic]:\tCalculate item number, file name: " + UnmappedSamFile2[i].getName());
                LinkerR2Unmapped[i] = UnmappedSamFile2[i].getItemNum();
            }
        });
        t[6] = new Thread(() -> {
            for (int i = 0; i < Linkers.length; i++) {
                System.out.println(new Date() + "[Alignment statistic]:\tCalculate item number, file name: " + MultiSamFile2[i].getName());
                LinkerR2MultiMapped[i] = MultiSamFile2[i].getItemNum();
            }
        });
        t[7] = new Thread(() -> {
            for (int i = 0; i < Linkers.length; i++) {
                System.out.println(new Date() + "[Alignment statistic]:\tCalculate item number, file name: " + BedPeFile[i].getName());
                MergeNum[i] = BedPeFile[i].getItemNum();
            }
        });
        LinkedList<Thread> list = new LinkedList<>(Arrays.asList(t));
        t = new Thread[thread];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                Thread a;
                while (true) {
                    synchronized (list) {
                        if (list.size() <= 0) {
                            break;
                        }
                        a = list.remove(0);
                    }
                    a.start();
                    try {
                        a.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
    }

    @Override
    public String Show() {
        UpDate();
        StringBuilder show = new StringBuilder();
        show.append("=====================================Alignment Statistic========================================\n");
        show.append("Alignment software: ").append(AlignmentSoftware).append("\n");
        show.append("Genome file: \t").append(GenomeFile).append("\n");
        show.append("Genome index:\t").append(GenomeIndex).append("\n");
        show.append("Minimum unique mapped quality:\t").append(Threshold).append("\n");
        show.append("Output directory:\t").append(OutDir).append("\n");
        show.append("------------------------------------------------------------------------------------------------\n");
        for (int i = 0; i < Linkers.length; i++) {
            show.append("Linker \t").append(Linkers[i].getType()).append(":\t").append(Linkers[i].getSeq()).append("\n");
            show.append("Input: \t").append(new DecimalFormat("#,###").format(LinkerInputNum[i])).append("\n");
            show.append("R1:    \t").append("UniqueMapped: ").append(new DecimalFormat("#,###").format(LinkerR1Mapped[i])).append(" ").append(String.format("%.2f", (double) LinkerR1Mapped[i] / LinkerInputNum[i] * 100)).append("%").append("\t");
            show.append("Unmapped: ").append(new DecimalFormat("#,###").format(LinkerR1Unmapped[i])).append(" ").append(String.format("%.2f", (double) LinkerR1Unmapped[i] / LinkerInputNum[i] * 100)).append("%").append("\t");
            show.append("MultiMapped: ").append(new DecimalFormat("#,###").format(LinkerR1MultiMapped[i])).append(" ").append(String.format("%.2f", (double) LinkerR1MultiMapped[i] / LinkerInputNum[i] * 100)).append("%").append("\n");
            show.append("R2:    \t").append("UniqueMapped: ").append(new DecimalFormat("#,###").format(LinkerR2Mapped[i])).append(" ").append(String.format("%.2f", (double) LinkerR2Mapped[i] / LinkerInputNum[i] * 100)).append("%").append("\t");
            show.append("Unmapped: ").append(new DecimalFormat("#,###").format(LinkerR2Unmapped[i])).append(" ").append(String.format("%.2f", (double) LinkerR2Unmapped[i] / LinkerInputNum[i] * 100)).append("%").append("\t");
            show.append("MultiMapped: ").append(new DecimalFormat("#,###").format(LinkerR2MultiMapped[i])).append(" ").append(String.format("%.2f", (double) LinkerR2MultiMapped[i] / LinkerInputNum[i] * 100)).append("%").append("\n");
            show.append("Merge: \t").append(new DecimalFormat("#,###").format(MergeNum[i])).append(" ").append(String.format("%.2f", (double) MergeNum[i] / LinkerInputNum[i] * 100)).append("%").append("\n");
            show.append("\n");
        }
        show.append("Total:\n");
        show.append("Input:\t").append(new DecimalFormat("#,###").format(InputNum)).append("\n");
        show.append("R1:   \t").append("UniqueMapped: ").append(new DecimalFormat("#,###").format(StatUtil.sum(LinkerR1Mapped))).append(" ").append(String.format("%.2f", (double) StatUtil.sum(LinkerR1Mapped) / InputNum * 100)).append("%").append("\t");
        show.append("Unmapped: ").append(new DecimalFormat("#,###").format(StatUtil.sum(LinkerR1Unmapped))).append(" ").append(String.format("%.2f", (double) StatUtil.sum(LinkerR1Unmapped) / InputNum * 100)).append("%").append("\t");
        show.append("MultiMapped: ").append(new DecimalFormat("#,###").format(StatUtil.sum(LinkerR1MultiMapped))).append(" ").append(String.format("%.2f", (double) StatUtil.sum(LinkerR1MultiMapped) / InputNum * 100)).append("%").append("\n");
        show.append("R2:   \t").append("UniqueMapped: ").append(new DecimalFormat("#,###").format(StatUtil.sum(LinkerR2Mapped))).append(" ").append(String.format("%.2f", (double) StatUtil.sum(LinkerR2Mapped) / InputNum * 100)).append("%").append("\t");
        show.append("Unmapped: ").append(new DecimalFormat("#,###").format(StatUtil.sum(LinkerR2Unmapped))).append(" ").append(String.format("%.2f", (double) StatUtil.sum(LinkerR2Unmapped) / InputNum * 100)).append("%").append("\t");
        show.append("MultiMapped: ").append(new DecimalFormat("#,###").format(StatUtil.sum(LinkerR2MultiMapped))).append(" ").append(String.format("%.2f", (double) StatUtil.sum(LinkerR2MultiMapped) / InputNum * 100)).append("%").append("\n");
        show.append("Merge:\t").append(new DecimalFormat("#,###").format(StatUtil.sum(MergeNum))).append(" ").append(String.format("%.2f", (double) StatUtil.sum(MergeNum) / InputNum * 100)).append("%").append("\n");

        return show.toString();
    }

    @Override
    protected void UpDate() {
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
        MergeNum = new long[Linkers.length];
        //--------------------------------------------------------------------------
        InputFile = new FastqFile[Linkers.length];
        UniqueSamFile1 = new SamFile[Linkers.length];
        MultiSamFile1 = new SamFile[Linkers.length];
        UnmappedSamFile1 = new SamFile[Linkers.length];
        UniqueSamFile2 = new SamFile[Linkers.length];
        MultiSamFile2 = new SamFile[Linkers.length];
        UnmappedSamFile2 = new SamFile[Linkers.length];
        BedPeFile = new BedpeFile[Linkers.length];
    }
}
