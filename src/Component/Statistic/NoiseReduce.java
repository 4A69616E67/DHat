package Component.Statistic;

import Component.File.BedpeFile;
import Component.tool.Tools;
import Component.unit.LinkerSequence;
import Component.unit.Opts;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by snowf on 2019/3/10.
 */

public class NoiseReduce extends AbstractStat {
    public LinkerSequence[] Linkers;
    public long[] LinkerRawDataNum, LinkerSelfLigationNum, LinkerReLigationNum, LinkerRepeatNum, LinkerCleanNum;
    public long RawDataNum, SelfLigationNum, ReLigationNum, RepeatNum, CleanNum;
    public HashMap<Integer, Integer> InteractionRangeDistribution = new HashMap<>();
    public File OutDir;

    //-----------------------------------------------------------------------------
    public BedpeFile[] InputFile, SelfLigationFile, ReLigationFile, RepeatFile, CleanFile;

    @Override

    public void Stat() {

    }

    public void Stat(int thread) {
        if (thread <= 0) {
            thread = 1;
        }
        LinkedList<BedpeFile> list = new LinkedList<>();
        for (int i = 0; i < Linkers.length; i++) {
            list.add(InputFile[i]);
            list.add(SelfLigationFile[i]);
            list.add(ReLigationFile[i]);
            list.add(RepeatFile[i]);
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
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        for (int i = 0; i < Linkers.length; i++) {
            LinkerRawDataNum[i] = list.removeFirst().getItemNum();
            LinkerSelfLigationNum[i] = list.removeFirst().getItemNum();
            LinkerReLigationNum[i] = list.removeFirst().getItemNum();
            LinkerRepeatNum[i] = list.removeFirst().getItemNum();
            LinkerCleanNum[i] = list.removeFirst().getItemNum();
        }
    }

    @Override
    public String Show() {
        //show.append( ).append("\t").append(new DecimalFormat("#,###").format( )).append("\t").append(String.format("%.2f", )).append("%").append("\n");
        UpDate();
        StringBuilder show = new StringBuilder();
        show.append("##=================================Noise reduce Statistic=======================================\n");
        show.append("Output directory:\t").append(OutDir).append("\n");
        show.append("------------------------------------------------------------------------------------------------\n");
        for (int i = 0; i < Linkers.length; i++) {
            show.append("Linker ").append(Linkers[i].getType()).append(":\t").append(Linkers[i].getSeq()).append("\n");
            show.append("Raw data:     \t").append(new DecimalFormat("#,###").format(LinkerRawDataNum[i])).append("\n");
            show.append("Self-ligation:\t").append(new DecimalFormat("#,###").format(LinkerSelfLigationNum[i])).append("\t").append(String.format("%.2f", (double) LinkerSelfLigationNum[i] / LinkerRawDataNum[i] * 100)).append("%").append("\t");
            show.append("Re-ligation: ").append(new DecimalFormat("#,###").format(LinkerReLigationNum[i])).append(" ").append(String.format("%.2f", (double) LinkerReLigationNum[i] / LinkerRawDataNum[i] * 100)).append("%").append("\t");
            show.append("Repeat: ").append(new DecimalFormat("#,###").format(LinkerRepeatNum[i])).append(" ").append(String.format("%.2f", (double) LinkerRepeatNum[i] / LinkerRawDataNum[i] * 100)).append("%").append("\n");
            show.append("Clean data:   \t").append(new DecimalFormat("#,###").format(LinkerCleanNum[i])).append("\t").append(String.format("%.2f", (double) LinkerCleanNum[i] / LinkerRawDataNum[i] * 100)).append("%").append("\n");
            show.append("\n");
        }
        show.append("Total:\n");
        show.append("Raw data:     \t").append(new DecimalFormat("#,###").format(RawDataNum)).append("\n");
        show.append("Self-ligation:\t").append(new DecimalFormat("#,###").format(SelfLigationNum)).append("\t").append(String.format("%.2f", (double) SelfLigationNum / RawDataNum * 100)).append("%").append("\t");
        show.append("Re-ligation: ").append(new DecimalFormat("#,###").format(ReLigationNum)).append(" ").append(String.format("%.2f", (double) ReLigationNum / RawDataNum * 100)).append("%").append("\t");
        show.append("Repeat: ").append(new DecimalFormat("#,###").format(RepeatNum)).append(" ").append(String.format("%.2f", (double) RepeatNum / RawDataNum * 100)).append("%").append("\n");
        show.append("Clean data:   \t").append(new DecimalFormat("#,###").format(CleanNum)).append("\t").append(String.format("%.2f", (double) CleanNum / RawDataNum * 100)).append("%").append("\n");
        show.append("\n");
        return show.toString();
    }

    @Override
    protected void UpDate() {
        RawDataNum = StatUtil.sum(LinkerRawDataNum);
        SelfLigationNum = StatUtil.sum(LinkerSelfLigationNum);
        ReLigationNum = StatUtil.sum(LinkerReLigationNum);
        RepeatNum = StatUtil.sum(LinkerRepeatNum);
        CleanNum = StatUtil.sum(LinkerCleanNum);
    }

    @Override
    public void Init() {
        LinkerRawDataNum = new long[Linkers.length];
        LinkerSelfLigationNum = new long[Linkers.length];
        LinkerReLigationNum = new long[Linkers.length];
        LinkerRepeatNum = new long[Linkers.length];
        LinkerCleanNum = new long[Linkers.length];
        //-------------------------------------------------------------------
        InputFile = new BedpeFile[Linkers.length];
        SelfLigationFile = new BedpeFile[Linkers.length];
        ReLigationFile = new BedpeFile[Linkers.length];
        RepeatFile = new BedpeFile[Linkers.length];
        CleanFile = new BedpeFile[Linkers.length];
    }
}
