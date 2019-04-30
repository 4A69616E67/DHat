package Component.Statistic.NoiseReduce;

import Component.File.BedpeFile;
import Component.Statistic.AbstractStat;
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


public class NoiseReduceStat extends AbstractStat {
    public LinkerSequence[] Linkers;
    public Stat[] linkers;
    public Stat total = new Stat();
    //    public long[] LinkerRawDataNum, LinkerSelfLigationNum, LinkerReLigationNum, LinkerRepeatNum, LinkerSameCleanNum, LinkerDiffCleanNum, LinkerCleanNum, LinkerShortRangeNum, LinkerLongRangeNum;
//    public long RawDataNum, SelfLigationNum, ReLigationNum, RepeatNum, SameCleanNum, DiffCleanNum, CleanNum;
//    public long ShortRangeNum, LongRangeNum;
    public HashMap<Integer, Integer> InteractionRangeDistribution = new HashMap<>();
    public Region ShortRegion, LongRegion;
    public File OutDir;

    //-----------------------------------------------------------------------------
//    public BedpeFile[] InputFile, SelfLigationFile, ReLigationFile, DuplicateFile, SameCleanFile, DiffCleanFile, CleanFile;

    @Override

    public void Stat() {
        Stat(1);
    }

    public void Stat(int thread) {
        if (thread <= 0) {
            thread = 1;
        }
        total.ShortRangeNum = 0;
        total.LongRangeNum = 0;
        LinkedList<BedpeFile> list = new LinkedList<>();
        for (int i = 0; i < Linkers.length; i++) {
            list.add(linkers[i].InputFile);
            list.add(linkers[i].SelfLigationFile);
            list.add(linkers[i].ReLigationFile);
            list.add(linkers[i].DuplicateFile);
            list.add(linkers[i].DiffCleanFile);
            list.add(linkers[i].CleanFile);
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
                        temp = linkers[j].SameCleanFile;
                        temp.ItemNum = 0;
                        index[0]++;
                    }
                    System.out.println(new Date() + " [Noise Reduce statistic]:\tCalculate the number of short and long range, file name: " + temp.getName());
                    try {
                        long[] result = RangeCount(temp);
                        linkers[j].ShortRangeNum = result[0];
                        linkers[j].LongRangeNum = result[1];
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        for (int i = 0; i < Linkers.length; i++) {
            linkers[i].RawDataNum = linkers[i].InputFile.getItemNum();
            linkers[i].SelfLigationNum = linkers[i].SelfLigationFile.getItemNum();
            linkers[i].ReLigationNum = linkers[i].ReLigationFile.getItemNum();
            linkers[i].DuplicateNum = linkers[i].DuplicateFile.getItemNum();
            linkers[i].SameCleanNum = linkers[i].SameCleanFile.getItemNum();
            linkers[i].DiffCleanNum = linkers[i].DiffCleanFile.getItemNum();
            linkers[i].CleanNum = linkers[i].CleanFile.getItemNum();
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
        for (int i = 0; i < linkers.length; i++) {
            show.append("Linker ").append(linkers[i].Linker.getType()).append(":\t").append(linkers[i].Linker.getSeq()).append("\n");
            show.append("Raw data:        \t").append(new DecimalFormat("#,###").format(linkers[i].RawDataNum)).append("\n");
            show.append("Self-ligation:   \t").append(new DecimalFormat("#,###").format(linkers[i].SelfLigationNum)).append("\t").append(String.format("%.2f", (double) linkers[i].SelfLigationNum / linkers[i].RawDataNum * 100)).append("%").append("\t");
            show.append("Re-ligation: ").append(new DecimalFormat("#,###").format(linkers[i].ReLigationNum)).append(" ").append(String.format("%.2f", (double) linkers[i].ReLigationNum / linkers[i].RawDataNum * 100)).append("%").append("\t");
            show.append("Duplicate: ").append(new DecimalFormat("#,###").format(linkers[i].DuplicateNum)).append(" ").append(String.format("%.2f", (double) linkers[i].DuplicateNum / linkers[i].RawDataNum * 100)).append("%").append("\n");
            show.append("Clean data:      \t").append(new DecimalFormat("#,###").format(linkers[i].CleanNum)).append("\t").append(String.format("%.2f", (double) linkers[i].CleanNum / linkers[i].RawDataNum * 100)).append("%").append("\n");
            show.append("Intra-chromosome:\t").append(new DecimalFormat("#,###").format(linkers[i].SameCleanNum)).append("\t").append(String.format("%.2f", (double) linkers[i].SameCleanNum / linkers[i].CleanNum * 100)).append("%").append("\t");
            show.append("Inter-chromosome: ").append(new DecimalFormat("#,###").format(linkers[i].DiffCleanNum)).append("\t").append(String.format("%.2f", (double) linkers[i].DiffCleanNum / linkers[i].CleanNum * 100)).append("%").append("\n");
            show.append("Short range:     \t").append(new DecimalFormat("#,###").format(linkers[i].ShortRangeNum)).append("\t").append(String.format("%.2f", (double) linkers[i].ShortRangeNum / linkers[i].SameCleanNum * 100)).append("%").append("\t");
            show.append("Long range: ").append(new DecimalFormat("#,###").format(linkers[i].LongRangeNum)).append("\t").append(String.format("%.2f", (double) linkers[i].LongRangeNum / linkers[i].SameCleanNum * 100)).append("%").append("\n");
            show.append("\n");
        }
        show.append("Total:\n");
        show.append("Raw data:        \t").append(new DecimalFormat("#,###").format(total.RawDataNum)).append("\n");
        show.append("Self-ligation:   \t").append(new DecimalFormat("#,###").format(total.SelfLigationNum)).append("\t").append(String.format("%.2f", (double) total.SelfLigationNum / total.RawDataNum * 100)).append("%").append("\t");
        show.append("Re-ligation: ").append(new DecimalFormat("#,###").format(total.ReLigationNum)).append(" ").append(String.format("%.2f", (double) total.ReLigationNum / total.RawDataNum * 100)).append("%").append("\t");
        show.append("Duplicate: ").append(new DecimalFormat("#,###").format(total.DuplicateNum)).append(" ").append(String.format("%.2f", (double) total.DuplicateNum / total.RawDataNum * 100)).append("%").append("\n");
        show.append("Clean data:      \t").append(new DecimalFormat("#,###").format(total.CleanNum)).append("\t").append(String.format("%.2f", (double) total.CleanNum / total.RawDataNum * 100)).append("%").append("\n");
        show.append("Intra-chromosome:\t").append(new DecimalFormat("#,###").format(total.SameCleanNum)).append("\t").append(String.format("%.2f", (double) total.SameCleanNum / total.CleanNum * 100)).append("%").append("\t");
        show.append("Inter-chromosome: ").append(new DecimalFormat("#,###").format(total.DiffCleanNum)).append("\t").append(String.format("%.2f", (double) total.DiffCleanNum / total.CleanNum * 100)).append("%").append("\n");
        show.append("Short range:     \t").append(new DecimalFormat("#,###").format(total.ShortRangeNum)).append("\t").append(String.format("%.2f", (double) total.ShortRangeNum / total.SameCleanNum * 100)).append("%").append("\t");
        show.append("Long range: ").append(new DecimalFormat("#,###").format(total.LongRangeNum)).append("\t").append(String.format("%.2f", (double) total.LongRangeNum / total.SameCleanNum * 100)).append("%").append("\n");
        show.append("\n");
        return show.toString();
    }

    @Override
    protected void UpDate() {
        total.clear();
        for (Stat linker : linkers) {
            total.RawDataNum += linker.RawDataNum;
            total.SelfLigationNum += linker.SelfLigationNum;
            total.ReLigationNum += linker.ReLigationNum;
            total.DuplicateNum += linker.DuplicateNum;
            total.SameCleanNum += linker.SameCleanNum;
            total.DiffCleanNum += linker.DiffCleanNum;
            total.CleanNum += linker.CleanNum;
            total.ShortRangeNum += linker.ShortRangeNum;
            total.LongRangeNum += linker.LongRangeNum;
        }
    }

    @Override
    public void Init() {
        linkers = new Stat[Linkers.length];
        for (int i = 0; i < linkers.length; i++) {
            linkers[i] = new Stat();
            linkers[i].Linker = Linkers[i];
        }
//        LinkerRawDataNum = new long[Linkers.length];
//        LinkerSelfLigationNum = new long[Linkers.length];
//        LinkerReLigationNum = new long[Linkers.length];
//        LinkerRepeatNum = new long[Linkers.length];
//        LinkerSameCleanNum = new long[Linkers.length];
//        LinkerDiffCleanNum = new long[Linkers.length];
//        LinkerCleanNum = new long[Linkers.length];
//        LinkerShortRangeNum = new long[Linkers.length];
//        LinkerLongRangeNum = new long[Linkers.length];
//        //-------------------------------------------------------------------
//        InputFile = new BedpeFile[Linkers.length];
//        SelfLigationFile = new BedpeFile[Linkers.length];
//        ReLigationFile = new BedpeFile[Linkers.length];
//        DuplicateFile = new BedpeFile[Linkers.length];
//        SameCleanFile = new BedpeFile[Linkers.length];
//        DiffCleanFile = new BedpeFile[Linkers.length];
//        CleanFile = new BedpeFile[Linkers.length];
    }
}
