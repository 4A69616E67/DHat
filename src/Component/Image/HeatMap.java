package Component.Image;

import Component.File.FileTool;
import org.apache.commons.cli.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
/**
 * Created by snowf on 2019/2/17.
 *
 */
public class HeatMap {
    private double[][] Matrix;
    private int Width;
    private int Height;
    private int Count;
    private int ColorType = 1;
    private double MaxValue = 0;
    private BufferedImage HeatMapImage;

    public HeatMap(File matrixfile) throws IOException {
        Matrix = FileTool.ReadMatrixFile(matrixfile);
        Init();
    }

    public HeatMap(int height, int width) {
        Matrix = new double[height][width];
        Init();
    }

    public HeatMap(double[][] matrix) {
        Matrix = matrix;
        Init();
    }

    private void Init() {
        Height = Matrix.length;
        Width = Matrix[0].length;
        double[] rowCount = new double[Height];
        double[] colCount = new double[Width];
        for (int i = 0; i < Height; i++) {
            for (int j = 0; j < Width; j++) {
                rowCount[i] += Matrix[i][j];
                colCount[j] += Matrix[i][j];
                if (Matrix[i][j] > MaxValue) {
                    MaxValue = Matrix[i][j];
                }
            }
            Count += rowCount[i];
        }
    }

    public HeatMap Draw() {
        HeatMapImage = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < Height; i++) {
            for (int j = 0; j < Width; j++) {
                if (Matrix[i][j] == 0) {
                    HeatMapImage.setRGB(j, i, Color.WHITE.getRGB());
                } else {
//                    im.setRGB(j, i, new Color(255, 255 - (int) (Math.sqrt(Matrix[i][j] / Max) * 255), 255 - (int) (Math.sqrt(Matrix[i][j] / Max) * 255)).getRGB());
                    HeatMapImage.setRGB(j, i, new Color(255, 0, 0).getRGB());
                }
            }
        }
        return this;
    }

    public static void main(String[] args) throws IOException, ParseException {
        Options Argument = new Options();
        Argument.addOption("f", true, "Matrix file");
        Argument.addOption("o", true, "Out file");
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp DLO-HiC-AnalysisTools.jar Component.Image.HeatMap [option]", Argument);
            System.exit(1);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        File MatrixFile = new File(ComLine.getOptionValue("f"));
        String OutFile = ComLine.getOptionValue("o");
        HeatMap map = new HeatMap(MatrixFile);
        BufferedImage im = map.Draw().getImage();
        ImageIO.write(im, "png", new File(OutFile));
    }

    public BufferedImage getImage() {
        return HeatMapImage;
    }

    private void setRegionColor(int x1, int y1, int x2, int y2, Color c) {
        for (int i = Math.min(x1, x2); i <= Math.max(x1, x2); i++) {
            for (int j = Math.min(y1, y2); j <= Math.max(y1, y2); j++) {
                HeatMapImage.setRGB(j, i, c.getRGB());
            }
        }
    }

    public void setMatrix(double[][] matrix) {
        Matrix = matrix;
    }

    public void Set(int x, int y, double v) {
        Matrix[x][y] = v;
    }

}
