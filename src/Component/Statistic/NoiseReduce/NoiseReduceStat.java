package Component.Statistic.NoiseReduce;

import Component.File.AbstractFile;
import Component.File.BedPeFile.BedpeFile;
import Component.Statistic.AbstractStat;
import Component.tool.Tools;
import Component.File.BedPeFile.BedpeItem;
import Component.unit.LinkerSequence;
import Component.unit.Region;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;


public class NoiseReduceStat extends AbstractStat {
    public LinkerSequence[] Linkers;
    public Stat[] linkers;
    public Stat total = new Stat();
    public HashMap<Integer, long[]> InteractionRangeDistribution = new HashMap<>();
    public Region ShortRegion, LongRegion;
    public File OutDir;
    public HashMap<String, HashMap<String, long[]>> OrientationPositionStat = new HashMap<>();
    public HashMap<String, HashMap<String, long[]>> SameOriPosStat = new HashMap<>();
    public HashMap<String, HashMap<String, long[]>> DiffOriPosStat = new HashMap<>();
    public static final String[] OriList = new String[]{"+,+", "+,-", "-,+", "-,-"};
    public static final String[] PosList = new String[]{"s,s", "s,t", "t,s", "t,t"};

    public File InteractionRangeDistributionPng;
    public File _50M_InteractionRangeDistributionPng;
    public File _10M_InteractionRangeDistributionPng;
    public File _2M_InteractionRangeDistributionPng;

    public static HashMap<String, HashMap<String, long[]>> CreateOrientationPositionStat() {
        HashMap<String, HashMap<String, long[]>> OrientationPositionStat = new HashMap<>();
        for (String ori : OriList) {
            OrientationPositionStat.put(ori, new HashMap<>());
            for (String pos : PosList) {
                OrientationPositionStat.get(ori).put(pos, new long[]{0});
            }
        }
        return OrientationPositionStat;
    }

