package Component.Statistic;

import Component.File.BedpeFile;
import Component.tool.Tools;
import Component.unit.BedpeItem;
import Component.unit.LinkerSequence;
import Component.unit.Region;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by snowf on 2019/3/10.
 */

public class NoiseReduceStat extends AbstractStat {
    public LinkerSequence[] Linkers;
    public long[] LinkerRawDataNum, LinkerSelfLigationNum, LinkerReLigationNum, LinkerRepeatNum, LinkerSameCleanNum, LinkerDiffCleanNum, LinkerCleanNum, LinkerShortRangeNum, LinkerLongRangeNum;
    public long RawDataNum, SelfLigationNum, ReLigationNum, RepeatNum, SameCleanNum, DiffCleanNum, CleanNum;
    public long ShortRangeNum, LongRangeNum;
    public HashMap<Integer, Integer> InteractionRangeDistribution = new HashMap<>();
    public Region ShortRegion, LongRegion;
    public File OutDir;

    //-----------------------------------------------------------------------------
    public BedpeFile[] InputFile, SelfLigationFile, ReLigationFile, RepeatFile, SameCleanFile, DiffCleanFile, CleanFile;

    @Override

    public void Stat() {
        Stat(1);
    }

    public void Stat(int thread) {
        if (thread <= 0) {
            thread = 1;
        }
        ShortRangeNum = 0;
        LongRangeNum = 0;
        LinkedList<BedpeFile> list = new LinkedList<>();
        for (int i = 0; i < Linkers.length; i++) {
            list.add(InputFile[i]);
            list.add(SelfLigationFile[i]);
            list.add(ReLigationFile[i]);
            list.add(RepeatFile[i]);
            list.add(DiffCleanFile[i]);
            list.add(CleanFile[i]);
        }
        int[] index = new int[]{0};
        Thread[] t = new Thread[thread];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                BedpeFile temp;
                while (true) {
                    synchronized (t) {
                        if (index[0] >= list.size()) {
                            break;
                        }
                        temp = list.get(index[0]);
                        index[0]++;
                    }
                    System.out.println(new Date() + " [Noise Reduce statistic]:\tCalculate item number, file name: " + temp.getName());
                    temp.getItemNum();
                }
                while (true) {
                    int j;
                    synchronized (t) {
                        if (index[0] >= list.size() + Linkers.length) {
                            break;
                        }
                        j = index[0] - list.size();
                        temp = SameCleanFile[j];
                        temp.ItemNum = 0;
                        index[0]++;
                    }
                    System.out.println(new Date() + " [Noise Reduce statistic]:\tCalculate the number of short and long range, file name: " + temp.getName());
                    try {
                        long[] result = RangeCount(temp);
                        LinkerShortRangeNum[j] = result[0];
                        LinkerLongRangeNum[j] = result[1];
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        for (int i = 0; i < Linkers.length; i++) {
            LinkerRawDataNum[i] = InputFile[i].getItemNum();
            LinkerSelfLigationNum[i] = SelfLigationFile[i].getItemNum();
            LinkerReLigationNum[i] = ReLigationFile[i].getItemNum();
            LinkerRepeatNum[i] = RepeatFile[i].getItemNum();
            LinkerSameCleanNum[i] = SameCleanFile[i].getItemNum();
            LinkerDiffCleanNum[i] = DiffCleanFile[i].getItemNum();
            LinkerCleanNum[i] = CleanFile[i].getItemNum();
        }

    }

    public long[] RangeCount(BedpeFile inFile) throws IOException {
        long[] result = new long[2];
        try {
            inFile.ReadOpen();
        } catch (IOException e) {
            System.err.println("[" + this.getClass().getName() + "] " + e.getMessage());
            return new long[]{0, 0};
        }
        BedpeItem item;
        while ((item = inFile.ReadItem()) != null) {
            int distance = item.getLocation().Distance();
            if (ShortRegion.IsContain(distance)) {
                result[0]++;
            } else if (LongRegion.IsContain(distance)) {
                result[1]++;
            }
            synchronized (this) {
                if (!InteractionRangeDistribution.containsKey(distance)) {
                    InteractionRangeDistribution.put(distance, 0);
                }
                InteractionRangeDistribution.put(distance, InteractionRangeDistribution.get(distance) + 1);
            }
            inFile.ItemNum++;
        }
        inFile.ReadClose();
        return result;
    }

    @Override
    public String Show() {
        //show.append( ).append("\t").append(new DecimalFormat("#,###").format( )).append("\t").append(String.format("%.2f", )).append("%").append("\n");
        UpDate();
        StringBuilder show = new StringBuilder();
        show.append("##=================================Noise reduce Statistic=======================================\n");
        show.append("Short range: <= ").append(ShortRegion.End).append("\n");
        show.append("Long range:  >  ").append(LongRegion.Start).append("\n");
        show.append("Output directory:\t").append(OutDir).append("\n");
        show.append("------------------------------------------------------------------------------------------------\n");
        for (int i = 0; i < Linkers.length; i++) {
            show.append("Linker ").append(Linkers[i].getType()).append(":\t").append(Linkers[i].getSeq()).append("\n");
            show.append("Raw data:     \t").append(new DecimalFormat("#,###").format(LinkerRawDataNum[i])).append("\n");
            show.append("Self-ligation:\t").append(new DecimalFormat("#,###").format(LinkerSelfLigationNum[i])).append("\t").append(String.format("%.2f", (double) LinkerSelfLigationNum[i] / LinkerRawDataNum[i] * 100)).append("%").append("\t");
            show.append("Re-ligation: ").append(new DecimalFormat("#,###").format(LinkerReLigationNum[i])).append(" ").append(String.format("%.2f", (double) LinkerReLigationNum[i] / LinkerRawDataNum[i] * 100)).append("%").append("\t");
            show.append("Repeat: ").append(new DecimalFormat("#,###").format(LinkerRepeatNum[i])).append(" ").append(String.format("%.2f", (double) LinkerRepeatNum[i] / LinkerRawDataNum[i] * 100)).append("%").append("\n");
            show.append("Clean data:   \t").append(new DecimalFormat("#,###").format(LinkerCleanNum[i])).append("\t").append(String.format("%.2f", (double) LinkerCleanNum[i] / LinkerRawDataNum[i] * 100)).append("%").append("\n");
            show.append("Intra-action: \t").append(new DecimalFormat("#,###").format(LinkerSameCleanNum[i])).append("\t").append(String.format("%.2f", (double) LinkerSameCleanNum[i] / LinkerCleanNum[i] * 100)).append("%").append("\t");
            show.append("Inter-action: ").append(new DecimalFormat("#,###").format(LinkerDiffCleanNum[i])).append("\t").append(String.format("%.2f", (double) LinkerDiffCleanNum[i] / LinkerCleanNum[i] * 100)).append("%").append("\n");
            show.append("Short range:  \t").append(new DecimalFormat("#,###").format(LinkerShortRangeNum[i])).append("\t").append(String.format("%.2f", (double) LinkerShortRangeNum[i] / LinkerSameCleanNum[i] * 100)).append("%").append("\t");
            show.append("Long range: ").append(new DecimalFormat("#,###").format(LinkerLongRangeNum[i])).append("\t").append(String.format("%.2f", (double) LinkerLongRangeNum[i] / LinkerSameCleanNum[i] * 100)).append("%").append("\n");
            show.append("\n");
        }
        show.append("Total:\n");
        show.append("Raw data:     \t").append(new DecimalFormat("#,###").format(RawDataNum)).append("\n");
        show.append("Self-ligation:\t").append(new DecimalFormat("#,###").format(SelfLigationNum)).append("\t").append(String.format("%.2f", (double) SelfLigationNum / RawDataNum * 100)).append("%").append("\t");
        show.append("Re-ligation: ").append(new DecimalFormat("#,###").format(ReLigationNum)).append(" ").append(String.format("%.2f", (double) ReLigationNum / RawDataNum * 100)).append("%").append("\t");
        show.append("Repeat: ").append(new DecimalFormat("#,###").format(RepeatNum)).append(" ").append(String.format("%.2f", (double) RepeatNum / RawDataNum * 100)).append("%").append("\n");
        show.append("Clean data:   \t").append(new DecimalFormat("#,###").format(CleanNum)).append("\t").append(String.format("%.2f", (double) CleanNum / RawDataNum * 100)).append("%").append("\n");
        show.append("Intra-action: \t").append(new DecimalFormat("#,###").format(SameCleanNum)).append("\t").append(String.format("%.2f", (double) SameCleanNum / CleanNum * 100)).append("%").append("\t");
        show.append("Inter-action: ").append(new DecimalFormat("#,###").format(DiffCleanNum)).append("\t").append(String.format("%.2f", (double) DiffCleanNum / CleanNum * 100)).append("%").append("\n");
        show.append("Short range:  \t").append(new DecimalFormat("#,###").format(ShortRangeNum)).append("\t").append(String.format("%.2f", (double) ShortRangeNum / SameCleanNum * 100)).append("%").append("\t");
        show.append("Long range: ").append(new DecimalFormat("#,###").format(LongRangeNum)).append("\t").append(String.format("%.2f", (double) LongRangeNum / SameCleanNum * 100)).append("%").append("\n");
        show.append("\n");
        return show.toString();
    }

    @Override
    protected void UpDate() {
        RawDataNum = StatUtil.sum(LinkerRawDataNum);
        SelfLigationNum = StatUtil.sum(LinkerSelfLigationNum);
        ReLigationNum = StatUtil.sum(LinkerReLigationNum);
        RepeatNum = StatUtil.sum(LinkerRepeatNum);
        SameCleanNum = StatUtil.sum(LinkerSameCleanNum);
        DiffCleanNum = StatUtil.sum(LinkerDiffCleanNum);
        CleanNum = StatUtil.sum(LinkerCleanNum);
        ShortRangeNum = StatUtil.sum(LinkerShortRangeNum);
        LongRangeNum = StatUtil.sum(LinkerLongRangeNum);
    }

    @Override
    public void Init() {
        LinkerRawDataNum = new long[Linkers.length];
        LinkerSelfLigationNum = new long[Linkers.length];
        LinkerReLigationNum = new long[Linkers.length];
        LinkerRepeatNum = new long[Linkers.length];
        LinkerSameCleanNum = new long[Linkers.length];
        LinkerDiffCleanNum = new long[Linkers.length];
        LinkerCleanNum = new long[Linkers.length];
        LinkerShortRangeNum = new long[Linkers.length];
        LinkerLongRangeNum = new long[Linkers.length];
        //-------------------------------------------------------------------
        InputFile = new BedpeFile[Linkers.length];
        SelfLigationFile = new BedpeFile[Linkers.length];
        ReLigationFile = new BedpeFile[Linkers.length];
        RepeatFile = new BedpeFile[Linkers.length];
        SameCleanFile = new BedpeFile[Linkers.length];
        DiffCleanFile = new BedpeFile[Linkers.length];
        CleanFile = new BedpeFile[Linkers.length];
    }
}
