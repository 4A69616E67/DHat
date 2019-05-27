package Component.Process;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import Component.File.AbstractFile;
import Component.File.BedFile.BedFile;
import Component.File.BedFile.BedItem;
import Component.File.BedPeFile.BedpeFile;
import Component.File.BedPeFile.BedpeItem;
import Component.Statistic.NoiseReduce.NoiseReduceStat;
import Component.tool.Tools;
import Component.unit.*;
import org.apache.commons.cli.*;

/**
 * Created by snowf on 2019/2/17.
 */
public class BedpeProcess {
    private File OutPath = new File("./");
    private String Prefix = Configure.Prefix;
    private BedpeFile BedpeFile;
    private BedFile[] EnzyFile;
    private BedFile AllEnzyFile;
    private String Restriction;
    public int Threads = 1;
    private Chromosome[] Chromosomes;
    private File GenomeFile;
    private BedpeFile SameFile;//染色体内的交互文件
    private BedpeFile DiffFile;//染色体间的交互文件
    private BedpeFile FragmentDiffFile;//定位的染色体间的交互文件
    private BedpeFile FragmentLocationFile;//总的交互定位文件
    private BedpeFile[] ChrFragLocationFile;//每条染色体的交互定位
    /**
     * 每一行表示一条染色体，每一列表示一种连接类型例如：
     * <p>Chr1  Chr2  Chr3  Chr4</p>
     * <p>Sel    -     -     -     -</p>
     * <p>Rel    -     -     -     -</p>
     * <p>Valid  -     -     -     -</p>
     */
    private BedpeFile[][] ChrLigationFile;
    private BedpeFile SelfLigationFile;//总的自连接文件=SUM(ChrLigationFile[0][:])
    private BedpeFile ReLigationFile;//总的再连接文件=SUM(ChrLigationFile[1][:])
    private BedpeFile ValidFile;//总的有效数据文件=SUM(ChrLigationFile[2][:])
    private BedpeFile[] ChrSameFile;//每条染色体内的交互
    private BedpeFile[] ChrSameNoDumpFile;//每条染色体内的交互（去duplication）=ChrLigationFile[2][:]去duplication
    private BedpeFile[] ChrSameRepetaFile;///每条染色体内的重复片段
    private BedpeFile SameNoDumpFile;//最终的染色体内的交互文件=SUM(ChrSameNoDumpFile[:])
    private BedpeFile DiffNoDumpFile;//最终的染色体间的交互文件=DiffFile去duplication
    private BedpeFile DiffRepeatFile;
    private File EnzyDir;
    private BedpeFile RepeatFile;//最终的重复片段
    private BedpeFile FinalFile;//最终文件=SameNoDumpFile+DiffNoDumpFile
    //============================================================
    private HashMap<String, HashMap<String, long[]>>[] SameOriPosStat, DiffOriPosStat;
//    private HashMap<Integer, long[]> InteractionDistanceDistribution = new HashMap<>();

    public static void main(String[] args) throws IOException, ParseException {
        BedpeProcess bedpe = new BedpeProcess(args);
        bedpe.Run();
    }

    //================================================================

