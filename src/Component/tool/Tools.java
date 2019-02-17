package Component.tool;

import Component.File.CommonFile;
import Component.unit.Chromosome;
import Component.unit.Opts;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.*;
import java.util.*;

/**
 * Created by snowf on 2019/2/17.
 *
 */

public class Tools {
    private Tools() {

    }

    public static void ThreadsWait(Thread[] T) {
        for (Thread t : T) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {
            }
        }
    }

    public static String ArraysToString(Object[] o) {
        String s = "";
        if (o != null) {
            for (Object i : o) {
                s += i + " ";
            }
            s = s.trim();
        }
        return s;
    }

    public static String ArraysToString(int[] o) {
        String s = "";
        if (o != null) {
            for (int i : o) {
                s += i + " ";
            }
            s = s.trim();
        }

        return s;
    }

    public static Hashtable<String, Integer> ExtractChrSize(File ChrSizeFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(ChrSizeFile));
        String Line;
        String[] Str;
        Hashtable<String, Integer> ChrSize = new Hashtable<>();
        while ((Line = reader.readLine()) != null) {
            Str = Line.split("\\s+");
            ChrSize.put(Str[0], Integer.parseInt(Str[1]));
        }
        reader.close();
        return ChrSize;
    }

    public static Chromosome[] CheckChromosome(Chromosome[] Chrs, File GenomeFile) throws IOException {
        ArrayList<Chromosome> TempChrSize = null;
        if (Chrs == null || Chrs.length == 0) {
            TempChrSize = CalculateChrSize(GenomeFile);
            Chrs = new Chromosome[TempChrSize.size()];
            for (int i = 0; i < TempChrSize.size(); i++) {
                Opts.ChrSize.put(TempChrSize.get(i).Name, TempChrSize.get(i).Size);
                Chrs[i] = new Chromosome(TempChrSize.get(i).Name, TempChrSize.get(i).Size);
            }
            return Chrs;
        } else {
            for (Chromosome Chr : Chrs) {
                if (Chr.Size == 0) {
                    TempChrSize = CalculateChrSize(GenomeFile);
                    for (Chromosome aTempChrSize : TempChrSize) {
                        Opts.ChrSize.put(aTempChrSize.Name, aTempChrSize.Size);
                        for (int i = 0; i < Chrs.length; i++) {
                            if (aTempChrSize.Name.equals(Chrs[i].Name)) {
                                Chrs[i] = aTempChrSize;
                                break;
                            }
                        }
                    }
                    for (int i = 0; i < Chrs.length; i++) {
                        if (Chrs[i].Size == 0) {
                            System.err.println(new Date() + "\tWarning! No " + Chrs[i].Name + " in genomic file");
                        }
                    }
                    return Chrs;
                }
            }
            return Chrs;
        }
    }

    public static ArrayList<Chromosome> CalculateChrSize(File GenomeFile) throws IOException {
        ArrayList<Chromosome> ChrList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(GenomeFile));
        String Line, Chr;
        int Size = 0;
        Line = reader.readLine();
        while (Line != null && !Line.matches("^>.+")) {
            Line = reader.readLine();
        }
        Chr = Line.split("\\s+")[0].replace(">", "");
        Line = reader.readLine();
        while (Line != null) {
            if (Line.matches("^>.+")) {
                ChrList.add(new Chromosome(Chr, Size));
                Chr = Line.split("\\s+")[0].replace(">", "");
                Size = 0;
            } else {
                Size += Line.length();
            }
            Line = reader.readLine();
        }
        ChrList.add(new Chromosome(Chr, Size));
        return ChrList;
    }

    public static void PrintList(ArrayList<?> List, File OutFile) throws IOException {
        BufferedWriter outfile = new BufferedWriter(new FileWriter(OutFile));
        for (Object s : List) {
            outfile.write(s + "\n");
        }
        outfile.close();
    }

    public static void PrintMatrix(RealMatrix Matrix, File DenseFile, File SpareMatrix) throws IOException {
        BufferedWriter twodfile = new BufferedWriter(new FileWriter(DenseFile));
        BufferedWriter sparefile = new BufferedWriter(new FileWriter(SpareMatrix));
        //打印二维矩阵
        for (int i = 0; i < Matrix.getRowDimension(); i++) {
            for (double data : Matrix.getRow(i)) {
                twodfile.write(data + "\t");
            }
            twodfile.write("\n");
        }
        //打印稀疏矩阵
        for (int i = 0; i < Matrix.getRowDimension(); i++) {
            for (int j = 0; j < Matrix.getColumnDimension(); j++) {
                if (Matrix.getEntry(i, j) != 0) {
                    sparefile.write((i + 1) + "\t" + (j + 1) + "\t" + Matrix.getEntry(i, j) + "\n");
                }
            }
        }
        sparefile.close();
        twodfile.close();
    }

    public static String DateFormat(long Date) {
        return Date / 3600 + "H" + (Date % 3600) / 60 + "M" + (Date % 3600) % 60 + "S";
    }

    public static double UnitTrans(double Num, String PrimaryUint, String TransedUint) {
        String[] Unit = new String[]{"B", "b", "K", "k", "M", "m", "G", "g"};
        Double[] Value = new Double[]{1D, 1D, 1e3, 1e3, 1e6, 1e6, 1e9, 1e9};
        HashMap<String, Double> UnitMap = new HashMap<>();
        for (int i = 0; i < Unit.length; i++) {
            UnitMap.put(Unit[i], Value[i]);
        }
        return Num * UnitMap.get(PrimaryUint) / UnitMap.get(TransedUint);
    }

    public static int ExecuteCommandStr(String CommandStr, PrintWriter Out, PrintWriter Error) throws IOException, InterruptedException {
        int ExitValue;
        System.out.println(new Date() + "\t" + CommandStr);
        Process P = Runtime.getRuntime().exec(CommandStr);
        Thread OutThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String line;
                    BufferedReader bufferedReaderIn = new BufferedReader(new InputStreamReader(P.getInputStream()));
                    if (Out != null) {
                        while ((line = bufferedReaderIn.readLine()) != null) {
                            Out.print(line + "\n");
                            Out.flush();
                        }
                        bufferedReaderIn.close();
                    } else {
                        while (bufferedReaderIn.readLine() != null) ;
                        bufferedReaderIn.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread ErrThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String line;
                    BufferedReader bufferedReaderIn = new BufferedReader(new InputStreamReader(P.getErrorStream()));
                    if (Error != null) {
                        while ((line = bufferedReaderIn.readLine()) != null) {
                            Error.write(line + "\n");
                            Error.flush();
                        }
                        bufferedReaderIn.close();
                    } else {
                        while (bufferedReaderIn.readLine() != null) ;
                        bufferedReaderIn.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        OutThread.start();
        ErrThread.start();
        OutThread.join();
        ErrThread.join();
        ExitValue = P.waitFor();
        return ExitValue;
    }

    public static int RemoveEmptyFile(File Dir) {
        int Count = 0;
        File[] FileList = Dir.listFiles();
        if (FileList == null) {
            return Count;
        }
        for (File file : FileList) {
            if (file.isFile() && file.length() == 0) {
                file.delete();
                Count++;
            } else {
                Count += RemoveEmptyFile(file);
            }
        }
        return Count;
    }

    public static String[] GetKmer(String str, int l) {
        if (l > str.length()) {
            return new String[0];
        }
        String[] Kmer = new String[l + 1];
        int length = str.length() - l;
        for (int i = 0; i < l + 1; i++) {
            Kmer[i] = str.substring(i, i + length);
        }
        return Kmer;
    }

    public static String ReverseComple(String str) {
        char[] RevComStr = new StringBuffer(str).reverse().toString().toCharArray();
        for (int k = 0; k < RevComStr.length; k++) {
            switch (RevComStr[k]) {
                case 'A':
                case 'a':
                    RevComStr[k] = 'T';
                    break;
                case 'T':
                case 't':
                    RevComStr[k] = 'A';
                    break;
                case 'C':
                case 'c':
                    RevComStr[k] = 'G';
                    break;
                case 'G':
                case 'g':
                    RevComStr[k] = 'C';
            }
        }
        return new String(RevComStr);
    }

    public static File[] CheckEnzyFragFile(Chromosome[] s) {
        File[] EnzyFragBedFile = new File[s.length];
        if (new File(Opts.OutDir.EnzyFragDir.toString()).isDirectory()) {
            File[] list_files = new File(Opts.OutDir.EnzyFragDir.toString()).listFiles();
            if (list_files != null) {
                for (int i = 0; i < s.length; i++) {
                    boolean flag = false;
                    for (File file : list_files) {
                        if (file.getName().matches(".*\\." + s[i].Name + "\\..*")) {
                            EnzyFragBedFile[i] = new CommonFile(file);
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        return null;
                    }
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
        return EnzyFragBedFile;
    }

}
