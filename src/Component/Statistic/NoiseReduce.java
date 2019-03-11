package Component.Statistic;

import Component.unit.LinkerSequence;

import java.util.HashMap;

/**
 * Created by snowf on 2019/3/10.
 */

public class NoiseReduce extends AbstractStat {
    public LinkerSequence Linkers;
    public long[] LinkerRawDataNum, LinkerSelfLigationNum, LinkerReLigationNum, LinkerRepeatNum, LinkerCleanNum;
    public long RawDataNum, SelfLigationNum, ReLigationNum, RepeatNum, CleanNum;
    public long IntraActionNum, InterActionNum;
    public long LongRangeNum, ShortRangeNum;
    public HashMap<Integer, Integer> InteractionRangeDistribution = new HashMap<>();

    @Override

    public void Stat() {

    }

    @Override
    public String Show() {
        UpDate();
        StringBuilder show = new StringBuilder();
        show.append("##=================================Noise reduce Statistic=======================================\n");
        show.append("Alignment software:\t").append(" ").append("\n");
        show.append("Minimum unique mapped quality:\t").append(" ").append("\n");
        show.append("------------------------------------------------------------------------------------------------\n");
        return null;
    }

    @Override
    protected void UpDate() {

    }

    @Override
    public void Init() {

    }
}
