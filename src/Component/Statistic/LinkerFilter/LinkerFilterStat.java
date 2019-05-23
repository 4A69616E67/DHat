package Component.Statistic.LinkerFilter;

import Component.File.AbstractFile;
import Component.File.CommonFile;
import Component.File.FastQFile.FastqItem;
import Component.Statistic.AbstractStat;
import Component.Statistic.StatUtil;
import Component.tool.DivideLinker;
import Component.tool.Tools;
import Component.unit.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;

public class LinkerFilterStat extends AbstractStat {
    public LinkerSequence[] Linkers = new LinkerSequence[0];
    public Stat[] linkers;
    public Stat total = new Stat();
    public String[] HalfLinkers = new String[]{""};
    public String[] Adapters = new String[]{""};
    public int Threshold;
    public String EnzymeCuttingSite = "";
    public int MaxReadsLen;
    public File OutDir;
    private String[] lock = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    //--------------------------------------------------------------------
    public CommonFile InputFile;
    public long InputNum;
    public HashMap<String, int[]> LinkerMatchScoreDistribution = new HashMap<>();

    public long LinkerUnmatchableNum;
    public long AdapterMatchableNum;
    public long AdapterUnmatchableNum;

    public File AdapterBaseDisPng;
    public File LinkerScoreDisPng;


    public int ThreadNum;

