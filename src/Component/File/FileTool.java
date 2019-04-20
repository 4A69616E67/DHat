package Component.File;

import Component.tool.Tools;
import Component.unit.*;

import java.io.*;
import java.util.*;

/**
 * Created by snowf on 2019/2/17.
 */
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

    public static Opts.FileFormat ReadsType(FastqFile fastqfile) throws IOException {
        int LineNumber = 100, i = 0, Count = 0;
        fastqfile.ReadOpen();
//        BufferedReader reader = new BufferedReader(new FileReader(fastqfile));
        FastqItem item;
        while ((item = fastqfile.ReadItem()) != null) {
            Count += item.Sequence.length();
            i++;
            if (i >= LineNumber) {
                break;
            }
        }
        fastqfile.ReadClose();
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

    public static void MergeSamFile(AbstractFile[] InFile, AbstractFile MergeFile) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(MergeFile));
        MergeFile.ItemNum = 0;
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
                MergeFile.ItemNum++;
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
            if (List.get(i).length >= 0) System.arraycopy(List.get(i), 0, matrix[i], 0, List.get(i).length);
        }
        return matrix;
    }

    public static String AdapterDetection(FastqFile file, File Prefix, int SubIndex, AbstractFile stat_file) throws IOException, InterruptedException {
        StringBuilder Adapter = new StringBuilder();
        int SeqNum = 100;
        FastaFile HeadFile = new FastaFile(Prefix + ".head" + SeqNum);
        file.ReadOpen();
        BufferedWriter writer = new BufferedWriter(new FileWriter(HeadFile));
        FastqItem fastq_item;
        int linenumber = 0;
        while ((fastq_item = file.ReadItem()) != null && ++linenumber <= SeqNum) {
            writer.write(fastq_item.Title.replace("@", ">") + "\n");
            writer.write(fastq_item.Sequence.substring(SubIndex) + "\n");
        }
        file.ReadClose();
        writer.close();
        FastaItem[] SplitAdapter = FindSimilarSequences(HeadFile, stat_file, 0.5f);
        int MaxValue = 0;
        for (FastaItem aSplitAdapter : SplitAdapter) {
            if (aSplitAdapter.Sequence.length() > MaxValue) {
                MaxValue = aSplitAdapter.Sequence.length();
                Adapter = aSplitAdapter.Sequence;
            }
        }
        return Adapter.toString();
    }

    public static LinkerSequence[] LinkersDetection(FastqFile input_file, File prefix, int length, AbstractFile stat_file) throws IOException, InterruptedException {
        ArrayList<FastqItem> ValidKmerList = GetValidKmer(input_file, 0, length, 1000, 0.05f);
        ArrayList<ArrayList<FastqItem>> assembly_list = Assembly(ValidKmerList);
        ArrayList<ArrayList<FastqItem>> final_assembly_list = new ArrayList<>(), temp_assembly_list = new ArrayList<>();
        for (int i = 0; i < assembly_list.size(); i++) {
            if (assembly_list.get(i).size() >= 3) {
                if (temp_assembly_list.size() > final_assembly_list.size()) {
                    final_assembly_list = temp_assembly_list;
                }
                temp_assembly_list = new ArrayList<>();
            } else {
                temp_assembly_list.add(assembly_list.get(i));
            }
        }
        if (temp_assembly_list.size() > final_assembly_list.size()) {
            final_assembly_list = temp_assembly_list;
        }

        BufferedWriter writer = stat_file.WriteOpen();
        int i = 1;
        for (FastqItem s : ValidKmerList) {
//            writer.write("@seq" + i + "\n");
            writer.write(s.Sequence + "\n");
//            writer.write("+\n");
//            writer.write(CleanMap.get(s)[0] + "\n");
            i++;
        }
        stat_file.WriteClose();

//        FastaItem[] SimilarSeqs = FindSimilarSequences(HeadFile, stat_file);

        return new LinkerSequence[0];
    }

    private static ArrayList<ArrayList<FastqItem>> Assembly(ArrayList<FastqItem> origin) {
        ArrayList<FastqItem> temp_list = new ArrayList<>(origin);
        ArrayList<ArrayList<FastqItem>> assembly_list = new ArrayList<>();
        ArrayList<ArrayList<FastqItem>> temp_assembly_list = AssemblyListInit(temp_list);
        FastqItem item;
        while (temp_list.size() > 0) {
            while (true) {
                ArrayList<FastqItem> start_list = temp_assembly_list.get(0);
                ArrayList<FastqItem> insert_list = AssemblySearch(start_list, temp_list, 0);
                if (insert_list.size() == 0) {
                    break;
                } else {
                    temp_assembly_list.add(0, insert_list);
                }
            }
            while (true) {
                ArrayList<FastqItem> end_list = temp_assembly_list.get(temp_assembly_list.size() - 1);
                ArrayList<FastqItem> insert_list = AssemblySearch(end_list, temp_list, 1);
                if (insert_list.size() == 0) {
                    break;
                } else {
                    temp_assembly_list.add(insert_list);
                }
            }
            if (temp_assembly_list.size() > assembly_list.size()) {
                assembly_list = temp_assembly_list;
            }
            if (temp_list.size() > 0) {
                temp_assembly_list = AssemblyListInit(temp_list);
            }
        }
        return assembly_list;
    }

    private static ArrayList<FastqItem> AssemblySearch(ArrayList<FastqItem> search_list, ArrayList<FastqItem> list, int type) {
        ArrayList<FastqItem> insert_list = new ArrayList<>();
        FastqItem item;
        for (FastqItem aSearch_list : search_list) {
            item = aSearch_list;
            String sub1;
            if (type == 0) {
                sub1 = item.Sequence.substring(0, item.Sequence.length() - 1);
            } else {
                sub1 = item.Sequence.substring(1);
            }
            for (int j = 0; j < list.size(); j++) {
                item = list.get(j);
                String sub2;
                if (type == 0) {
                    sub2 = item.Sequence.substring(1);
                } else {
                    sub2 = item.Sequence.substring(0, item.Sequence.length() - 1);
                }
                if (sub1.equals(sub2)) {
                    insert_list.add(item);
                    list.remove(j);
                    j--;
                }
            }
        }
        return insert_list;
    }

    private static ArrayList<ArrayList<FastqItem>> AssemblyListInit(ArrayList<FastqItem> list) {
        ArrayList<ArrayList<FastqItem>> assembly_list = new ArrayList<>();
        assembly_list.add(new ArrayList<>());
        FastqItem item = list.remove(0);
        assembly_list.get(0).add(item);
        String s = item.Sequence;
        int i = 0;
        while (list.size() > 0 && i < list.size()) {
            String[] sub1 = new String[]{s.substring(0, s.length() - 1), s.substring(1)};
            String[] sub2 = new String[]{list.get(i).Sequence.substring(0, list.get(i).Sequence.length() - 1), list.get(i).Sequence.substring(1)};
            if (sub1[0].equals(sub2[0]) || sub1[1].equals(sub2[1])) {
                assembly_list.get(0).add(list.get(i));
                list.remove(i);
            } else {
                i++;
            }
        }
        return assembly_list;
    }

    private static ArrayList<FastqItem> GetValidKmer(FastqFile input_file, int start_site, int end_site, int seq_num, float threshold) throws IOException {
        ArrayList<FastqItem> list = input_file.Extraction(seq_num);
        HashMap<String, int[]> KmerMap = new HashMap<>();
        HashMap<String, int[]> CleanMap = new HashMap<>();
        for (FastqItem item : list) {
            String[] kmer = Tools.GetKmer(item.Sequence.substring(start_site, end_site), 10);
            for (String s : kmer) {
                if (!KmerMap.containsKey(s)) {
                    KmerMap.put(s, new int[]{0});
                }
                KmerMap.get(s)[0]++;
            }
        }
        for (String s : KmerMap.keySet()) {
            if (KmerMap.get(s)[0] > threshold * seq_num) {
                CleanMap.put(s, KmerMap.get(s));
            }
        }
        KmerMap.clear();
        ArrayList<FastqItem> result = new ArrayList<>();
        int i = 0;
        for (String s : CleanMap.keySet()) {
            i++;
            result.add(new FastqItem(new String[]{"@seq" + i, s, "+", String.valueOf(CleanMap.get(s)[0])}));
        }
        return result;
    }

    private static FastaItem[] FindSimilarSequences(FastaFile file, AbstractFile stat_file, float threshold) throws IOException, InterruptedException {
        FastaFile MsaFile = new FastaFile(file.getPath() + ".msa");
        StringBuilder SimSeq = new StringBuilder();
        ArrayList<char[]> MsaStat = new ArrayList<>();
        ArrayList<float[]> BaseFreq = new ArrayList<>();
        int[] CountArrays = new int[255];
        FastaItem[] ResItems;
        //----------------------------------------------------------------------
        String ComLine = "mafft " + file.getPath();
        Opts.CommandOutFile.Append(ComLine + "\n");
        PrintWriter msa = new PrintWriter(MsaFile);
        if (Configure.DeBugLevel < 1) {
            Tools.ExecuteCommandStr(ComLine, msa, null);
        } else {
            Tools.ExecuteCommandStr(ComLine, msa, new PrintWriter(System.err));
        }
        msa.close();
        MsaFile.ReadOpen();
        FastaItem item;
        while ((item = MsaFile.ReadItem()) != null) {
            MsaStat.add(item.Sequence.toString().toCharArray());
        }
        int SeqNum = MsaStat.size();
        MsaFile.ReadClose();
        for (int i = 0; i < MsaStat.get(0).length; i++) {
            CountArrays['A'] = 0;
            CountArrays['T'] = 0;
            CountArrays['C'] = 0;
            CountArrays['G'] = 0;
            CountArrays['-'] = 0;
            for (char[] aMsaStat : MsaStat) {
                CountArrays[Character.toUpperCase(aMsaStat[i])]++;
            }
            int MaxValue = 0;
            char MaxBase = '-';
            BaseFreq.add(new float[255]);
            for (char base : new char[]{'A', 'T', 'C', 'G', '-'}) {
                BaseFreq.get(i)[base] = (float) CountArrays[base] / SeqNum;
                if (CountArrays[base] > MaxValue) {
                    MaxValue = CountArrays[base];
                    MaxBase = base;
                }
            }
            if (MaxValue > SeqNum * threshold) {
                SimSeq.append(MaxBase);
            } else {
                SimSeq.append('N');
            }
        }
        String[] SplitSeq = SimSeq.toString().replace("-", "").split("N+");
        ResItems = new FastaItem[SplitSeq.length];
        for (int i = 0; i < ResItems.length; i++) {
            ResItems[i] = new FastaItem(">seq" + i);
            ResItems[i].Sequence.append(SplitSeq[i]);
        }
        if (stat_file != null) {
            BufferedWriter writer = stat_file.WriteOpen();
            writer.write("Position\tA\tT\tC\tG\t-\n");
            for (int i = 0; i < BaseFreq.size(); i++) {
                writer.write(String.valueOf(i + 1));
                for (char base : new char[]{'A', 'T', 'C', 'G', '-'}) {
                    writer.write("\t" + String.format("%.2f", BaseFreq.get(i)[base]));
                }
                writer.write("\n");
            }
            stat_file.WriteClose();
        }
        return ResItems;
    }


}