    //-----------------------------------------------------------------------------
    public NoiseReduceStat() {
        for (String ori : OriList) {
            OrientationPositionStat.put(ori, new HashMap<>());
            SameOriPosStat.put(ori, new HashMap<>());
            DiffOriPosStat.put(ori, new HashMap<>());
            for (String pos : PosList) {
                OrientationPositionStat.get(ori).put(pos, new long[]{0});
                SameOriPosStat.get(ori).put(pos, new long[]{0});
                DiffOriPosStat.get(ori).put(pos, new long[]{0});
            }
        }
    }


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
        LinkedList<BedpeFile> list1 = new LinkedList<>(), list2 = new LinkedList<>();
        for (int i = 0; i < Linkers.length; i++) {
            list1.add(linkers[i].InputFile);
            list1.add(linkers[i].SelfLigationFile);
            list1.add(linkers[i].ReLigationFile);
            list1.add(linkers[i].DuplicateFile);
            list1.add(linkers[i].DiffCleanFile);
//            list1.add(linkers[i].CleanFile);
        }
        for (int i = 0; i < Linkers.length; i++) {
            list2.add(linkers[i].SameCleanFile);
            list2.add(linkers[i].DiffCleanFile);
        }
        int[] index = new int[]{0, 0, 0, 0};
        Thread[] t = new Thread[thread];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                BedpeFile temp;
                while (true) {
                    synchronized (t) {
                        if (index[0] >= list1.size()) {
                            break;
                        }
                        temp = list1.get(index[0]);
                        index[0]++;
                    }
                    System.out.println(new Date() + " [Noise Reduce statistic]:\tCalculate item number, file name: " + temp.getName());
                    temp.getItemNum();
                }
                while (true) {
                    int tempIndex;
                    synchronized (t) {
                        if (index[1] >= list2.size()) {
                            break;
                        }
                        tempIndex = index[1];
                        temp = new BedpeFile(list2.get(tempIndex));
                        index[1]++;
                    }
                    System.out.println(new Date() + " [Noise Reduce statistic]:\tCalculate item number, file name: " + temp.getName());
                    //---
                    try {
                        temp.ReadOpen();
                        BedpeItem Item;
                        while ((Item = temp.ReadItem()) != null) {
                            String Key1 = Item.getLocation().getLeft().Orientation + "," + Item.getLocation().getRight().Orientation;
                            String Key2 = Item.Extends[0].charAt(Item.Extends[0].length() - 1) + "," + Item.Extends[2].charAt(Item.Extends[2].length() - 1);
                            synchronized (this) {
                                if (tempIndex % 2 == 0) {
                                    SameOriPosStat.get(Key1).get(Key2)[0]++;
                                } else {
                                    DiffOriPosStat.get(Key1).get(Key2)[0]++;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //---
                }
                while (true) {
                    int j;
                    synchronized (t) {
                        if (index[3] >= Linkers.length) {
                            break;
                        }
                        j = index[3];
                        index[3]++;
                    }
                    System.out.println(new Date() + " [Noise Reduce statistic]:\tCalculate the number of short and long range, file name: " + linkers[j].SameCleanFile.getName());
                    try {
                        long[] result = RangeCount(linkers[j].SameCleanFile);
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
        inFile.ItemNum = 0;
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
                    InteractionRangeDistribution.put(distance, new long[]{0});
                }
                InteractionRangeDistribution.get(distance)[0]++;
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
        show.append("------------------------------------------------------------------------------------------------\n");
        show.append("(Intra chromosome)\t");
        show.append(ShowOriPosStat(SameOriPosStat));
        show.append("(Inter chromosome)\t");
        show.append(ShowOriPosStat(DiffOriPosStat));
        show.append("(total)\t");
        show.append(ShowOriPosStat(OrientationPositionStat));
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
        for (String k1 : OriList) {
            for (String k2 : PosList) {
                OrientationPositionStat.get(k1).get(k2)[0] = SameOriPosStat.get(k1).get(k2)[0] + DiffOriPosStat.get(k1).get(k2)[0];
            }
        }
    }

    @Override
    public void Init() {
        linkers = new Stat[Linkers.length];
        for (int i = 0; i < linkers.length; i++) {
            linkers[i] = new Stat();
            linkers[i].Linker = Linkers[i];
        }
    }

    public String ShowOriPosStat(HashMap<String, HashMap<String, long[]>> map) {
        StringBuilder show = new StringBuilder();
        show.append("Orientation - Position statistic:\n");
        show.append("Item\t").append(String.join("\t", PosList)).append("\n");
        for (String ori : OriList) {
            show.append(ori);
            for (String pos : PosList) {
                show.append("\t").append(new DecimalFormat("#,###").format(map.get(ori).get(pos)[0]));
//                show.append("/").append(String.format("%.2f", (double) OrientationPositionStat.get(ori).get(pos)[0] / total.CleanNum * 100)).append("%");
            }
            show.append("\n");
        }
        return show.toString();
    }

    public void WriteInterRangeDis(AbstractFile f, Region reg, String bin, int x_log, int y_log) throws IOException {
        BufferedWriter output = f.WriteOpen();
        int Bin = 1;
        if (bin != null) {
            if (bin.matches(".*M") | bin.matches(".*m")) {
                Bin = Integer.parseInt(bin.substring(0, bin.length() - 1)) * 1000000;
            } else if (bin.matches(".*K") | bin.matches(".*k")) {
                Bin = Integer.parseInt(bin.substring(0, bin.length() - 1)) * 1000;
            }
        } else {
            bin = "";
        }
        output.write("Distance/(" + bin + ")");
        if (x_log > 1) {
            output.write("(log" + x_log + ")");
        }
        output.write("[" + new DecimalFormat("#,###").format(reg.Start) + ":" + new DecimalFormat("#,###").format(reg.End) + "]");
        if (y_log > 1) {
            output.write("\tCount/(log" + y_log + ")\n");
        } else {
            output.write("\tCount/\n");
        }
        HashMap<Float, long[]> CountMap = new HashMap<>();
        for (int key : InteractionRangeDistribution.keySet()) {
            if (reg.IsContain(key)) {
                float i = (int) Math.ceil((float) key / Bin);
                if (x_log > 1) {
                    i = (float) (Math.log10(i) / Math.log10(x_log));
                }
                if (!CountMap.containsKey(i)) {
                    CountMap.put(i, new long[]{0});
                }
                CountMap.get(i)[0] += InteractionRangeDistribution.get(key)[0];
            }
        }
        ArrayList<Float> Keys = new ArrayList<>(CountMap.keySet());
        Collections.sort(Keys);
        for (float key : Keys) {
            if (y_log > 1) {
                output.write(key + "\t" + Math.log10(CountMap.get(key)[0] / Math.log10(y_log)) + "\n");
            } else {
                output.write(key + "\t" + CountMap.get(key)[0] + "\n");
            }
        }
        output.close();
    }
}
