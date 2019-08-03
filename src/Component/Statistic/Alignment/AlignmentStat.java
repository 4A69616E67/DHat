package Component.Statistic.Alignment;

import Component.File.AbstractFile;
import Component.Software.AbstractSoftware;
import Component.Statistic.AbstractStat;
import Component.tool.Tools;
import Component.unit.LinkerSequence;
import Component.unit.Opts;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by 浩 on 2019/3/2.
 */
public class AlignmentStat extends AbstractStat {
    public Stat[] linkers;
    public Stat total = new Stat();
    public LinkerSequence[] Linkers = new LinkerSequence[0];
    public int Threshold;
    public AbstractSoftware AlignmentSoftware;
    public File GenomeFile;
    public File GenomeIndex;
    public File OutDir;


    @Override
    public void Stat() {
        for (int i = 0; i < linkers.length; i++) {
            if (Opts.LFStat.linkers == null || Opts.LFStat.linkers[i].ValidPairNum <= 0) {
                linkers[i].InputNum = linkers[i].InputFile.getItemNum();
            }
            linkers[i].R1Mapped = linkers[i].UniqueSamFile1.getItemNum();
            linkers[i].R1Unmapped = linkers[i].UnmappedSamFile1.getItemNum();
            linkers[i].R1MultiMapped = linkers[i].MultiSamFile1.getItemNum();
            //----------------------------------------------------------------------------------
            linkers[i].R2Mapped = linkers[i].UniqueSamFile2.getItemNum();
            linkers[i].R2Unmapped = linkers[i].UnmappedSamFile2.getItemNum();
            linkers[i].R2MultiMapped = linkers[i].MultiSamFile2.getItemNum();
        }
    }

