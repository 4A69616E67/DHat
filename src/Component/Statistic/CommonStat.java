package Component.Statistic;

import java.text.DecimalFormat;

/**
 * Created by snowf on 2019/3/15.
 */

public class CommonStat extends AbstractStat {
    public long RawDataNum;
    public long AlignmentNum;
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
        String show = "##==============================================================================================\n" +
                "Raw data:      \t" + RawDataNum +
                "Alignment data:\t" + new DecimalFormat("#,###").format(AlignmentNum) + "\t" + String.format("%.2f", (double) AlignmentNum / RawDataNum * 100) + "%" + "\n" +
                "Valid data:    \t" + new DecimalFormat("#,###").format(CleanNum) + "\t" + String.format("%.2f", (double) CleanNum / RawDataNum * 100) + "%" + "\n" +
                "Inter-action:  \t" + new DecimalFormat("#,###").format(InterActionNum) + "\t" + String.format("%.2f", (double) InterActionNum / CleanNum * 100) + "%" + "\t" +
                "Intra-action:  \t" + new DecimalFormat("#,###").format(IntraActionNum) + "\t" + String.format("%.2f", (double) IntraActionNum / CleanNum * 100) + "%" + "\n" +
                "Short range(<" + RangeThreshold + "):\t" + new DecimalFormat("#,###").format(ShortRange) + "\t" + String.format("%.2f", (double) ShortRange / IntraActionNum * 100) + "%" + "\t" +
                "Long range(>=" + RangeThreshold + "):\t" + new DecimalFormat("#,###").format(LongRange) + "\t" + String.format("%.2f", (double) LongRange / IntraActionNum * 100) + "%" + "\n";
        return show;
    }

    @Override
    protected void UpDate() {

    }

    @Override
    public void Init() {

    }
}
