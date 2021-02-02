package Utils;

import Component.File.MatrixFile.MatrixFile;
import Component.File.MatrixFile.MatrixItem;
import Component.unit.Opts;
import org.apache.commons.cli.*;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import java.io.IOException;

/**
 * Created by snowf on 2019/12/30.
 */

public class MatrixCorrelation {
    enum Method {
        COSINE, PEARSON, SPEARMAN
    }

    public static void main(String[] args) throws IOException, ParseException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("m").longOpt("method").hasArg().argName("string").desc("method of calculate correlation ('Cosine, pearson, spearman')").build());
        Argument.addOption(Option.builder("1").hasArg().argName("file").required().desc("input file 1").build());
        Argument.addOption(Option.builder("2").hasArg().argName("file").required().desc("input file 2").build());
        Argument.addOption(Option.builder("s").hasArg().argName("int").required().desc("matrix size").build());
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getName() + " " + MatrixCorrelation.class.getName() + " <-1 file1> <-2 file2> [option]", Argument);
            System.exit(1);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        MatrixFile file1 = new MatrixFile(Opts.GetStringOpt(ComLine, "1", ""));
        MatrixFile file2 = new MatrixFile(Opts.GetStringOpt(ComLine, "2", ""));
        int Size = Opts.GetIntOpt(ComLine,"s",0);
        RealMatrix matrix1, matrix2;
        System.err.println("Read " + file1 + " ......");
        file1.ReadOpen();
        matrix1 = file1.ReadItem().item;
        file1.ReadClose();
        if (MatrixFile.FormatDetection(file1)== MatrixFile.Format.SparseMatrix){
            System.err.println("convert to dense matrix");
            matrix1= MatrixItem.Sparse2Dense(matrix1,1,Size);
        }

        System.err.println("Read " + file2 + " ......");
        file2.ReadOpen();
        matrix2 = file2.ReadItem().item;
        file2.ReadClose();
        if (MatrixFile.FormatDetection(file2)== MatrixFile.Format.SparseMatrix){
            System.err.println("convert to dense matrix");
            matrix2= MatrixItem.Sparse2Dense(matrix2,1,Size);
        }
        String method = Opts.GetStringOpt(ComLine, "m", "cosine");
        boolean flag = false;
        for (Method m : Method.values()) {
            if (m.name().equalsIgnoreCase(method)) {
                System.err.println("Method : " + method);
                System.out.println(Calculate(matrix1, matrix2, m));
                flag = true;
                break;
            }
        }
        if (!flag) {
            System.err.println("Unknown method : " + method);
        }
    }

    public static double Calculate(RealMatrix matrix1, RealMatrix matrix2, Method method) {
        RealVector vector1 = new ArrayRealVector();
        for (int i = 0; i < matrix1.getRowDimension(); i++) {
            vector1 = vector1.append(matrix1.getRowVector(i));
        }
        RealVector vector2 = new ArrayRealVector();
        for (int i = 0; i < matrix2.getRowDimension(); i++) {
            vector2 = vector2.append(matrix2.getRowVector(i));
        }
        return Calculate(vector1, vector2, method);
    }

    public static double Calculate(RealVector vector1, RealVector vector2, Method method) {
        switch (method) {
            case COSINE:
                return vector1.dotProduct(vector2) / Math.sqrt(StatUtils.sum(vector1.ebeMultiply(vector1).toArray())) / Math.sqrt(StatUtils.sum(vector2.ebeMultiply(vector2).toArray()));
            case PEARSON:
                return new PearsonsCorrelation().correlation(vector1.toArray(), vector2.toArray());
            case SPEARMAN:
                return new SpearmansCorrelation().correlation(vector1.toArray(), vector2.toArray());
            default:
                return 0;
        }
    }
}
