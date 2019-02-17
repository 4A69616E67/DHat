package Component.Process;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import Component.File.BedpeFile;
import Component.File.CommonFile;
import Component.tool.FindRestrictionSite;
import Component.tool.Tools;
import Component.unit.*;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
/**
 * Created by snowf on 2019/2/17.
 *
 */
public class BedpeProcess {
    private File OutPath = new File("./");
    private String Prefix = Configure.Prefix;
    private BedpeFile BedpeFile;
    private CommonFile[] EnzyFile;
    private CommonFile AllEnzyFile;
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
     * Sel     Rel     Valid
     * Chr1  -       -       -
     * Chr2  -       -       -
     * Chr3  -       -       -
     * Chr4  -       -       -
     */
    private BedpeFile[][] ChrLigationFile;
    private BedpeFile SelfLigationFile;//总的自连接文件=SUM(ChrLigationFile[:][0])
    private BedpeFile ReLigationFile;//总的再连接文件=SUM(ChrLigationFile[:][1])
    private BedpeFile ValidFile;//总的有效数据文件=SUM(ChrLigationFile[:][2])
    private BedpeFile[] ChrSameFile;//每条染色体内的交互
    private BedpeFile[] ChrSameNoDumpFile;//每条染色体内的交互（去duplication）=ChrLigationFile[:][2]去duplication
    private BedpeFile[] ChrSameRepetaFile;///每条染色体内的重复片段
    private BedpeFile SameNoDumpFile;//最终的染色体内的交互文件=SUM(ChrSameNoDumpFile[:])
    private BedpeFile DiffNoDumpFile;//最终的染色体间的交互文件=DiffFile去duplication
    private BedpeFile DiffRepeatFile;
    private File EnzyDir;
    private BedpeFile RepeatFile;//最终的重复片段
    private BedpeFile FinalFile;//最终文件=SameNoDumpFile+DiffNoDumpFile
    //============================================================

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
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

    public BedpeProcess(File OutPath, String Prefix, Chromosome[] Chrs, CommonFile[] EnzyFile, BedpeFile BedpeFile) {
        this.OutPath = OutPath;
        this.Prefix = Prefix;
        this.Chromosomes = Chrs;
        this.BedpeFile = BedpeFile;
        this.EnzyFile = EnzyFile;
        Init();
    }

