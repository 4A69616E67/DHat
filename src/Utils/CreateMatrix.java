package Utils;

import Component.File.BedPeFile.BedpeFile;
import Component.File.BedPeFile.BedpeItem;
import Component.File.MatrixFile.MatrixFile;
import Component.File.MatrixFile.MatrixItem;
import Component.tool.Statistic;
import Component.tool.Tools;
import Component.unit.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

public class CreateMatrix {
    private BedpeFile BedpeFile;
    private Chromosome[] Chromosomes;
    private ChrRegion Region1, Region2;
    private ArrayList<ChrRegion> BinSizeList = new ArrayList<>();
    private int Resolution;
    private String Prefix;
    private MatrixFile DenseMatrixFile, SparseMatrixFile;
    private File RegionFile;
    private File BinSizeFile;
    private int Threads;
    public boolean UseCount = false;

    public CreateMatrix(BedpeFile BedpeFile, Chromosome[] Chrs, int Resolution, String Prefix, int Threads) {
        this.BedpeFile = BedpeFile;
        this.Chromosomes = Chrs;
        this.Resolution = Resolution;
        this.Prefix = Prefix;
        this.Threads = Threads;
        Init();
    }

    private CreateMatrix(String[] args) throws IOException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("f").hasArg().argName("file").required().desc("[required] bedpefile").build());
        Argument.addOption(Option.builder("s").hasArg().longOpt("size").argName("file").desc("Chromosomes size file").build());
        Argument.addOption(Option.builder("chr").hasArgs().argName("strings").desc("The chromosome name which you want to calculator").build());
        Argument.addOption(Option.builder("res").hasArg().argName("int").desc("Resolution (default 1M)").build());
        Argument.addOption(Option.builder("region").hasArgs().argName("strings").desc("(sample chr1:0:100 chr4:100:400) region you want to calculator, if not set, will calculator chromosome size").build());
        Argument.addOption(Option.builder("t").hasArg().argName("int").desc("Threads (default 1)").build());
        Argument.addOption(Option.builder("p").hasArg().argName("string").desc("out prefix (default bedpefile)").build());
        Argument.addOption(Option.builder("count").hasArg(false).desc("use count value").build());
        final String helpHeader = "Author: " + Opts.Author;
        final String helpFooter = "Note:\n" +
                "you can set -chr like \"Chr:ChrSize\" or use -s to define the \"ChrSize\"\n" +
                "If you set -s, you can set -chr like \"Chr\"\n" +
                "The file format of option -s is \"Chromosomes    Size\" for each row\n" +
                "We will calculate all chromosome in Chromosomes size file if you don't set -chr\n" +
                "You needn't set -s and -chr if you set -region";
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp Path/" + Opts.JarFile.getName() + " " + CreateMatrix.class.getName(), helpHeader, Argument, helpFooter, true);
            System.exit(1);
        }
        CommandLine ComLine = null;
        try {
            ComLine = new DefaultParser().parse(Argument, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            new HelpFormatter().printHelp("java -cp Path/" + Opts.JarFile.getName() + " " + CreateMatrix.class.getName(), helpHeader, Argument, helpFooter, true);
            System.exit(1);
        }
        BedpeFile = new BedpeFile(ComLine.getOptionValue("f"));
        String[] Chr = ComLine.hasOption("chr") ? ComLine.getOptionValues("chr") : null;
        if (Chr != null) {
            Chromosomes = new Chromosome[Chr.length];
            for (int i = 0; i < Chr.length; i++) {
                Chromosomes[i] = new Chromosome(Chr[i].split(":"));
            }
        }
        String SizeFile = ComLine.hasOption("size") ? ComLine.getOptionValue("size") : null;
        Resolution = ComLine.hasOption("res") ? Integer.parseInt(ComLine.getOptionValue("res")) : Default.Resolution;
        Prefix = ComLine.hasOption("p") ? ComLine.getOptionValue("p") : BedpeFile.getPath();
        Threads = ComLine.hasOption("t") ? Integer.parseInt(ComLine.getOptionValue("t")) : 1;
        Region1 = ComLine.hasOption("region") ? new ChrRegion(ComLine.getOptionValue("region").split(":")) : null;
        Region2 = ComLine.hasOption("region") && ComLine.getOptionValues("region").length > 1 ? new ChrRegion(ComLine.getOptionValues("region")[1].split(":")) : Region1;
        UseCount = ComLine.hasOption("count");
        if (SizeFile != null) {
            List<String> ChrSizeList = FileUtils.readLines(new File(SizeFile), StandardCharsets.UTF_8);
            if (Chromosomes == null) {
                Chromosomes = new Chromosome[ChrSizeList.size()];
                for (int i = 0; i < Chromosomes.length; i++) {
                    Chromosomes[i] = new Chromosome(ChrSizeList.get(i).split("\\s+"));
                }
            } else {
                for (String aChrSizeList : ChrSizeList) {
                    for (Chromosome aChromosome : Chromosomes) {
                        if (aChromosome.Name.equals(aChrSizeList.split("\\s+")[0])) {
                            aChromosome.Size = Integer.parseInt(aChrSizeList.split("\\s+")[1]);
                            break;
                        }
                    }
                }
            }
        }
        Init();
    }

    private void Init() {
        DenseMatrixFile = new MatrixFile(Prefix + ".dense.matrix");
        SparseMatrixFile = new MatrixFile(Prefix + ".sparse.matrix");
        RegionFile = new File(Prefix + ".matrix.Region");
        BinSizeFile = new File(Prefix + ".matrix.BinSize");
    }

    public static void main(String[] args) throws IOException {

        new CreateMatrix(args).Run();

    }

    public Array2DRowRealMatrix Run() throws IOException {
        if (Region1 != null) {
            return Run(Region1, Region2);
        }
        if (Chromosomes == null) {
            System.err.println("Error! no -chr  argument");
            System.exit(1);
        }
        System.out.println(new Date() + "\tBegin to create interaction matrix " + BedpeFile.getName() + " Resolution=" + Resolution + " Threads=" + Threads);
        int SumBin = 0;
        Chromosome[] ChrBinSize = Statistic.CalculatorBinSize(Chromosomes, Resolution);
        Hashtable<String, Integer> IndexBias = new Hashtable<>();
        //计算bin的总数
        for (int i = 0; i < ChrBinSize.length; i++) {
            IndexBias.put(Chromosomes[i].Name, SumBin);
            SumBin = SumBin + ChrBinSize[i].Size;
        }
        if (SumBin > Opts.MaxBinNum) {
            System.err.println("Error ! too many bins, there are " + SumBin + " bins.");
            System.exit(1);
        }
        double[][] intermatrix = new double[SumBin][SumBin];
        int[] DataIndex = IndexParse(BedpeFile);
        BufferedReader infile = new BufferedReader(new FileReader(BedpeFile));
        Thread[] Process = new Thread[Threads];
        //----------------------------------------------------------------------------
        for (int i = 0; i < Threads; i++) {
            int finalSumBin = SumBin;
            Process[i] = new Thread(() -> {
                try {
                    String line;
                    String[] str;
                    while ((line = infile.readLine()) != null) {
                        str = line.split("\\s+");
                        int row = (Integer.parseInt(str[DataIndex[1]]) + Integer.parseInt(str[DataIndex[2]])) / 2 / Resolution;
                        int col = (Integer.parseInt(str[DataIndex[4]]) + Integer.parseInt(str[DataIndex[5]])) / 2 / Resolution;
                        if (!IndexBias.containsKey(str[DataIndex[0]])) {
                            continue;
                        }
                        row += IndexBias.get(str[DataIndex[0]]);
                        if (row >= finalSumBin) {
                            continue;
                        }
                        if (!IndexBias.containsKey(str[DataIndex[3]])) {
                            continue;
                        }
                        col += IndexBias.get(str[DataIndex[3]]);
                        if (col >= finalSumBin) {
                            continue;
                        }
                        synchronized (Process) {
                            if (UseCount) {
                                intermatrix[row][col] += Integer.parseInt(str[DataIndex[5] + 2]);
                            } else {
                                intermatrix[row][col]++;
                            }
                            if (row != col) {
                                intermatrix[col][row] = intermatrix[row][col];
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Process[i].start();
        }
        //-------------------------------------------------
        Tools.ThreadsWait(Process);
        infile.close();
        //--------------------------------------------------------
        //打印矩阵
        Array2DRowRealMatrix InterMatrix = new Array2DRowRealMatrix(intermatrix);
        Tools.PrintMatrix(InterMatrix, DenseMatrixFile, SparseMatrixFile);
        System.out.println(new Date() + "\tEnd to create interaction matrix");
        //--------------------------------------------------------------------
        int temp = 0;
        BufferedWriter outfile = new BufferedWriter(new FileWriter(BinSizeFile));
        BinSizeList = getBinSizeList();
        for (int i = 0; i < BinSizeList.size(); i++) {
            outfile.write(BinSizeList.get(i).toString());
        }
        outfile.close();
        return InterMatrix;
    }//OK

    public Array2DRowRealMatrix Run(ChrRegion reg1, ChrRegion reg2) throws IOException {
        System.out.println(new Date() + "\tBegin to create interaction matrix " + reg1.toString().replace("\t", ":") + " " + reg2.toString().replace("\t", ":"));
        Chromosome[] ChrBinSize;
        ChrBinSize = Statistic.CalculatorBinSize(new Chromosome[]{new Chromosome(reg1.Chr, reg1.region.getLength()), new Chromosome(reg2.Chr, reg2.region.getLength())}, Resolution);
        if (Math.max(ChrBinSize[0].Size, ChrBinSize[1].Size) > Opts.MaxBinNum) {
            System.err.println("Error ! too many bins, there are " + Math.max(ChrBinSize[0].Size, ChrBinSize[1].Size) + " bins.");
            System.exit(0);
        }
        MatrixItem InterMatrix = new MatrixItem(new InterAction(reg1, reg2), Resolution);
        BedpeFile.ReadOpen();
        Thread[] Process = new Thread[Threads];
        //----------------------------------------------------------------------------
        for (int i = 0; i < Threads; i++) {
            Process[i] = new Thread(() -> {
                try {
                    BedpeItem line;
                    String[] str;
                    while ((line = BedpeFile.ReadItem()) != null) {
                        if (!UseCount) {
                            line.Score = 1;
                        }
                        synchronized (InterMatrix) {
                            InterMatrix.add(line.getLocation(), line.Score);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Process[i].start();
        }
        //-------------------------------------------------
        Tools.ThreadsWait(Process);
        BedpeFile.ReadClose();
//        infile.close();
        //--------------------------------------------------------
        //打印矩阵
        Tools.PrintMatrix(InterMatrix.item, DenseMatrixFile, SparseMatrixFile);
        System.out.println(new Date() + "\tEnd to create interaction matrix");
        //--------------------------------------------------------------------
        BufferedWriter outfile = new BufferedWriter(new FileWriter(RegionFile));
        outfile.write(reg1.toString() + "\n");
        outfile.write(reg2.toString() + "\n");
        outfile.close();
        return InterMatrix.item;
    }

    public static int[] Bedpe2Index(ChrRegion reg1, ChrRegion reg2, ChrRegion left, ChrRegion right, int resolution) {
        int[] index = new int[]{-1, -1};
        if (left.IsBelong(reg1) && right.IsBelong(reg2)) {
            index[0] = (left.region.Start - reg1.region.Start) / resolution;
            index[1] = (right.region.Start - reg2.region.Start) / resolution;
        }
        return index;
    }

    public ArrayList<MatrixItem> Run(List<InterAction> list) throws IOException {
        //==================================================初始化矩阵列表==============================
//        ArrayList<MatrixItem> MatrixList = new ArrayList<>();
        for (InterAction action : list) {
            if (action.Left.compareTo(action.Right) > 0) {
                ChrRegion temp = action.Left;
                action.Left = action.Right;
                action.Right = temp;
            }
        }
        Collections.sort(list);
        HashMap<String, HashMap<String, ArrayList<MatrixItem>>> MatrixList = new HashMap<>();
        for (InterAction aList : list) {
            String Left = aList.getLeft().Chr;
            String Right = aList.getRight().Chr;
            if (!MatrixList.containsKey(Left) || !MatrixList.get(Left).containsKey(Right)) {
                MatrixList.put(Left, new HashMap<>());
                MatrixList.get(Left).put(Right, new ArrayList<>());
            }
            MatrixList.get(Left).get(Right).add(new MatrixItem(aList, Resolution));
        }
        //==================================================多线程构建矩阵================================
        BedpeFile.ReadOpen();
        Thread[] t = new Thread[Threads];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                BedpeItem Line;
                try {
                    while ((Line = BedpeFile.ReadItem()) != null) {
                        if (!UseCount) {
                            Line.Score = 1;
                        }
                        String LeftChr = Line.getLocation().getLeft().Chr;
                        String RightChr = Line.getLocation().getRight().Chr;
                        if (MatrixList.containsKey(LeftChr)) {
                            if (MatrixList.get(LeftChr).containsKey(RightChr)) {
                                ArrayList<MatrixItem> temp_list = MatrixList.get(LeftChr).get(RightChr);
                                for (int j = 0; j < temp_list.size(); j++) {
                                    synchronized (temp_list.get(j)) {
                                        temp_list.get(j).add(Line.getLocation().Left.region, Line.getLocation().Right.region, Line.Score);
                                    }
                                }
                            }
                        } else if (MatrixList.containsKey(RightChr)) {
                            if (MatrixList.get(RightChr).containsKey(LeftChr)) {
                                ArrayList<MatrixItem> temp_list = MatrixList.get(RightChr).get(LeftChr);
                                for (int j = 0; j < temp_list.size(); j++) {
                                    synchronized (temp_list.get(j)) {
                                        temp_list.get(j).add(Line.getLocation().Right.region, Line.getLocation().Left.region, Line.Score);
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        ArrayList<MatrixItem> Result = new ArrayList<>();
        for (String key1 : MatrixList.keySet()) {
            for (String key2 : MatrixList.get(key1).keySet()) {
                Result.addAll(MatrixList.get(key1).get(key2));
            }
        }
        return Result;
    }

    public ArrayList<MatrixItem> Run(List<InterAction> list, ArrayList<Integer> Resolution) throws IOException {
        //初始化矩阵列表
        ArrayList<MatrixItem> MatrixList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            MatrixItem aMatrix = new MatrixItem(list.get(i), Resolution.get(i));
            MatrixList.add(aMatrix);
        }
        BedpeFile.ReadOpen();
        //多线程构建矩阵
        Thread[] t = new Thread[Threads];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                BedpeItem Line;
                try {
                    while ((Line = BedpeFile.ReadItem()) != null) {
                        if (!UseCount) {
                            Line.Score = 1;
                        }
                        for (int j = 0; j < list.size(); j++) {
                            synchronized (MatrixList.get(j)) {
                                MatrixList.get(j).add(Line.getLocation(), Line.Score);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        return MatrixList;
    }

    public MatrixFile getSparseMatrixFile() {
        return SparseMatrixFile;
    }

    public MatrixFile getDenseMatrixFile() {
        return DenseMatrixFile;
    }

    public File getBinSizeFile() {
        return BinSizeFile;
    }

    private int[] IndexParse(BedpeFile file) throws IOException {
        int[] Index = new int[6];
        switch (file.BedpeDetect()) {
            case BedpePointFormat:
                Index = new int[]{0, 1, 1, 2, 3, 3};
                break;
            case BedpeRegionFormat:
                Index = new int[]{0, 1, 2, 3, 4, 5};
                break;
            case EmptyFile:
                break;
            default:
                System.err.println(new Date() + "\t" + "[" + CreateMatrix.class.getName() + "]\tError format!");
                System.exit(1);
        }
        return Index;
    }

    public ArrayList<ChrRegion> getBinSizeList() {
        if (BinSizeList.size() == 0) {
            Chromosome[] ChrBinSize = Statistic.CalculatorBinSize(Chromosomes, Resolution);
            int temp = 0;
            BinSizeList = new ArrayList<>();
            for (int i = 0; i < Chromosomes.length; i++) {
                temp = temp + 1;
                ChrRegion region = new ChrRegion(Chromosomes[i].Name, temp, 0);
                temp = temp + ChrBinSize[i].Size - 1;
                region.region.End = temp;
                BinSizeList.add(region);
            }
        }
        return BinSizeList;
    }
}
