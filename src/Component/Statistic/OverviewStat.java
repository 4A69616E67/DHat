package Component.Statistic;

import Component.File.FastqFile;
import Component.unit.Opts;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by snowf on 2019/3/15.
 */

public class OverviewStat extends AbstractStat {
    public FastqFile InputFile;
    public String Prefix;
    public File OutDir;
    public long RawDataNum;
    public long AlignmentNum;
    public long UniqueMappedNum;
    public long CleanNum;
    public long IntraActionNum;
    public long InterActionNum;
    public long ShortRange;
    public long LongRange;
    public int RangeThreshold;

    @Override
    public void Stat() {

    }

    @Override
    public String Show() {
        //show.append( ).append("\t").append(new DecimalFormat("#,###").format( )).append("\t").append(String.format("%.2f", *100)).append("%").append("\n");
        UpDate();
        StringBuilder show = new StringBuilder();
        show.append("##====================================Overview=================================================##\n");
        show.append("Input file:   \t").append(InputFile).append("\n");
        show.append("Out directory:\t").append(OutDir).append("\n");
        show.append("Out prefix:   \t").append(Prefix).append("\n");
        show.append("-------------------------------------------------------------------------------------------------\n");
        show.append("Raw data:      \t").append(new DecimalFormat("#,###").format(RawDataNum)).append("\n");
        show.append("Alignment data:\t").append(new DecimalFormat("#,###").format(AlignmentNum)).append("\t").append(String.format("%.2f", (double) AlignmentNum / RawDataNum * 100)).append("%").append("\n");
        show.append("Unique mapped: \t").append(new DecimalFormat("#,###").format(UniqueMappedNum)).append("\t").append(String.format("%.2f", (double) UniqueMappedNum / RawDataNum * 100)).append("%").append("\n");
        show.append("Clean data:    \t").append(new DecimalFormat("#,###").format(CleanNum)).append("\t").append(String.format("%.2f", (double) CleanNum / RawDataNum * 100)).append("%").append("\n");
        show.append("Inter-action:  \t").append(new DecimalFormat("#,###").format(InterActionNum)).append("\t").append(String.format("%.2f", (double) InterActionNum / CleanNum * 100)).append("%").append("\t");
        show.append("Intra-action:  \t").append(new DecimalFormat("#,###").format(IntraActionNum)).append("\t").append(String.format("%.2f", (double) IntraActionNum / CleanNum * 100)).append("%").append("\n");
        show.append("Short range(<=").append(RangeThreshold).append("):\t").append(new DecimalFormat("#,###").format(ShortRange)).append("\t").append(String.format("%.2f", (double) ShortRange / IntraActionNum * 100)).append("%").append("\t");
        show.append("Long range(>").append(RangeThreshold).append("):\t").append(new DecimalFormat("#,###").format(LongRange)).append("\t").append(String.format("%.2f", (double) LongRange / IntraActionNum * 100)).append("%").append("\n");
        return show.toString();
    }

    @Override
    protected void UpDate() {
        RawDataNum = Opts.LFStat.InputNum;
        AlignmentNum = StatUtil.sum(Opts.ALStat.LinkerInputNum);
        UniqueMappedNum = StatUtil.sum(Opts.ALStat.MergeNum);
        CleanNum = StatUtil.sum(Opts.NRStat.LinkerCleanNum);
        RangeThreshold = Opts.NRStat.ShortRegion.End;
        IntraActionNum = Opts.NRStat.SameCleanNum;
        InterActionNum = Opts.NRStat.DiffCleanNum;
        ShortRange = Opts.NRStat.ShortRangeNum;
        LongRange = Opts.NRStat.LongRangeNum;
    }

    @Override
    public void Init() {

    }
}
