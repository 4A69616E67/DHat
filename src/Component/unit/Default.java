package Component.unit;


import java.text.DateFormat;
import java.util.Date;

public class Default {
    public static final String OutPath = "./";
    public static final String Prefix = "out." + DateFormat.getTimeInstance().format(new Date());
    public static final int Resolution = 1000000;
    public static final int DetectResolution = 100000;
    public static final int Thread = 1;
    public static final String Step = "-";
    //=========================advanced========================
    public static final String AlignType = "Short";
    public static final String Iteration = "false";
    public static final int MaxReadsLen = 20;
    public static final int MinReadsLen = 16;
    public static final int MatchScore = 1;
    public static final int MisMatchScore = -1;
    public static final int InDelScore = -1;
    public static final int MaxThreads = 4;
    public static final int AlignThread = 1;
    public static final int AlignMisMatchNum = 0;
}
