package Utils;

import Component.File.BedPeFile.BedpeFile;
import Component.File.BedPeFile.BedpeItem;
import Component.File.MatrixFile.MatrixItem;
import Component.Statistic.StatUtil;
import Component.tool.Tools;
import Component.unit.ChrRegion;
import Component.unit.InterAction;
import Component.unit.Opts;
import org.apache.commons.cli.*;
import org.apache.commons.math3.linear.RealMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by snowf on 2020/1/9.
 */

public class APAGenerator {
    private BedpeFile InteractionFile;
    private BedpeFile ClusterFile;
    private int BinSize = 10000;
    private int BinNumber = 5;
    private String Title = "";
    public int Threads = 1;
    public int MergeLen = -1;
    public int MaxDistance = Integer.MAX_VALUE;
    public int MinDistance = Integer.MIN_VALUE;


    public APAGenerator(BedpeFile interactionFile, BedpeFile clusterFile, int binSize, int binNumber) {
        InteractionFile = interactionFile;
        ClusterFile = clusterFile;
        BinSize = binSize <= 0 ? BinSize : binSize;
        BinNumber = binNumber <= 0 ? BinNumber : binNumber;
    }


    public static void main(String[] args) throws ParseException, IOException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("i").longOpt("input").hasArg().argName("file").desc("input bedpe file").required().build());
        Argument.addOption(Option.builder("l").longOpt("list").hasArg().argName("file").desc("interaction cluster file").required().build());
        Argument.addOption(Option.builder("bs").longOpt("binsize").hasArg().argName("int").desc("length for each bin (default 1000)").build());
        Argument.addOption(Option.builder("bn").longOpt("binnumber").hasArg().argName("int").desc("the number of extend bin (default 10)").build());
        Argument.addOption(Option.builder("t").longOpt("thread").hasArg().argName("int").desc("the number of thread (default 1)").build());
        Argument.addOption(Option.builder("title").hasArg().argName("string").desc("figure title").build());
        Argument.addOption(Option.builder("minD").longOpt("mindistance").hasArg().argName("int").desc("program only consider the distance of two anchor large than this value (default 0)").build());
        Argument.addOption(Option.builder("maxD").longOpt("maxdistance").hasArg().argName("int").desc("program only consider the distance of two anchor less than this value (default inf)").build());
        Argument.addOption(Option.builder("merge").hasArg().argName("int").desc("merge interactions which distance less than this value (default 0)").build());
        Argument.addOption(Option.builder("o").longOpt("outfile").hasArg().argName("file").desc("out file name").build());
        if (args.length < 2) {
            new HelpFormatter().printHelp("java -cp Path/" + Opts.JarFile.getName() + " " + APAGenerator.class.getName(), Argument, true);
            System.exit(1);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        BedpeFile infile1 = new BedpeFile(Opts.GetFileOpt(ComLine, "i", null));
        BedpeFile infile2 = new BedpeFile(Opts.GetFileOpt(ComLine, "l", null));
        String title = Opts.GetStringOpt(ComLine, "title", "");
        int bin_size = Opts.GetIntOpt(ComLine, "bs", 1000);
        int bin_number = Opts.GetIntOpt(ComLine, "bn", 10);
        int merge_length = Opts.GetIntOpt(ComLine, "merge", 0);
        APAGenerator generator = new APAGenerator(infile1, infile2, bin_size, bin_number);
        generator.MaxDistance = Opts.GetIntOpt(ComLine, "maxD", Integer.MAX_VALUE);
        generator.MinDistance = Opts.GetIntOpt(ComLine, "minD", Integer.MIN_VALUE);
        generator.Threads = Opts.GetIntOpt(ComLine, "t", 1);
        generator.MergeLen = merge_length;
        generator.setTitle(title);
        BufferedImage image = generator.Run();
        File outFile = Opts.GetFileOpt(ComLine, "o", new File("Test.APA.png"));
        ImageIO.write(image, "png", outFile);
    }

    public BufferedImage Run() throws IOException {
        ClusterFile.ReadOpen();
        ArrayList<InterAction> List = new ArrayList<>();
        BedpeItem item;
        int ExtendLength = (int) (BinSize * (BinNumber + 0.5));
        while ((item = ClusterFile.ReadItem()) != null) {
            if (item.getLocation().Distance() > MaxDistance || item.getLocation().Distance() < MinDistance) {
                continue;
            }
            if (item.getLocation().getLeft().compareTo(item.getLocation().getRight()) > 0) {
                List.add(new InterAction(item.getLocation().getRight(), item.getLocation().getLeft()));
            } else {
                List.add(item.getLocation());
            }
        }
        ClusterFile.ReadClose();
        Collections.sort(List);
        if (MergeLen > 0) {
            List = new PetCluster(List, MergeLen / 2, MergeLen / 2, Threads).Run();
        }
        for (int i = 0; i < List.size(); i++) {
            int n1 = List.get(i).getLeft().region.Center();
            int n2 = List.get(i).getRight().region.Center();
            List.get(i).getLeft().region.Start = n1 - ExtendLength + 1;
            List.get(i).getLeft().region.End = n1 + ExtendLength;
            List.get(i).getRight().region.Start = n2 - ExtendLength + 1;
            List.get(i).getRight().region.End = n2 + ExtendLength;
        }
        CreateMatrix matrix = new CreateMatrix(InteractionFile, null, BinSize, "out", Threads);
        matrix.UseCount = false;
        ArrayList<MatrixItem> MatrixList = matrix.Run(List);
        double[][] TotalMatrix = new double[BinNumber * 2 + 1][BinNumber * 2 + 1];
        for (MatrixItem aMatrix : MatrixList) {
            for (int i = 0; i < TotalMatrix.length; i++) {
                for (int j = 0; j < TotalMatrix[i].length; j++) {
                    TotalMatrix[i][j] += aMatrix.item.getEntry(i, j);
                }
            }
        }
        MatrixItem matrixItem = new MatrixItem(TotalMatrix);
        int Width = matrixItem.item.getColumnDimension();
        int Height = matrixItem.item.getRowDimension();
        //------------------------------------------------
        matrixItem.Chr1 = matrixItem.Chr2 = new ChrRegion("", -ExtendLength, ExtendLength);
        matrixItem.unit = MatrixItem.Unit.KB;
        BufferedImage image = matrixItem.DrawHeatMap(BinSize, 0.95f, false);
        //--------------------------------------------
        double BLValue, BRValue, ULValue, URValue, CenterValue, XLeft, XRight, YUp, YDown;
        RealMatrix CentreMatrix = matrixItem.item.getSubMatrix(Height / 3, Height - Height / 3 - 1, Width / 3, Width - Width / 3 - 1);//CenterMatrix
        CenterValue = StatUtil.sum(CentreMatrix) / CentreMatrix.getColumnDimension() / CentreMatrix.getRowDimension() / MatrixList.size();
        RealMatrix ULMatrix = matrixItem.item.getSubMatrix(0, Height / 3 - 1, 0, Width / 3 - 1);//ULMatrix
        ULValue = CenterValue / (StatUtil.sum(ULMatrix) / ULMatrix.getColumnDimension() / ULMatrix.getRowDimension() / MatrixList.size());
        RealMatrix URMatrix = matrixItem.item.getSubMatrix(0, Height / 3 - 1, Width - Width / 3, Width - 1);//URMatrix
        URValue = CenterValue / (StatUtil.sum(URMatrix) / URMatrix.getColumnDimension() / URMatrix.getRowDimension() / MatrixList.size());
        RealMatrix BLMatrix = matrixItem.item.getSubMatrix(Height - Height / 3, Height - 1, 0, Width / 3 - 1);//BLMatrix;
        BLValue = CenterValue / (StatUtil.sum(BLMatrix) / BLMatrix.getColumnDimension() / BLMatrix.getRowDimension() / MatrixList.size());
        RealMatrix BRMatrix = matrixItem.item.getSubMatrix(Height - Height / 3, Height - 1, Width - Width / 3, Width - 1);//BRMatrix
        BRValue = CenterValue / (StatUtil.sum(BRMatrix) / BRMatrix.getColumnDimension() / BRMatrix.getRowDimension() / MatrixList.size());
        //----------------------------------
        int fold = matrixItem.getFold();
        int marginal = matrixItem.getMarginal();
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        Font t = new Font("Times New Roman", Font.PLAIN, 50);
        g.setStroke(new BasicStroke(2.0f));
        XLeft = marginal + Width * fold / 6;
        XRight = marginal + Width * fold - Width * fold / 6;
        YUp = marginal + Height * fold / 6;
        YDown = marginal + Height * fold - Height * fold / 6;
        //--BLMatrix
        g.drawRect(marginal, marginal + (Height - Height / 3) * fold, Width / 3 * fold, Height / 3 * fold);
        Tools.DrawStringCenter(g, String.format("%.4f", BLValue), t, (int) XLeft, (int) YDown, 0);
        //--BRMatrix
        g.drawRect(marginal + (Width - Width / 3) * fold, marginal + (Height - Height / 3) * fold, Width / 3 * fold, Height / 3 * fold);
        Tools.DrawStringCenter(g, String.format("%.4f", BRValue), t, (int) XRight, (int) YDown, 0);
        //--ULMatrix
        g.drawRect(marginal, marginal, Width / 3 * fold, Height / 3 * fold);
        Tools.DrawStringCenter(g, String.format("%.4f", ULValue), t, (int) XLeft, (int) YUp, 0);
        //--URMatrix
        g.drawRect(marginal + (Width - Width / 3) * fold, marginal, Width / 3 * fold, Height / 3 * fold);
        Tools.DrawStringCenter(g, String.format("%.4f", URValue), t, (int) XRight, (int) YUp, 0);
        //-----------------------------------
        Tools.DrawStringCenter(g, Title + "(P2LL=" + String.format("%.4f", BLValue) + ")", new Font("Times New Roman", Font.PLAIN, 90), marginal + Width * fold / 2, marginal - 60, 0);
        g.drawLine(marginal + Width * fold, marginal, marginal + Width * fold, marginal + Height * fold);
        g.drawLine(marginal, marginal, marginal + Width * fold, marginal);
        return image;
    }

    public void setTitle(String title) {
        Title = title;
    }
}
