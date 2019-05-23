package Component.Statistic.LinkerFilter;

import Component.unit.LinkerSequence;

import java.io.File;
import java.util.HashMap;

/**
 * Created by snowf on 2019/3/1.
 */

public class Stat {
    public LinkerSequence Linker;
    public long LeftValidPairNum, RightValidPairNum;
    public long ValidPairNum;
    public long AddBaseToLeftPair, AddBaseToRightPair;
    public long MatchableNum;
    public HashMap<Integer, int[]> ReadsLengthDistributionR1 = new HashMap<>();
    public HashMap<Integer, int[]> ReadsLengthDistributionR2 = new HashMap<>();
    public final String[] lock = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
    public File ReadLengthDisPng;

    public void clear() {
        Linker = null;
        LeftValidPairNum = RightValidPairNum = ValidPairNum = AddBaseToLeftPair = AddBaseToRightPair = MatchableNum = 0;
        ReadsLengthDistributionR1.clear();
        ReadsLengthDistributionR2.clear();
    }
}