    public void Run() throws IOException {
        if (EnzyFile == null) {
            FindRestrictionSite fd = new FindRestrictionSite(GenomeFile, OutPath, Restriction, Prefix);
            fd.Run();
            File[] EnzyFileTemps = fd.getChrFragmentFile();
            EnzyFile = new CommonFile[Chromosomes.length];
            for (int i = 0; i < Chromosomes.length; i++) {
                for (File EnzyFileTemp : EnzyFileTemps) {
                    if (EnzyFileTemp.getName().matches(".*\\." + Chromosomes[i].Name + "\\..*")) {
                        EnzyFile[i] = new CommonFile(EnzyFileTemp);
                    }
                }
            }
        }
        //===============================================================================================
        //将bedpe分成染色体内的交互和染色体间的交互
        BedpeToSameAndDiff(BedpeFile, SameFile, DiffFile);
        ChrSameFile = SameFile.SeparateBedpe(Chromosomes, OutPath + "/" + Prefix, Threads);
        //=====================================染色体内的交互处理=========================================
        Thread[] Process = new Thread[Chromosomes.length];
        for (int i = 0; i < Chromosomes.length; i++) {
            int finalI = i;
            Process[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //定位交互发生在哪个酶切片段
                        FragmentLocation(ChrSameFile[finalI], EnzyFile[finalI], ChrFragLocationFile[finalI]);
                        //区分不同的连接类型（自连接，再连接，有效数据）
                        SeparateLigationType(ChrFragLocationFile[finalI], ChrLigationFile[finalI][0], ChrLigationFile[finalI][1], ChrLigationFile[finalI][2]);
                        BedpeFile SortChrLigationFile = new BedpeFile(ChrLigationFile[finalI][2] + ".sort");
                        //按交互位置排序
                        ChrLigationFile[finalI][2].SortFile(SortChrLigationFile);
                        //去除duplication
                        RemoveRepeat(SortChrLigationFile, ChrSameNoDumpFile[finalI], ChrSameRepetaFile[finalI]);
                        if (Configure.DeBugLevel < 1) {
                            SortChrLigationFile.delete();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            Process[i].start();
        }
        Tools.ThreadsWait(Process);
        //==========================================染色体间的交互处理==================================================
        BedpeFile SortDiffFile = new BedpeFile(FragmentDiffFile + ".sort");
        AllEnzyFile.Merge(EnzyFile);
        FragmentLocation(DiffFile, AllEnzyFile, FragmentDiffFile);
        FragmentDiffFile.SplitSortFile(SortDiffFile);
        RemoveRepeat(SortDiffFile, DiffNoDumpFile, DiffRepeatFile);//去duplication
        //================================================================
        File[] NeedRemove = new File[]{SameNoDumpFile, SelfLigationFile, ReLigationFile, ValidFile, FragmentLocationFile};
        for (File f : NeedRemove) {
            if (f.exists() && !f.delete()) {
                System.err.println(new Date() + "\tWarning! Can't delete " + f.getName());
            }
        }
        SameNoDumpFile.delete();
        SelfLigationFile.delete();
        ReLigationFile.delete();
        ValidFile.delete();
        FragmentLocationFile.delete();
        FileUtils.touch(SameNoDumpFile);
        FileUtils.touch(SelfLigationFile);
        FileUtils.touch(ReLigationFile);
        FileUtils.touch(ValidFile);
        FileUtils.touch(FragmentLocationFile);
        for (int j = 0; j < Chromosomes.length; j++) {
            SameNoDumpFile.Append(ChrSameNoDumpFile[j]);//合并染色体内的交互（去除duplication）
            SelfLigationFile.Append(ChrLigationFile[j][0]);//合并自连接
            ReLigationFile.Append(ChrLigationFile[j][1]);//合并再连接
            ValidFile.Append(ChrLigationFile[j][2]);//合并有效数据（未去duplication）
            FragmentLocationFile.Append(ChrFragLocationFile[j]);//合并定位的交互片段
            RepeatFile.Append(ChrSameRepetaFile[j]);
            //删除中间文件
            if (Configure.DeBugLevel < 1) {
                for (int i = 0; i < 3; i++) {
                    ChrLigationFile[j][i].delete();//删除（自连接，再连接，有效数据）
                }
                ChrFragLocationFile[j].delete();//删除每条染色体的交互片段定位的文件（只保留包含全部染色体的一个文件）
            }

        }
        FinalFile.Merge(new BedpeFile[]{SameNoDumpFile, DiffNoDumpFile});
        RepeatFile.Append(DiffRepeatFile);
    }

    public Component.File.BedpeFile getFinalFile() {
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
        if (EnzyDir == null && GenomeFile == null && EnzyFile == null) {
            System.err.println(BedpeProcess.class.getName() + ":\tError! No Enzyme fragment file and Genome file");
            System.exit(1);
        }
        if (EnzyDir != null) {
            File[] files = EnzyDir.listFiles();
            if (files == null) {
                System.err.println(EnzyDir + " is not a directory");
                System.exit(1);
            }
            EnzyFile = new CommonFile[Chromosomes.length];
            for (int i = 0; i < Chromosomes.length; i++) {
                for (File file : files) {
                    if (file.getName().matches(".*\\." + Chromosomes[i].Name + "\\..*")) {
                        EnzyFile[i] = new CommonFile(file);
                    }
                }
            }
        }
        //===========================================================================
        AllEnzyFile = new CommonFile(OutPath + "/" + Prefix + ".restriction_fragment.bed");
        SameFile = new BedpeFile(OutPath + "/" + Prefix + ".same.bedpe");
        DiffFile = new BedpeFile(OutPath + "/" + Prefix + ".diff.bedpe");
        FragmentDiffFile = new BedpeFile(OutPath + "/" + Prefix + ".diff.frag.bedpe");
        SelfLigationFile = new BedpeFile(LigationDir + "/" + Prefix + ".self.bedpe");
        ReLigationFile = new BedpeFile(LigationDir + "/" + Prefix + ".rel.bedpe");
        ValidFile = new BedpeFile(LigationDir + "/" + Prefix + ".valid.bedpe");
        FragmentLocationFile = new BedpeFile(LigationDir + "/" + Prefix + ".enzy.bedpe");
        ChrSameFile = new BedpeFile[Chromosomes.length];
        ChrFragLocationFile = new BedpeFile[Chromosomes.length];
        ChrLigationFile = new BedpeFile[Chromosomes.length][3];
        ChrSameNoDumpFile = new BedpeFile[Chromosomes.length];
        ChrSameRepetaFile = new BedpeFile[Chromosomes.length];
        for (int j = 0; j < Chromosomes.length; j++) {
            ChrFragLocationFile[j] = new BedpeFile(MiddleDir + "/" + Prefix + "." + Chromosomes[j].Name + ".enzy.bedpe");
            ChrLigationFile[j][0] = new BedpeFile(MiddleDir + "/" + Prefix + "." + Chromosomes[j].Name + ".self.bedpe");
            ChrLigationFile[j][1] = new BedpeFile(MiddleDir + "/" + Prefix + "." + Chromosomes[j].Name + ".rel.bedpe");
            ChrLigationFile[j][2] = new BedpeFile(MiddleDir + "/" + Prefix + "." + Chromosomes[j].Name + ".valid.bedpe");
            ChrSameNoDumpFile[j] = new BedpeFile(FinalDir + "/" + Prefix + "." + Chromosomes[j].Name + ".same.clean.bedpe");
            ChrSameRepetaFile[j] = new BedpeFile(FinalDir + "/" + Prefix + "." + Chromosomes[j].Name + ".same.repeat.bedpe");
        }
        SameNoDumpFile = new BedpeFile(FinalDir + "/" + Prefix + ".same.clean.bedpe");
        DiffNoDumpFile = new BedpeFile(FinalDir + "/" + Prefix + ".diff.clean.bedpe");
        DiffRepeatFile = new BedpeFile(FinalDir + "/" + Prefix + ".diff.repeat.bedpe");
        FinalFile = new BedpeFile(FinalDir + "/" + Prefix + "." + "final.bedpe");
        RepeatFile = new BedpeFile(FinalDir + "/" + Prefix + ".repeat.bedpe");
    }

    private void FragmentLocation(File BedpeFile, File EnySiteFile, File OutFile) throws IOException {
//        ArrayList<int[]> EnySiteList = new ArrayList<>();
        Hashtable<String, ArrayList<Region>> EnySiteList = new Hashtable<>();
        BufferedReader EnySiteRead = new BufferedReader(new FileReader(EnySiteFile));
        BufferedReader SeqRead = new BufferedReader(new FileReader(BedpeFile));
        BufferedWriter OutWrite = new BufferedWriter(new FileWriter(OutFile));
        String line;
        String[] str;
        System.out.println(new Date() + "\tBegin to find eny site\t" + BedpeFile.getName());
        //---------------------------------------------------
        while ((line = EnySiteRead.readLine()) != null) {
            str = line.split("\\s+");
            if (!EnySiteList.containsKey(str[2])) {
                EnySiteList.put(str[2], new ArrayList<>());
            }
            EnySiteList.get(str[2]).add(new Region(Integer.parseInt(str[str.length - 2]), Integer.parseInt(str[str.length - 1])));
        }
        EnySiteRead.close();
        while ((line = SeqRead.readLine()) != null) {
            str = line.split("\\s+");
            String[] chr = new String[]{str[0], str[3]};
            Region[] position = new Region[]{new Region(Integer.parseInt(str[1]), Integer.parseInt(str[2])), new Region(Integer.parseInt(str[4]), Integer.parseInt(str[5]))};
            FragSite[] index = new FragSite[position.length];
            //-----------------------二分法查找-----------------
            for (int i = 0; i < position.length; i++) {
                index[i] = Location(EnySiteList.get(chr[i]), position[i]);
            }
            //-----------------------------------------------------------------
            OutWrite.write(line);
            for (int i = 0; i < index.length; i++) {
                OutWrite.write("\t" + index[i]);
            }
            OutWrite.write("\n");
        }
        //----------------------------------------------------
        SeqRead.close();
        OutWrite.close();
        System.out.println(new Date() + "\tEnd to find eny site\t" + BedpeFile.getName());
    }//OK

    private void SeparateLigationType(File InFile, File SelfFile, File ReligFile, File ValidFile) throws IOException {
        BufferedReader infile = new BufferedReader(new FileReader(InFile));
        BufferedWriter selffile = new BufferedWriter(new FileWriter(SelfFile));
        BufferedWriter religfile = new BufferedWriter(new FileWriter(ReligFile));
        BufferedWriter valifile = new BufferedWriter(new FileWriter(ValidFile));
        System.out.println(new Date() + "\tBegin to seperate ligation\t" + InFile.getName());
        String line;
        String[] str;
        FragSite[] fss = new FragSite[2];
        while ((line = infile.readLine()) != null) {
            str = line.split("\\s+");
            fss[0] = new FragSite(str[str.length - 4]);
            fss[1] = new FragSite(str[str.length - 2]);
            if (fss[0].getSite() == fss[1].getSite()) {
                selffile.write(line + "\n");
            } else if ((fss[1].getSite() - fss[0].getSite() == 1) && (fss[1].getOrientation() - fss[0].getOrientation() == -1)) {
                religfile.write(line + "\n");
            } else {
                valifile.write(line + "\n");
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
     * @param list
     * @param site
     * @return null 如果没有找到相应的片段返回null
     */
    private FragSite Location(ArrayList<Region> list, Region site) {
        int i = 0, j = list.size();
        int MinDis = Integer.MAX_VALUE, MinIndex = 0, dis, p = 0;
        Region item;
        FragSite fs = null;
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
        for (int k = p - 1; list.get(k).IsOverlap(site); k--) {
            item = list.get(k);
            dis = Math.min(Math.abs(site.Start - item.Start), Math.abs(site.End - item.End));
            if (dis < MinDis) {
                MinDis = dis;
                MinIndex = k;
            }
        }
        for (int k = p + 1; list.get(k).IsOverlap(site); k++) {
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

    private void RemoveRepeat(File InFile, File CleanFile, File RepeatFile) throws IOException {
        BufferedReader infile = new BufferedReader(new FileReader(InFile));
        BufferedWriter clean_file = new BufferedWriter(new FileWriter(CleanFile));
        BufferedWriter repeat_file = new BufferedWriter(new FileWriter(RepeatFile));
        ArrayList<String[]> TempList = new ArrayList<>();
        String line;
        String[] Str;
        System.out.println(new Date() + "\tStart to remove repeat\t" + InFile.getName());
        while ((line = infile.readLine()) != null) {
            Str = line.split("\\s+");
            boolean flag = false;
            for (int i = 0; i < TempList.size(); i++) {
                String[] tempstr = TempList.get(i);
                if (Str[0].equals(tempstr[0]) && Str[11].equals(tempstr[4])) {
                    if (Str[3].equals(tempstr[1]) && Str[9].equals(tempstr[2]) && Str[10].equals(tempstr[3]) && Str[13].equals(tempstr[5])) {
                        flag = true;
                        repeat_file.write(line + "\n");
                        break;
                    }
                } else {
                    TempList.clear();
                    break;
                }
            }
            if (!flag) {
                TempList.add(new String[]{Str[0], Str[3], Str[9], Str[10], Str[11], Str[13]});
                clean_file.write(line + "\n");
            }
        }
        infile.close();
        clean_file.close();
        repeat_file.close();
        System.out.println(new Date() + "\tEnd to remove repeat\t" + InFile.getName());
    }//OK

    private void BedpeToSameAndDiff(File BedpeFile, File SameBedpeFile, File DiffBedpeFile) throws IOException {
        BufferedReader BedpeRead = new BufferedReader(new FileReader(BedpeFile));
        BufferedWriter SameBedpeWrite = new BufferedWriter(new FileWriter(SameBedpeFile));
        BufferedWriter DiffBedpeWrite = new BufferedWriter(new FileWriter(DiffBedpeFile));
        Thread[] process = new Thread[Threads];
        System.out.println(new Date() + "\tBegin to Seperate bedpe file\t" + BedpeFile.getName());
        for (int i = 0; i < Threads; i++) {
            process[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    String line;
                    String[] str;
                    try {
                        while ((line = BedpeRead.readLine()) != null) {
                            str = line.split("\\s+");
                            if (str[0].equals(str[3])) {
                                //---------------------------取相同染色体上的交互-----------------------
                                if (Integer.parseInt(str[1]) < Integer.parseInt(str[4])) {
                                    synchronized (SameBedpeWrite) {
                                        SameBedpeWrite.write(line + "\n");
                                    }
                                } else {
                                    synchronized (SameBedpeWrite) {
                                        SameBedpeWrite.write(str[3] + "\t" + str[4] + "\t" + str[5] + "\t" + str[0] + "\t" + str[1] + "\t" + str[2] + "\t" + str[6] + "\t" + str[8] + "\t" + str[7] + "\t" + str[10] + "\t" + str[9] + "\n");
                                    }
                                }
                            } else {
                                //--------------------------取不同染色体上的交互----------------------
                                if (str[0].compareTo(str[3]) < 0) {
                                    synchronized (DiffBedpeWrite) {
                                        DiffBedpeWrite.write(line + "\n");
                                    }
                                } else {
                                    synchronized (DiffBedpeWrite) {
                                        DiffBedpeWrite.write(str[3] + "\t" + str[4] + "\t" + str[5] + "\t" + str[0] + "\t" + str[1] + "\t" + str[2] + "\t" + str[6] + "\t" + str[8] + "\t" + str[7] + "\t" + str[10] + "\t" + str[9] + "\n");
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

    public BedpeFile getSameFile() {
        return SameFile;
    }
}

class FragSite {
    private int Site;
    private char Orientation = 's';
    private int distance;

    FragSite(int i, char s) {
        Site = i;
        Orientation = s;
    }

    FragSite(int i, char s, int d) {
        this(i, s);
        distance = d;
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
