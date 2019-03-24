package Component.Statistic;

import Component.unit.LinkerSequence;
import Component.unit.Opts;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Created by snowf on 2019/3/10.
 */

public class NoiseReduce extends AbstractStat {
    public LinkerSequence[] Linkers;
    public long[] LinkerRawDataNum, LinkerSelfLigationNum, LinkerReLigationNum, LinkerRepeatNum, LinkerCleanNum;
    public long RawDataNum, SelfLigationNum, ReLigationNum, RepeatNum, CleanNum;
    public HashMap<Integer, Integer> InteractionRangeDistribution = new HashMap<>();

    @Override

    public void Stat() {

    }

    @Override
    public String Show() {
        //show.append( ).append("\t").append(new DecimalFormat("#,###").format( )).append("\t").append(String.format("%.2f", )).append("%").append("\n");
        UpDate();
        StringBuilder show = new StringBuilder();
        show.append("##=================================Noise reduce Statistic=======================================\n");
        show.append(" \t").append(" ").append("\n");
        show.append("------------------------------------------------------------------------------------------------\n");
        for (int i = 0; i < Linkers.length; i++) {
            show.append("Linker ").append(Linkers[i].getType()).append(":\t").append(Linkers[i].getSeq()).append("\n");
            show.append("Raw data").append("\t").append(new DecimalFormat("#,###").format(LinkerRawDataNum[i])).append("\n");
            show.append("Self-ligation").append("\t").append(new DecimalFormat("#,###").format(LinkerSelfLigationNum[i])).append("\t").append(String.format("%.2f", (double) LinkerSelfLigationNum[i] / LinkerRawDataNum[i] * 100)).append("%").append("\t");
            show.append("Re-ligation").append("\t").append(new DecimalFormat("#,###").format(LinkerReLigationNum[i])).append("\t").append(String.format("%.2f", (double) LinkerReLigationNum[i] / LinkerRawDataNum[i] * 100)).append("%").append("\t");
            show.append("Repeat").append("\t").append(new DecimalFormat("#,###").format(LinkerRepeatNum[i])).append("\t").append(String.format("%.2f", (double) LinkerRepeatNum[i] / LinkerRawDataNum[i] * 100)).append("%").append("\n");
            show.append("Clean data").append("\t").append(new DecimalFormat("#,###").format(LinkerCleanNum[i])).append("\t").append(String.format("%.2f", (double) LinkerCleanNum[i] / LinkerRawDataNum[i] * 100)).append("%").append("\n");
            show.append("\n");
        }
        show.append("Total:\n");
        show.append("Raw data").append("\t").append(new DecimalFormat("#,###").format(RawDataNum)).append("\n");
        show.append("Self-ligation").append("\t").append(new DecimalFormat("#,###").format(SelfLigationNum)).append("\t").append(String.format("%.2f", (double) SelfLigationNum / RawDataNum * 100)).append("%").append("\t");
        show.append("Re-ligation").append("\t").append(new DecimalFormat("#,###").format(ReLigationNum)).append("\t").append(String.format("%.2f", (double) ReLigationNum / RawDataNum * 100)).append("%").append("\t");
        show.append("Repeat").append("\t").append(new DecimalFormat("#,###").format(RepeatNum)).append("\t").append(String.format("%.2f", (double) RepeatNum / RawDataNum * 100)).append("%").append("\n");
        show.append("Clean data").append("\t").append(new DecimalFormat("#,###").format(CleanNum)).append("\t").append(String.format("%.2f", (double) CleanNum / RawDataNum * 100)).append("%").append("\n");
        show.append("\n");
        return show.toString();
    }

    @Override
    protected void UpDate() {
        Opts.OVStat.UniqueMappedNum = RawDataNum = StatUtil.sum(LinkerRawDataNum);
        SelfLigationNum = StatUtil.sum(LinkerSelfLigationNum);
        ReLigationNum = StatUtil.sum(LinkerReLigationNum);
        RepeatNum = StatUtil.sum(LinkerRepeatNum);
        Opts.OVStat.CleanNum = CleanNum = StatUtil.sum(LinkerCleanNum);
    }

    @Override
    public void Init() {
        LinkerRawDataNum = new long[Linkers.length];
        LinkerSelfLigationNum = new long[Linkers.length];
        LinkerReLigationNum = new long[Linkers.length];
        LinkerRepeatNum = new long[Linkers.length];
        LinkerCleanNum = new long[Linkers.length];
    }
}
