package Component.Statistic;

import Component.File.CommonFile;
import Component.unit.LinkerSequence;

import java.text.DecimalFormat;

/**
 * Created by snowf on 2019/3/1.
 */

public class LinkerFilterStat extends AbstractStat {
    public LinkerSequence[] Linkers = new LinkerSequence[0];
    public int Threshold;
    public String EnzymeCuttingSite = "";

    //--------------------------------------------------------------------
    public CommonFile InputFile;
    public long[] LeftValidPairNum = new long[0];
    public long[] RightValidPairNum = new long[0];
    public long[] ValidPairNum = new long[0];
    public int[] LinkerMatchScoreDistribution = new int[0];
    public long[] AddBaseToLeftPair = new long[0];
    public long[] AddBaseToRightPair = new long[0];
    public long[] LinkerMatchableNum = new long[0];

    public long AllLinkerMatchable;
    public long LinkerUnmatchableNum;
    public long AdapterMatchableNum;
    public long AdapterUnmatchableNum;
    public long AllValidLeftPair;
    public long AllLeftAddBase;
    public long AllValidRightPair;
    public long AllRightAddBase;
    public long AllValidPair;


    public int ThreadNum;

    public LinkerFilterStat() {
    }

    @Override
    public void Stat() {

    }

    @Override
    public String Show() {
        //show.append( ).append("\t").append(new DecimalFormat("#,###").format( )).append("\t").append(String.format("%.2f", )).append("%").append("\n");
        UpDate();
        StringBuilder show = new StringBuilder();
        show.append("##================================Linker filter statistic=====================================\n");
        show.append("Enzyme cutting site:\t").append(EnzymeCuttingSite).append("\n");
        show.append("Linker mapping minimum quality:\t").append(Threshold).append("\n");
        show.append("##--------------------------------------------------------------------------------------------\n");
        show.append("Item\tNumber\tPercentage\n");
        show.append(InputFile.getName()).append("\t").append(new DecimalFormat("#,###").format(InputFile.getItemNum())).append("\t").append("-").append("\n");
        for (int i = 0; i < Linkers.length; i++) {
            try {
                show.append("Linker ").append(Linkers[i].getType()).append(":").append("\n");
                show.append("Linker matchable").append("\t").append(new DecimalFormat("#,###").format(LinkerMatchableNum[i])).append("\t").append(String.format("%.2f", (double) LinkerMatchableNum[i] / InputFile.getItemNum() * 100)).append("%").append("\n");
                show.append("Left pair valid").append("\t").append(new DecimalFormat("#,###").format(LeftValidPairNum[i])).append("\t").append(String.format("%.2f", (double) LeftValidPairNum[i] / InputFile.getItemNum() * 100)).append("%").append("\n");
                show.append("Add base to left pair").append("\t").append(new DecimalFormat("#,###").format(AddBaseToLeftPair[i])).append("\t").append(String.format("%.2f", (double) AddBaseToLeftPair[i] / InputFile.getItemNum() * 100)).append("%").append("\n");
                show.append("Right pair valid").append("\t").append(new DecimalFormat("#,###").format(RightValidPairNum[i])).append("\t").append(String.format("%.2f", (double) RightValidPairNum[i] / InputFile.getItemNum() * 100)).append("%").append("\n");
                show.append("Add base to right pair").append("\t").append(new DecimalFormat("#,###").format(AddBaseToRightPair[i])).append("\t").append(String.format("%.2f", (double) AddBaseToRightPair[i] / InputFile.getItemNum() * 100)).append("%").append("\n");
                show.append("Valid reads pair").append("\t").append(new DecimalFormat("#,###").format(ValidPairNum[i])).append("\t").append(String.format("%.2f", (double) ValidPairNum[i] / InputFile.getItemNum() * 100)).append("%").append("\n");
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        show.append("Total:\n");
        show.append("Linker matchable").append("\t").append(new DecimalFormat("#,###").format(AllLinkerMatchable)).append("\t").append(String.format("%.2f", (double) AllLinkerMatchable / InputFile.getItemNum() * 100)).append("%").append("\n");
        show.append("Linker unmatchable").append("\t").append(new DecimalFormat("#,###").format(LinkerUnmatchableNum)).append("\t").append(String.format("%.2f", (double) LinkerUnmatchableNum / InputFile.getItemNum() * 100)).append("%").append("\n");
        show.append("Adapter matchable").append("\t").append(new DecimalFormat("#,###").format(AdapterMatchableNum)).append("\t").append(String.format("%.2f", (double) AdapterMatchableNum / InputFile.getItemNum() * 100)).append("%").append("\n");
        show.append("Adapter unmatchable").append("\t").append(new DecimalFormat("#,###").format(AdapterUnmatchableNum)).append("\t").append(String.format("%.2f", (double) AdapterUnmatchableNum / InputFile.getItemNum() * 100)).append("%").append("\n");
        show.append("Left pair valid").append("\t").append(new DecimalFormat("#,###").format(AllValidLeftPair)).append("\t").append(String.format("%.2f", (double) AllValidLeftPair / InputFile.getItemNum() * 100)).append("%").append("\n");
        show.append("Add base to left pair").append("\t").append(new DecimalFormat("#,###").format(AllLeftAddBase)).append("\t").append(String.format("%.2f", (double) AllLeftAddBase / InputFile.getItemNum() * 100)).append("%").append("\n");
        show.append("Right pair valid").append("\t").append(new DecimalFormat("#,###").format(AllValidRightPair)).append("\t").append(String.format("%.2f", (double) AllValidRightPair / InputFile.getItemNum() * 100)).append("%").append("\n");
        show.append("Add base to right pair").append("\t").append(new DecimalFormat("#,###").format(AllRightAddBase)).append("\t").append(String.format("%.2f", (double) AllRightAddBase / InputFile.getItemNum() * 100)).append("%").append("\n");
        show.append("Valid reads pair").append("\t").append(new DecimalFormat("#,###").format(AllValidPair)).append("\t").append(String.format("%.2f", (double) AllValidPair / InputFile.getItemNum() * 100)).append("%").append("\n");
        return show.toString();
    }

    @Override
    protected void UpDate() {
        AllLinkerMatchable = sum(LinkerMatchableNum);
        if (LinkerUnmatchableNum == 0 && AllLinkerMatchable != 0) {
            LinkerUnmatchableNum = InputFile.getItemNum() - AllLinkerMatchable;
        }
        if (AdapterUnmatchableNum == 0 && AdapterMatchableNum != 0) {
            AdapterUnmatchableNum = InputFile.getItemNum() - AdapterMatchableNum;
        }
        AllValidPair = sum(ValidPairNum);
        AllValidLeftPair = sum(LeftValidPairNum);
        AllValidRightPair = sum(RightValidPairNum);
        AllLeftAddBase = sum(AddBaseToLeftPair);
        AllRightAddBase = sum(AddBaseToRightPair);
    }

    @Override
    public void Init() {
        LinkerMatchableNum = new long[Linkers.length];
        ValidPairNum = new long[Linkers.length];
        LeftValidPairNum = new long[Linkers.length];
        RightValidPairNum = new long[Linkers.length];
        AddBaseToLeftPair = new long[Linkers.length];
        AddBaseToRightPair = new long[Linkers.length];
    }

    private long sum(long[] l) {
        long res = 0;
        if (l != null) {
            for (long aL : l) {
                res += aL;
            }
        }
        return res;
    }

}