    public void Stat(int thread) throws IOException {
        if (thread <= 0) {
            thread = 1;
        }
        Init();
        InputFile.ReadOpen();
        Thread[] t = new Thread[thread];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                try {
                    StatBody();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
    }

    @Override
    public void Stat() throws IOException {
        Init();
        InputFile.ReadOpen();
        StatBody();
    }

    private void StatBody() throws IOException {
        String[] Lines, Str;
        String[] result = DivideLinker.RestrictionParse(EnzymeCuttingSite);
        String[] MatchSeq = new String[]{result[0], result[2]};
        String[] AppendSeq = new String[]{result[1], result[3]};
        String[] AppendQuality = new String[]{"I", "I"};
        while ((Lines = InputFile.ReadItemLine()) != null) {
            Str = Lines[0].split("\\t");
            synchronized (lock[0]) {
                if (!LinkerMatchScoreDistribution.containsKey(Str[6])) {
                    LinkerMatchScoreDistribution.put(Str[6], new int[]{0});
                }
                LinkerMatchScoreDistribution.get(Str[6])[0]++;
            }
            if (!Str[4].equals("*")) {
                synchronized (lock[1]) {
                    AdapterMatchableNum++;
                }
            }
            if (Integer.parseInt(Str[6]) >= Threshold) {
                for (int j = 0; j < linkers.length; j++) {
                    if (Str[5].equals(linkers[j].Linker.getType())) {
                        synchronized (linkers[j].lock[4]) {
                            linkers[j].MatchableNum++;
                        }
                        FastqItem[] fastq_string = DivideLinker.Execute(Str, MatchSeq, AppendSeq, AppendQuality, MaxReadsLen, DivideLinker.Format.All, j);
                        if (fastq_string[0] != null && fastq_string[1] != null) {
                            synchronized (linkers[j].lock[5]) {
                                linkers[j].ValidPairNum++;
                            }
                        }
                        break;
                    }
                }
            }
            synchronized (lock[2]) {
                InputNum++;
                if (InputNum % 1000000 == 0) {
                    System.out.println(new Date() + " [Linker filter statistic]:\t" + InputNum / 1000000 + " Million has been statistic");
                }
            }

        }
    }

    @Override
    public String Show() {
        //show.append( ).append("\t").append(new DecimalFormat("#,###").format( )).append("\t").append(String.format("%.2f", )).append("%").append("\n");
        UpDate();
        StringBuilder show = new StringBuilder();
        show.append("===================================Linker filter statistic======================================\n");
        show.append("Enzyme cutting site:\t").append(EnzymeCuttingSite).append("\n");
        show.append("Half-Linkers:\t").append(String.join(" ", HalfLinkers)).append("\n");
        show.append("Adapters:\t").append(String.join(" ", Adapters)).append("\n");
        show.append("Match score: ").append(Configure.MatchScore).append("\tMismatch score: ").append(Configure.MisMatchScore).append("\tInsert & Delete score: ").append(Configure.InDelScore).append("\n");
        show.append("Linker mapping minimum quality:\t").append(Threshold).append("\n");
        show.append("Output directory:\t").append(OutDir).append("\n");
        show.append("------------------------------------------------------------------------------------------------\n");
        show.append("Input:").append("\t").append(new DecimalFormat("#,###").format(InputNum)).append("\t").append("-").append("\n");
        show.append("\n");
        for (int i = 0; i < linkers.length; i++) {
            show.append("Linker ").append(linkers[i].Linker.getType()).append(":\t").append(linkers[i].Linker.getSeq()).append("\n");
            show.append("Linker matchable").append("\t").append(new DecimalFormat("#,###").format(linkers[i].MatchableNum)).append("\t").append(String.format("%.2f", (double) linkers[i].MatchableNum / InputNum * 100)).append("%").append("\n");
            show.append("Left pair valid ").append("\t").append(new DecimalFormat("#,###").format(linkers[i].LeftValidPairNum)).append("\t").append(String.format("%.2f", (double) linkers[i].LeftValidPairNum / InputNum * 100)).append("%").append("\t");
            show.append("Add base to left pair").append("\t").append(new DecimalFormat("#,###").format(linkers[i].AddBaseToLeftPair)).append("\t").append(String.format("%.2f", (double) linkers[i].AddBaseToLeftPair / InputNum * 100)).append("%").append("\n");
            show.append("Right pair valid").append("\t").append(new DecimalFormat("#,###").format(linkers[i].RightValidPairNum)).append("\t").append(String.format("%.2f", (double) linkers[i].RightValidPairNum / InputNum * 100)).append("%").append("\t");
            show.append("Add base to right pair").append("\t").append(new DecimalFormat("#,###").format(linkers[i].AddBaseToRightPair)).append("\t").append(String.format("%.2f", (double) linkers[i].AddBaseToRightPair / InputNum * 100)).append("%").append("\n");
            show.append("Valid reads pair").append("\t").append(new DecimalFormat("#,###").format(linkers[i].ValidPairNum)).append("\t").append(String.format("%.2f", (double) linkers[i].ValidPairNum / InputNum * 100)).append("%").append("\n");
            show.append("\n");
        }
        show.append("Total:\n");
        show.append("Linker matchable").append("\t").append(new DecimalFormat("#,###").format(total.MatchableNum)).append("\t").append(String.format("%.2f", (double) total.MatchableNum / InputNum * 100)).append("%").append("\t");
        show.append("Linker unmatchable").append("\t").append(new DecimalFormat("#,###").format(LinkerUnmatchableNum)).append("\t").append(String.format("%.2f", (double) LinkerUnmatchableNum / InputNum * 100)).append("%").append("\n");
        show.append("Adapter matchable").append("\t").append(new DecimalFormat("#,###").format(AdapterMatchableNum)).append("\t").append(String.format("%.2f", (double) AdapterMatchableNum / InputNum * 100)).append("%").append("\t");
        show.append("Adapter unmatchable").append("\t").append(new DecimalFormat("#,###").format(AdapterUnmatchableNum)).append("\t").append(String.format("%.2f", (double) AdapterUnmatchableNum / InputNum * 100)).append("%").append("\n");
        show.append("Left pair valid ").append("\t").append(new DecimalFormat("#,###").format(total.LeftValidPairNum)).append("\t").append(String.format("%.2f", (double) total.LeftValidPairNum / InputNum * 100)).append("%").append("\t");
        show.append("Add base to left pair").append("\t").append(new DecimalFormat("#,###").format(total.AddBaseToLeftPair)).append("\t").append(String.format("%.2f", (double) total.AddBaseToLeftPair / InputNum * 100)).append("%").append("\n");
        show.append("Right pair valid").append("\t").append(new DecimalFormat("#,###").format(total.RightValidPairNum)).append("\t").append(String.format("%.2f", (double) total.RightValidPairNum / InputNum * 100)).append("%").append("\t");
        show.append("Add base to right pair").append("\t").append(new DecimalFormat("#,###").format(total.AddBaseToRightPair)).append("\t").append(String.format("%.2f", (double) total.AddBaseToRightPair / InputNum * 100)).append("%").append("\n");
        show.append("Valid reads pair").append("\t").append(new DecimalFormat("#,###").format(total.ValidPairNum)).append("\t").append(String.format("%.2f", (double) total.ValidPairNum / InputNum * 100)).append("%").append("\n");
        return show.toString();
    }

    @Override
    protected void UpDate() {
        total.clear();
        for (Stat linker : linkers) {
            total.MatchableNum += linker.MatchableNum;
            total.LeftValidPairNum += linker.LeftValidPairNum;
            total.RightValidPairNum += linker.RightValidPairNum;
            total.AddBaseToLeftPair += linker.AddBaseToLeftPair;
            total.AddBaseToRightPair += linker.AddBaseToRightPair;
            total.ValidPairNum += linker.ValidPairNum;
        }
        if (LinkerUnmatchableNum == 0 && total.MatchableNum != 0) {
            LinkerUnmatchableNum = InputFile.getItemNum() - total.MatchableNum;
        }
        if (AdapterUnmatchableNum == 0 && AdapterMatchableNum != 0) {
            AdapterUnmatchableNum = InputFile.getItemNum() - AdapterMatchableNum;
        }
    }

    @Override
    public void Init() {
        linkers = new Stat[Linkers.length];
        for (int i = 0; i < Linkers.length; i++) {
            linkers[i] = new Stat();
            linkers[i].Linker = Linkers[i];
        }
    }

    public void WriteLinkerScoreDis(AbstractFile f) throws IOException {
        BufferedWriter outfile = f.WriteOpen();
        outfile.write("Score\tCount\n");
        int[] Keys = StringArrays.toInteger(LinkerMatchScoreDistribution.keySet().toArray(new String[0]));
        int max = StatUtil.maxValue(Keys);
        int min = StatUtil.min(Keys);
        for (int i = min; i <= max; i++) {
            outfile.write(i + "\t");
            if (!LinkerMatchScoreDistribution.containsKey(String.valueOf(i))) {
                outfile.write(0 + "\n");
            } else {
                outfile.write(LinkerMatchScoreDistribution.get(String.valueOf(i))[0] + "\n");
            }
        }
        f.ItemNum = max - min + 1;
        outfile.close();
    }

    public void WriteReadsLengthDis(AbstractFile[] f) throws IOException {
        for (int i = 0; i < f.length; i++) {
            BufferedWriter outfile = f[i].WriteOpen();
            outfile.write("Length\tR1\tR2\n");
            int[] R1Keys = new int[linkers[i].ReadsLengthDistributionR1.size()];
            int[] R2Keys = new int[linkers[i].ReadsLengthDistributionR2.size()];
            try {
                int index = 0;
                for (Integer k : linkers[i].ReadsLengthDistributionR1.keySet()) {
                    R1Keys[index] = k;
                    index++;
                }
                index = 0;
                for (Integer k : linkers[i].ReadsLengthDistributionR2.keySet()) {
                    R2Keys[index] = k;
                    index++;
                }
                int max = Math.max(StatUtil.maxValue(R1Keys), StatUtil.maxValue(R2Keys));
                int min = Math.min(StatUtil.min(R1Keys), StatUtil.min(R2Keys));
                for (int j = min; j <= max; j++) {
                    outfile.write(j + "\t");
                    if (!linkers[i].ReadsLengthDistributionR1.containsKey(j)) {
                        outfile.write(0 + "\t");
                    } else {
                        outfile.write(linkers[i].ReadsLengthDistributionR1.get(j)[0] + "\t");
                    }
                    if (!linkers[i].ReadsLengthDistributionR2.containsKey(j)) {
                        outfile.write(0 + "\n");
                    } else {
                        outfile.write(linkers[i].ReadsLengthDistributionR2.get(j)[0] + "\n");
                    }
                }
                outfile.close();
            } catch (IndexOutOfBoundsException e) {
                outfile.close();
            }
        }
    }

}