    public void Stat(int thread) {
        LinkedList<AbstractFile> list = new LinkedList<>();
        for (int i = 0; i < Linkers.length; i++) {
            list.add(linkers[i].InputFile);
            list.add(linkers[i].UniqueSamFile1);
            list.add(linkers[i].UnmappedSamFile1);
            list.add(linkers[i].MultiSamFile1);
            list.add(linkers[i].UniqueSamFile2);
            list.add(linkers[i].UnmappedSamFile2);
            list.add(linkers[i].MultiSamFile2);
            list.add(linkers[i].BedPeFile);
        }
        Thread[] t = new Thread[thread];
        int[] index = new int[]{0};
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                AbstractFile temp;
                while (true) {
                    synchronized (t) {
                        if (index[0] >= list.size()) {
                            break;
                        }
                        temp = list.get(index[0]);
                        index[0]++;
                    }
                    System.out.println(new Date() + " [Alignment statistic]:\tCalculate item number, file name: " + temp.getName());
                    temp.getItemNum();
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        for (int i = 0; i < Linkers.length; i++) {
            linkers[i].InputNum = list.removeFirst().getItemNum();
            linkers[i].R1Mapped = list.removeFirst().getItemNum();
            linkers[i].R1Unmapped = list.removeFirst().getItemNum();
            linkers[i].R1MultiMapped = list.removeFirst().getItemNum();
            linkers[i].R2Mapped = list.removeFirst().getItemNum();
            linkers[i].R2Unmapped = list.removeFirst().getItemNum();
            linkers[i].R2MultiMapped = list.removeFirst().getItemNum();
            linkers[i].MergeNum = list.removeFirst().getItemNum();
        }
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
        for (int i = 0; i < linkers.length; i++) {
            show.append("Linker \t").append(linkers[i].Linker.getType()).append(":\t").append(linkers[i].Linker.getSeq()).append("\n");
            show.append("Input: \t").append(new DecimalFormat("#,###").format(linkers[i].InputNum)).append("\n");
            show.append("R1:    \t").append("UniqueMapped: ").append(new DecimalFormat("#,###").format(linkers[i].R1Mapped)).append(" ").append(String.format("%.2f", (double) linkers[i].R1Mapped / linkers[i].InputNum * 100)).append("%").append("\t");
            show.append("Unmapped: ").append(new DecimalFormat("#,###").format(linkers[i].R1Unmapped)).append(" ").append(String.format("%.2f", (double) linkers[i].R1Unmapped / linkers[i].InputNum * 100)).append("%").append("\t");
            show.append("MultiMapped: ").append(new DecimalFormat("#,###").format(linkers[i].R1MultiMapped)).append(" ").append(String.format("%.2f", (double) linkers[i].R1MultiMapped / linkers[i].InputNum * 100)).append("%").append("\n");
            show.append("R2:    \t").append("UniqueMapped: ").append(new DecimalFormat("#,###").format(linkers[i].R2Mapped)).append(" ").append(String.format("%.2f", (double) linkers[i].R2Mapped / linkers[i].InputNum * 100)).append("%").append("\t");
            show.append("Unmapped: ").append(new DecimalFormat("#,###").format(linkers[i].R2Unmapped)).append(" ").append(String.format("%.2f", (double) linkers[i].R2Unmapped / linkers[i].InputNum * 100)).append("%").append("\t");
            show.append("MultiMapped: ").append(new DecimalFormat("#,###").format(linkers[i].R2MultiMapped)).append(" ").append(String.format("%.2f", (double) linkers[i].R2MultiMapped / linkers[i].InputNum * 100)).append("%").append("\n");
            show.append("Merge: \t").append(new DecimalFormat("#,###").format(linkers[i].MergeNum)).append(" ").append(String.format("%.2f", (double) linkers[i].MergeNum / linkers[i].InputNum * 100)).append("%").append("\n");
            show.append("\n");
        }
        show.append("Total:\n");
        show.append("Input:\t").append(new DecimalFormat("#,###").format(total.InputNum)).append("\n");
        show.append("R1:   \t").append("UniqueMapped: ").append(new DecimalFormat("#,###").format(total.R1Mapped)).append(" ").append(String.format("%.2f", (double) total.R1Mapped / total.InputNum * 100)).append("%").append("\t");
        show.append("Unmapped: ").append(new DecimalFormat("#,###").format(total.R1Unmapped)).append(" ").append(String.format("%.2f", (double) total.R1Unmapped / total.InputNum * 100)).append("%").append("\t");
        show.append("MultiMapped: ").append(new DecimalFormat("#,###").format(total.R1MultiMapped)).append(" ").append(String.format("%.2f", (double) total.R1MultiMapped / total.InputNum * 100)).append("%").append("\n");
        show.append("R2:   \t").append("UniqueMapped: ").append(new DecimalFormat("#,###").format(total.R2Mapped)).append(" ").append(String.format("%.2f", (double) total.R2Mapped / total.InputNum * 100)).append("%").append("\t");
        show.append("Unmapped: ").append(new DecimalFormat("#,###").format(total.R2Unmapped)).append(" ").append(String.format("%.2f", (double) total.R2Unmapped / total.InputNum * 100)).append("%").append("\t");
        show.append("MultiMapped: ").append(new DecimalFormat("#,###").format(total.R2MultiMapped)).append(" ").append(String.format("%.2f", (double) total.R2MultiMapped / total.InputNum * 100)).append("%").append("\n");
        show.append("Merge:\t").append(new DecimalFormat("#,###").format(total.MergeNum)).append(" ").append(String.format("%.2f", (double) total.MergeNum / total.InputNum * 100)).append("%").append("\n");

        return show.toString();
    }

    @Override
    protected void UpDate() {
        total.clear();
        for (Stat linker : linkers) {
            total.InputNum += linker.InputNum;
            total.R1Mapped += linker.R1Mapped;
            total.R1Unmapped += linker.R1Unmapped;
            total.R1MultiMapped += linker.R1MultiMapped;
            total.R2Mapped += linker.R2Mapped;
            total.R2Unmapped += linker.R2Unmapped;
            total.R2MultiMapped += linker.R2MultiMapped;
            total.MergeNum += linker.MergeNum;
        }
    }

    @Override
    public void Init() {
        linkers = new Stat[Linkers.length];
        for (int i = 0; i < Linkers.length; i++) {
            linkers[i] = new Stat();
            linkers[i].Linker = Linkers[i];
        }
    }
}