    public BedpeProcess(String[] args) throws ParseException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("i").longOpt("bedpe").hasArg().argName("file").desc("bedpe file").required().build());
        Argument.addOption(Option.builder("o").longOpt("out").hasArg().argName("string").desc("output path (default " + OutPath + ")").build());
        Argument.addOption(Option.builder("p").longOpt("prefix").hasArg().argName("string").desc("out prefix (default " + Prefix + ")").build());
        Argument.addOption(Option.builder("e").longOpt("enzyme").hasArg().argName("path").desc("directory which include enzyme fragment file").build());//待修改，改成支持目录和文件
        Argument.addOption(Option.builder("g").longOpt("genome").hasArg().argName("file").desc("reference genome file").build());
        Argument.addOption(Option.builder("c").longOpt("chr").hasArgs().argName("strings").desc("chromosome you want to processing").build());
        Argument.addOption(Option.builder("t").longOpt("ThreadNum").hasArgs().argName("int").desc("threads").build());
        Argument.addOption(Option.builder("r").longOpt("restriction").hasArg().argName("string").desc("restriction site sequence").build());
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getName() + " " + BedpeProcess.class.getName(), Argument, true);
            System.exit(1);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        BedpeFile = new BedpeFile(Opts.GetFileOpt(ComLine, "i", new File("")));
        OutPath = Opts.GetFileOpt(ComLine, "o", OutPath);
        Prefix = Opts.GetStringOpt(ComLine, "p", Prefix);
        EnzyDir = Opts.GetFileOpt(ComLine, "e", null);
        GenomeFile = Opts.GetFileOpt(ComLine, "g", null);
        Restriction = Opts.GetStringOpt(ComLine, "r", null);
        Threads = Opts.GetIntOpt(ComLine, "t", Threads);
        String[] str = Opts.GetStringOpts(ComLine, "c", null);
        if (str != null) {
            Chromosomes = new Chromosome[str.length];
            for (int i = 0; i < str.length; i++) {
                Chromosomes[i] = new Chromosome(str[i]);
            }
        }
        Init();
    }

    public BedpeProcess(File OutPath, String Prefix, Chromosome[] Chrs, BedpeFile BedpeFile) {
        this.OutPath = OutPath;
        this.Prefix = Prefix;
        this.Chromosomes = Chrs;
        this.BedpeFile = BedpeFile;
        this.EnzyFile = Opts.fragmentDigestedModule.getChrsFragmentFile();
        this.AllEnzyFile = Opts.fragmentDigestedModule.getAllChrsFragmentFile();
        Init();
    }

    public void Run() throws IOException {
        ArrayList<Thread> ThreadList = new ArrayList<>();
//        if (EnzyFile == null) {
//            FindRestrictionSite fd = new FindRestrictionSite(GenomeFile, OutPath, Restriction, Prefix);
//            fd.Run();
//            File[] EnzyFileTemps = fd.getChrFragmentFile();
//            EnzyFile = new BedFile[Chromosomes.length];
//            for (int i = 0; i < Chromosomes.length; i++) {
//                for (File EnzyFileTemp : EnzyFileTemps) {
//                    if (EnzyFileTemp.getName().matches(".*\\." + Chromosomes[i].Name + "\\..*")) {
//                        EnzyFile[i] = new BedFile(EnzyFileTemp);
//                    }
//                }
//            }
//        }
        //===============================================================================================
        //将bedpe分成染色体内的交互和染色体间的交互
        BedpeToSameAndDiff(BedpeFile, SameFile, DiffFile);
        ChrSameFile = SameFile.SeparateBedpe(Chromosomes, OutPath + "/" + Prefix, Threads);
        SameOriPosStat = new HashMap[Chromosomes.length];
        DiffOriPosStat = new HashMap[1];
        //=====================================染色体内的交互处理=========================================
        ThreadIndex index = new ThreadIndex(-2);
        Thread[] t = new Thread[Threads];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                int finalI = index.Add(1);
                try {
                    //===========================================染色体间的交互处理======================================
                    if (finalI < 0) {
                        BedpeFile SortDiffFile = new BedpeFile(FragmentDiffFile + ".sort");
//                        AllEnzyFile.Merge(EnzyFile);
                        FragmentLocation(DiffFile, new BedFile(AllEnzyFile.getPath()), FragmentDiffFile);
                        FragmentDiffFile.SplitSortFile(SortDiffFile);
                        DiffOriPosStat[0] = RemoveRepeat(SortDiffFile, DiffNoDumpFile, DiffRepeatFile);//去duplication
                        finalI = index.Add(1);
                    }
                    //=======================================染色体内的交互处理==========================================
                    while (finalI < Chromosomes.length) {
                        //定位交互发生在哪个酶切片段
                        FragmentLocation(ChrSameFile[finalI], new BedFile(EnzyFile[finalI].getPath()), ChrFragLocationFile[finalI]);
                        //区分不同的连接类型（自连接，再连接，有效数据）
                        SeparateLigationType(ChrFragLocationFile[finalI], ChrLigationFile[0][finalI], ChrLigationFile[1][finalI], ChrLigationFile[2][finalI]);
                        BedpeFile SortChrLigationFile = new BedpeFile(ChrLigationFile[2][finalI] + ".sort");
                        //按交互位置排序
                        ChrLigationFile[2][finalI].SortFile(SortChrLigationFile);
                        //去除duplication
                        SameOriPosStat[finalI] = RemoveRepeat(SortChrLigationFile, ChrSameNoDumpFile[finalI], ChrSameRepetaFile[finalI]);
                        if (Configure.DeBugLevel < 1) {
                            AbstractFile.delete(SortChrLigationFile);
                        }
                        finalI = index.Add(1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        //==============================================================================================================
        synchronized (Opts.NRStat) {
            for (int i = 0; i < SameOriPosStat.length; i++) {
                for (String k1 : NoiseReduceStat.OriList) {
                    for (String k2 : NoiseReduceStat.PosList) {
                        if (i == 0) {
                            Opts.NRStat.DiffOriPosStat.get(k1).get(k2)[0] += DiffOriPosStat[0].get(k1).get(k2)[0];
                        }
                        Opts.NRStat.SameOriPosStat.get(k1).get(k2)[0] += SameOriPosStat[i].get(k1).get(k2)[0];
                    }
                }
            }
        }
        AbstractFile[] NeedRemove = new AbstractFile[]{SameNoDumpFile, SelfLigationFile, ReLigationFile, ValidFile, FragmentLocationFile, RepeatFile};
        for (int i = 0; i < NeedRemove.length; i++) {
            if (NeedRemove[i].exists() && !NeedRemove[i].delete()) {
                System.err.println(new Date() + "\tWarning! Can't delete " + NeedRemove[i].getName());
            }
            if (!NeedRemove[i].clean()) {
                NeedRemove[i] = new BedpeFile(NeedRemove[i] + ".temp");
                System.err.println("Create another file " + NeedRemove[i]);
            }
        }
        index.setIndex(-1);
        for (int i = 0; i < Threads; i++) {
            t[i] = new Thread(() -> {
                int finalI = index.Add(1);
                try {
                    while (finalI < 6) {
                        switch (finalI) {
                            case 0:
                                SameNoDumpFile.Merge(ChrSameNoDumpFile);//合并染色体内的交互（去除duplication）
                                FinalFile.Merge(new BedpeFile[]{SameNoDumpFile, DiffNoDumpFile});
                                break;
                            case 1:
                                SelfLigationFile.Merge(ChrLigationFile[0]);//合并自连接
                                break;
                            case 2:
                                ReLigationFile.Merge(ChrLigationFile[1]);//合并再连接
                                break;
                            case 3:
                                ValidFile.Merge(ChrLigationFile[2]);//合并有效数据（未去duplication）
                                break;
                            case 4:
                                FragmentLocationFile.Merge(ChrFragLocationFile);//合并定位的交互片段
                                FragmentLocationFile.Append(FragmentDiffFile);
                                break;
                            case 5:
                                RepeatFile.Merge(ChrSameRepetaFile);//合并重复片段
                                RepeatFile.Append(DiffRepeatFile);
                                break;
                        }
                        finalI = index.Add(1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        for (int i = 0; i < Chromosomes.length; i++) {
            if (Configure.DeBugLevel < 1) {
                for (int j = 0; j < 3; j++) {
                    AbstractFile.delete(ChrLigationFile[j][i]);//删除（自连接，再连接，有效数据）
                }
                AbstractFile.delete(ChrSameRepetaFile[i]);
                AbstractFile.delete(ChrFragLocationFile[i]);//删除每条染色体的交互片段定位的文件（只保留包含全部染色体的一个文件）
            }
        }
    }

    public Component.File.BedPeFile.BedpeFile getFinalFile() {
        return FinalFile;
    }


    private void Init() {
        File LigationDir = new File(OutPath + "/Ligation");
        File MiddleDir = new File(OutPath + "/Temp");//存放中间文件的目录
        File FinalDir = new File(OutPath + "/Clean");//存放最终结果的目录
        File[] CheckDir = new File[]{OutPath, LigationDir, MiddleDir, FinalDir};
        for (File f : CheckDir) {
            synchronized (BedpeProcess.class) {
                if (!f.isDirectory() && !f.mkdir()) {
                    System.err.println(BedpeProcess.class.getName() + ":\tERROR! Can't Create " + f);
                    System.exit(1);
                }
            }
        }
//        if (EnzyDir == null && GenomeFile == null && EnzyFile == null) {
//            System.err.println(BedpeProcess.class.getName() + ":\tError! No Enzyme fragment file and Genome file");
//            System.exit(1);
//        }
//        if (EnzyDir != null) {
//            File[] files = EnzyDir.listFiles();
//            if (files == null) {
//                System.err.println(EnzyDir + " is not a directory");
//                System.exit(1);
//            }
//            EnzyFile = new CommonFile[Chromosomes.length];
//            for (int i = 0; i < Chromosomes.length; i++) {
//                for (File file : files) {
//                    if (file.getName().matches(".*\\." + Chromosomes[i].Name + "\\..*")) {
//                        EnzyFile[i] = new CommonFile(file);
//                    }
//                }
//            }
//        }
        //===========================================================================
//        AllEnzyFile = new BedFile(OutPath + "/" + Prefix + ".restriction_fragment.bed");
        SameFile = new BedpeFile(OutPath + "/" + Prefix + ".same.bedpe");
        DiffFile = new BedpeFile(OutPath + "/" + Prefix + ".diff.bedpe");
        FragmentDiffFile = new BedpeFile(OutPath + "/" + Prefix + ".diff.frag.bedpe");
        SelfLigationFile = new BedpeFile(LigationDir + "/" + Prefix + ".self.bedpe");
        ReLigationFile = new BedpeFile(LigationDir + "/" + Prefix + ".re.bedpe");
        ValidFile = new BedpeFile(LigationDir + "/" + Prefix + ".valid.bedpe");
        FragmentLocationFile = new BedpeFile(LigationDir + "/" + Prefix + ".enzy.bedpe");
        ChrSameFile = new BedpeFile[Chromosomes.length];
        ChrFragLocationFile = new BedpeFile[Chromosomes.length];
        ChrLigationFile = new BedpeFile[3][Chromosomes.length];
        ChrSameNoDumpFile = new BedpeFile[Chromosomes.length];
        ChrSameRepetaFile = new BedpeFile[Chromosomes.length];
        for (int j = 0; j < Chromosomes.length; j++) {
            ChrFragLocationFile[j] = new BedpeFile(MiddleDir + "/" + Prefix + "." + Chromosomes[j].Name + ".enzy.bedpe");
            ChrLigationFile[0][j] = new BedpeFile(MiddleDir + "/" + Prefix + "." + Chromosomes[j].Name + ".self.bedpe");
            ChrLigationFile[1][j] = new BedpeFile(MiddleDir + "/" + Prefix + "." + Chromosomes[j].Name + ".re.bedpe");
            ChrLigationFile[2][j] = new BedpeFile(MiddleDir + "/" + Prefix + "." + Chromosomes[j].Name + ".valid.bedpe");
            ChrSameNoDumpFile[j] = new BedpeFile(FinalDir + "/" + Prefix + "." + Chromosomes[j].Name + ".same.clean.bedpe");
            ChrSameRepetaFile[j] = new BedpeFile(FinalDir + "/" + Prefix + "." + Chromosomes[j].Name + ".same.repeat.bedpe");
        }
        SameNoDumpFile = new BedpeFile(FinalDir + "/" + Prefix + ".same.clean.bedpe");
        DiffNoDumpFile = new BedpeFile(FinalDir + "/" + Prefix + ".diff.clean.bedpe");
        DiffRepeatFile = new BedpeFile(FinalDir + "/" + Prefix + ".diff.repeat.bedpe");
        FinalFile = new BedpeFile(FinalDir + "/" + Prefix + "." + "final.bedpe");
        RepeatFile = new BedpeFile(FinalDir + "/" + Prefix + ".repeat.bedpe");
    }

    private void FragmentLocation(BedpeFile BedpeFile, BedFile EnySiteFile, File OutFile) throws IOException {
        Hashtable<String, ArrayList<Region>> EnySiteList = new Hashtable<>();
//        BufferedReader EnySiteRead = new BufferedReader(new FileReader(EnySiteFile));
//        BufferedReader SeqRead = new BufferedReader(new FileReader(BedpeFile));
        BufferedWriter OutWrite = new BufferedWriter(new FileWriter(OutFile));
        BedItem bedItem;
        BedpeItem bedpeItem;
        String[] str;
        System.out.println(new Date() + "\tBegin to find eny site\t" + BedpeFile.getName());
        //---------------------------------------------------
        EnySiteFile.ReadOpen();
        while ((bedItem = EnySiteFile.ReadItem()) != null) {
            if (!EnySiteList.containsKey(bedItem.getLocation().Chr)) {
                EnySiteList.put(bedItem.getLocation().Chr, new ArrayList<>());
            }
            EnySiteList.get(bedItem.getLocation().Chr).add(bedItem.getLocation().region);
        }
        EnySiteFile.ReadClose();
        BedpeFile.ReadOpen();
        while ((bedpeItem = BedpeFile.ReadItem()) != null) {
            String[] chr = new String[]{bedpeItem.getLocation().getLeft().Chr, bedpeItem.getLocation().getRight().Chr};
            Region[] position = new Region[]{bedpeItem.getLocation().getLeft().region, bedpeItem.getLocation().getRight().region};
            FragSite[] index = new FragSite[position.length];
            //-------------------------------------------------
            for (int i = 0; i < position.length; i++) {
                if (EnySiteList.get(chr[i]) == null) {
                    index = null;
                    break;
                }
            }
            if (index == null) {
                continue;
            }
            //-----------------------二分法查找-----------------
            for (int i = 0; i < position.length; i++) {
                index[i] = Location(EnySiteList.get(chr[i]), position[i]);
            }
            //-----------------------------------------------------------------
            OutWrite.write(bedpeItem.toString());
            for (FragSite anIndex : index) {
                OutWrite.write("\t" + anIndex);
            }
            OutWrite.write("\n");
        }
        //----------------------------------------------------
        BedpeFile.ReadClose();
        OutWrite.close();
        System.out.println(new Date() + "\tEnd to find eny site\t" + BedpeFile.getName());
    }//OK

    private void SeparateLigationType(BedpeFile InFile, BedpeFile SelfFile, BedpeFile RelFile, BedpeFile ValidFile) throws IOException {
        BufferedReader infile = new BufferedReader(new FileReader(InFile));
        BufferedWriter selffile = new BufferedWriter(new FileWriter(SelfFile));
        BufferedWriter religfile = new BufferedWriter(new FileWriter(RelFile));
        BufferedWriter valifile = new BufferedWriter(new FileWriter(ValidFile));
        InFile.ItemNum = SelfFile.ItemNum = RelFile.ItemNum = ValidFile.ItemNum = 0;
        System.out.println(new Date() + "\tBegin to separate ligation\t" + InFile.getName());
        String line;
        String[] str;
        FragSite[] fss = new FragSite[2];
        while ((line = infile.readLine()) != null) {
            InFile.ItemNum++;
            str = line.split("\\s+");
            fss[0] = new FragSite(str[str.length - 4]);
            fss[1] = new FragSite(str[str.length - 2]);
            if (fss[0].getSite() == fss[1].getSite()) {
                selffile.write(line + "\n");
                SelfFile.ItemNum++;
            } else if ((fss[1].getSite() - fss[0].getSite() == 1) && (fss[1].getOrientation() - fss[0].getOrientation() == -1)) {
                religfile.write(line + "\n");
                RelFile.ItemNum++;
            } else {
                valifile.write(line + "\n");
                ValidFile.ItemNum++;
            }
        }
        infile.close();
        selffile.close();
        religfile.close();
        valifile.close();
        System.out.println(new Date() + "\tEnd separate ligation\t" + InFile.getName());
    }//OK

    /**
     * 二分法查找
     *
     * @param list search list
     * @param site search site
     */
    private FragSite Location(ArrayList<Region> list, Region site) {
        if (list == null) {
            return null;
        }
        int i = 0, j = list.size();
        int MinDis = Integer.MAX_VALUE, MinIndex = 0, dis, p = 0;
        Region item;
        FragSite fs;
        while (i < j) {
            p = (i + j) / 2;
            item = list.get(p);
            if (site.Start > item.End) {
                i = p + 1;
            } else if (site.End < item.Start) {
                j = p - 1;
            } else {
                MinDis = Math.min(Math.abs(site.Start - item.Start), Math.abs(site.End - item.End));
                MinIndex = p;
                break;
            }
        }
        if (i >= j) {
            p = i;
        }
        for (int k = p; k >= 0 && list.get(k).IsOverlap(site); k--) {
            item = list.get(k);
            dis = Math.min(Math.abs(site.Start - item.Start), Math.abs(site.End - item.End));
            if (dis < MinDis) {
                MinDis = dis;
                MinIndex = k;
            }
        }
        for (int k = p + 1; k < list.size() && list.get(k).IsOverlap(site); k++) {
            item = list.get(k);
            dis = Math.min(Math.abs(site.Start - item.Start), Math.abs(site.End - item.End));
            if (dis < MinDis) {
                MinDis = dis;
                MinIndex = k;
            }
        }
        if (Math.abs(site.Start - list.get(MinIndex).Start) <= Math.abs(site.End - list.get(MinIndex).End)) {
            fs = new FragSite(MinIndex + 1, 's');
            fs.setDistance(site.Start - list.get(MinIndex).Start);
        } else {
            fs = new FragSite(MinIndex + 1, 't');
            fs.setDistance(site.End - list.get(MinIndex).End);
        }
        return fs;
    }

    private HashMap<String, HashMap<String, long[]>> RemoveRepeat(BedpeFile InFile, BedpeFile CleanFile, BedpeFile RepeatFile) throws IOException {
        HashMap<String, HashMap<String, long[]>> OriPosStat = NoiseReduceStat.CreateOrientationPositionStat();
        BufferedReader infile = new BufferedReader(new FileReader(InFile));
        BufferedWriter clean_file = new BufferedWriter(new FileWriter(CleanFile));
        BufferedWriter repeat_file = new BufferedWriter(new FileWriter(RepeatFile));
        InFile.ItemNum = CleanFile.ItemNum = RepeatFile.ItemNum = 0;
        ArrayList<String[]> TempList = new ArrayList<>();
        String line;
        String[] Str;
        System.out.println(new Date() + "\tStart remove repeat\t" + InFile.getName());
        while ((line = infile.readLine()) != null) {
            InFile.ItemNum++;
            Str = line.split("\\s+");
            boolean flag = false;
            for (int i = 0; i < TempList.size(); i++) {
                String[] tempstr = TempList.get(i);
                if (Str[0].equals(tempstr[0]) && Str[10].equals(tempstr[4])) {
                    if (Str[3].equals(tempstr[1]) && Str[8].equals(tempstr[2]) && Str[9].equals(tempstr[3]) && Str[12].equals(tempstr[5])) {
                        flag = true;
                        repeat_file.write(line + "\n");
                        RepeatFile.ItemNum++;
                        break;
                    }
                } else {
                    TempList.clear();
                    break;
                }
            }
            if (!flag) {
                String key1 = Str[8] + "," + Str[9];
                String key2 = Str[10].charAt(Str[10].length() - 1) + "," + Str[12].charAt(Str[12].length() - 1);
                OriPosStat.get(key1).get(key2)[0]++;
                TempList.add(new String[]{Str[0], Str[3], Str[8], Str[9], Str[10], Str[12]});
                clean_file.write(line + "\n");
                CleanFile.ItemNum++;
            }
        }
        infile.close();
        clean_file.close();
        repeat_file.close();
        System.out.println(new Date() + "\tRemove repeat finish\t" + InFile.getName());
        return OriPosStat;
    }//OK

    private void BedpeToSameAndDiff(BedpeFile BedpeFile, BedpeFile SameBedpeFile, BedpeFile DiffBedpeFile) throws IOException {
        BufferedReader BedpeRead = new BufferedReader(new FileReader(BedpeFile));
        BufferedWriter SameBedpeWrite = new BufferedWriter(new FileWriter(SameBedpeFile));
        BufferedWriter DiffBedpeWrite = new BufferedWriter(new FileWriter(DiffBedpeFile));
        BedpeFile.ItemNum = SameBedpeFile.ItemNum = DiffBedpeFile.ItemNum = 0;
        Thread[] process = new Thread[Threads];
        System.out.println(new Date() + "\tBegin Separate bedpe file\t" + BedpeFile.getName());
        for (int i = 0; i < Threads; i++) {
            process[i] = new Thread(() -> {
                String line;
                String[] str;
                try {
                    while ((line = BedpeRead.readLine()) != null) {
                        str = line.split("\\s+");
                        if (str[0].equals(str[3])) {
                            //---------------------------取相同染色体上的交互-----------------------
                            synchronized (SameBedpeWrite) {
                                SameBedpeWrite.write(line + "\n");
                                SameBedpeFile.ItemNum++;
                            }
                        } else {
                            //--------------------------取不同染色体上的交互----------------------
                            synchronized (DiffBedpeWrite) {
                                DiffBedpeWrite.write(line + "\n");
                                DiffBedpeFile.ItemNum++;
                            }
                        }
                        synchronized (process) {
                            BedpeFile.ItemNum++;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            process[i].start();
        }
        Tools.ThreadsWait(process);
        BedpeRead.close();
        SameBedpeWrite.close();
        DiffBedpeWrite.close();
    }//OK

    public BedpeFile[] getChrSameNoDumpFile() {
        return ChrSameNoDumpFile;
    }

    public BedpeFile getSameNoDumpFile() {
        return SameNoDumpFile;
    }

    public BedpeFile getDiffNoDumpFile() {
        return DiffNoDumpFile;
    }

    public BedpeFile getDiffFile() {
        return DiffFile;
    }

    public BedpeFile getSelfLigationFile() {
        return SelfLigationFile;
    }

    public BedpeFile getReLigationFile() {
        return ReLigationFile;
    }

    public BedpeFile getValidFile() {
        return ValidFile;
    }

    public Component.File.BedPeFile.BedpeFile getRepeatFile() {
        return RepeatFile;
    }

    public BedpeFile getSameFile() {
        return SameFile;
    }
}

class FragSite {
    private int Site;
    private char Orientation;
    private int distance;

    FragSite(int i, char s) {
        Site = i;
        Orientation = s;
    }

    FragSite(String s) {
        String[] Str = s.split("-");
        Site = Integer.parseInt(Str[0]);
        Orientation = Str[1].charAt(0);
    }

    @Override
    public String toString() {
        return Site + "-" + Orientation + "\t" + distance;
    }

    public int getSite() {
        return Site;
    }

    public char getOrientation() {
        return Orientation;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
