package Component.FragmentDigested;

import Component.tool.Tools;
import Component.unit.Chromosome;
import Component.unit.Configure;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
/**
 * Created by snowf on 2019/2/17.
 *
 */
public class FindRestrictionSite {
    private File FastFile;
    private File OutPath;
    private String Restriction;
    private String Prefix;
    private File ChrSizeFile;
    private Chromosome[] Chromosomes;
    private File[] ChrFragmentFile;
    private int Threads = Configure.Thread;

    public FindRestrictionSite(File FastFile, File OutPath, String Restriction, String Prefix) {
        this.FastFile = FastFile;
        this.OutPath = OutPath;
        this.Restriction = Restriction;
        this.Prefix = Prefix;
        this.ChrSizeFile = new File(OutPath + "/" + Prefix + ".ChrSize");
        if (!OutPath.isDirectory() && !OutPath.mkdir()) {
            System.err.println(new Date() + "\tERROR! Can't Create " + OutPath);
            System.exit(1);
        }
    }

    public ArrayList<Chromosome> Run() throws IOException {
        BufferedReader fastfile = new BufferedReader(new FileReader(FastFile));
        BufferedWriter chrwrite;
        ArrayList<File> OutFiles = new ArrayList<>();
        ArrayList<String> list = new ArrayList<>();
        ArrayList<Chromosome> ChrSize = new ArrayList<>();
        StringBuilder Seq = new StringBuilder();
        String line;
        String Chr = "";
        //找到第一个以 ">" 开头的行
        while ((line = fastfile.readLine()) != null) {
            if (line.matches("^>.+")) {
                Chr = line.split("\\s+")[0].replace(">", "");
                break;
            }
        }
        while ((line = fastfile.readLine()) != null) {
            if (line.matches("^>.+")) {
                int Count = 0;
                int len = Seq.length();
                ChrSize.add(new Chromosome(Chr, len));
                list.add(Chr + "\t" + len);
                File OutFile = new File(OutPath + "/" + Prefix + "." + Chr + ".bed");
                OutFiles.add(OutFile);
                chrwrite = new BufferedWriter(new FileWriter(OutFile));
                ArrayList<int[]> list1 = CreateResSite(Seq, Restriction);
                for (int i = 0; i < list1.size(); i++) {
                    Count++;
                    int[] item = list1.get(i);
                    chrwrite.write(Count + "\t+\t" + Chr + "\t" + item[0] + "\t" + item[1] + "\n");
                }
                chrwrite.close();
                Seq.setLength(0);
                Chr = line.split("\\s+")[0].replace(">", "");
            } else {
                Seq.append(line);
            }
        }
        //========================================打印最后一条染色体=========================================
        int Count = 0;
        int len = Seq.length();
        ChrSize.add(new Chromosome(Chr, len));
        list.add(Chr + "\t" + len);
        File OutFile = new File(OutPath + "/" + Prefix + "." + Chr + ".bed");
        OutFiles.add(OutFile);
        chrwrite = new BufferedWriter(new FileWriter(OutFile));
        ChrSize.add(new Chromosome(Chr, len));
        ArrayList<int[]> list1 = CreateResSite(Seq, Restriction);
        for (int i = 0; i < list1.size(); i++) {
            Count++;
            int[] item = list1.get(i);
            chrwrite.write(Count + "\t+\t" + Chr + "\t" + item[0] + "\t" + item[1] + "\n");
        }
        chrwrite.close();
        Seq.setLength(0);
        ChrFragmentFile = OutFiles.toArray(new File[0]);
        Tools.PrintList(list, ChrSizeFile);//打印染色体大小信息
        return ChrSize;
    }

    private ArrayList<int[]> CreateResSite(StringBuilder seq, String res) {
        int index = res.indexOf("^");
        int[] item = new int[]{1, 0};
        String Res = res.replace("^", "");
        ArrayList<int[]> List = new ArrayList<>();
        List.add(item);
        for (int i = 0; i <= seq.length() - Res.length(); i++) {
            if (seq.substring(i, i + Res.length()).compareToIgnoreCase(Res) == 0) {
                item[1] = i + index;
                item = new int[]{i + index + 1, 0};
                List.add(item);
            }
        }
        item[1] = seq.length();
        return List;
    }

    public File[] getChrFragmentFile() {
        return ChrFragmentFile;
    }

    public File getChrSizeFile() {
        return ChrSizeFile;
    }
}
