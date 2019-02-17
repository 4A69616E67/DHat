package Component.File;

import Component.unit.*;

import java.io.*;
import java.util.ArrayList;

public class FileTool {

    public static String[] Read4Line(BufferedReader file) {
        String[] Str = new String[4];
        try {
            Str[0] = file.readLine();
            Str[1] = file.readLine();
            Str[2] = file.readLine();
            Str[3] = file.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Str;
    }

    public static Opts.FileFormat ReadsType(File fastqfile) throws IOException {
        int LineNumber = 100, i = 0, Count = 0;
        BufferedReader reader = new BufferedReader(new FileReader(fastqfile));
        String line;
        try {
            reader.readLine();
            line = reader.readLine();
        } catch (NullPointerException e) {
            return Opts.FileFormat.ErrorFormat;
        }
        while (line != null) {
            i++;
            Count += line.length();
            if (i >= LineNumber) {
                break;
            }
            try {
                reader.readLine();
                reader.readLine();
                reader.readLine();
                line = reader.readLine();
            } catch (NullPointerException e) {
                break;
            }
        }
        if (i == 0) {
            return Opts.FileFormat.ErrorFormat;
        }
        if (Count / i >= 70) {
            return Opts.FileFormat.LongReads;
        } else {
            return Opts.FileFormat.ShortReads;
        }
    }

    public static InputStreamReader GetFileStream(String s) {
        return new InputStreamReader(FileTool.class.getResourceAsStream(s));
    }

    public static void ExtractFile(String InternalFile, File OutFile) throws IOException {
        BufferedReader reader = new BufferedReader(GetFileStream(InternalFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(OutFile));
        String Line;
        while ((Line = reader.readLine()) != null) {
            writer.write(Line + "\n");
        }
        writer.close();
    }

    public static void MergeSamFile(File[] InFile, File MergeFile) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(MergeFile));
        String line;
        BufferedReader gethead = new BufferedReader(new FileReader(InFile[0]));
        while ((line = gethead.readLine()) != null && line.matches("^@.*")) {
            out.write(line + "\n");
        }
        gethead.close();
        for (File file : InFile) {
            BufferedReader in = new BufferedReader(new FileReader(file));
            while ((line = in.readLine()) != null) {
                if (line.matches("^@.*")) {
                    continue;
                }
                out.write(line + "\n");
            }
            in.close();
        }
        out.close();
    }

    public static double[][] ReadMatrixFile(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line;
        ArrayList<double[]> List = new ArrayList<>();
        while ((line = in.readLine()) != null) {
            List.add(StringArrays.toDouble(line.split("\\s+")));
        }
        in.close();
        double[][] matrix = new double[List.size()][];
        for (int i = 0; i < List.size(); i++) {
            matrix[i] = new double[List.get(i).length];
            for (int j = 0; j < List.get(i).length; j++) {
                matrix[i][j] = List.get(i)[j];
            }
        }
        return matrix;
    }

    public static ArrayList<InterAction> ReadInterActionFile(BedpeFile file, int Name, int Count, int Fragment1, int Fragment2) throws IOException {
        String line;
        String[] str;
        ArrayList<InterAction> List = new ArrayList<>();
        BufferedReader in = new BufferedReader(new FileReader(file));
        if (file.BedpeDetect() == Opts.FileFormat.BedpePointFormat) {
            while ((line = in.readLine()) != null) {
                str = line.split("\\s+");
                InterAction inter = new InterAction(new ChrRegion(new String[]{str[0], str[1], str[1]}), new ChrRegion(new String[]{str[2], str[3], str[3]}));
                if (Name >= 0) {
                    try {
                        inter.Name = str[Name];
                    } catch (IndexOutOfBoundsException e) {
                        inter.Name = null;
                    }
                }
                if (Count >= 0) {
                    try {
                        inter.Count = Integer.parseInt(str[Count]);
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        inter.Count = 1;
                    }
                }
                if (Fragment1 >= 0 && Fragment2 >= 0) {
                    try {
                        inter.LeftFragment = Integer.parseInt(str[Fragment1]);
                        inter.RightFragment = Integer.parseInt(str[Fragment2]);
                    } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
                    }
                }
                List.add(inter);
            }
        } else if (file.BedpeDetect() == Opts.FileFormat.BedpeRegionFormat) {
            while ((line = in.readLine()) != null) {
                str = line.split("\\s+");
                InterAction inter = new InterAction(new ChrRegion(new String[]{str[0], str[1], str[2]}), new ChrRegion(new String[]{str[3], str[4], str[5]}));
                if (Name >= 0) {
                    try {
                        inter.Name = str[Name];
                    } catch (IndexOutOfBoundsException e) {
                        inter.Name = null;
                    }
                }
                if (Count >= 0) {
                    try {
                        inter.Count = Integer.parseInt(str[Count]);
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        inter.Count = 1;
                    }
                }
                if (Fragment1 >= 0 && Fragment2 >= 0) {
                    try {
                        inter.LeftFragment = Integer.parseInt(str[Fragment1]);
                        inter.RightFragment = Integer.parseInt(str[Fragment2]);
                    } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
                    }
                }
                List.add(inter);
            }
        } else {
            System.out.println("Error format");
            System.exit(1);
        }
        in.close();
        return List;
    }
}
