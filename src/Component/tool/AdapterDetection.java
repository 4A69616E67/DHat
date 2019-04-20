package Component.tool;

import Component.File.FastqFile;
import Component.unit.Configure;
import Component.unit.FastqItem;
import Component.unit.LinkerSequence;
import Component.unit.Opts;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by snowf on 2019/2/17.
 */
public class AdapterDetection {
    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("i").hasArg().desc("input file").required().build());
        Argument.addOption(Option.builder("p").hasArg().desc("prefix").build());
        Argument.addOption(Option.builder("c").hasArg().desc("cutoff position").build());
        Argument.addOption(Option.builder("q").hasArg(false).desc("quiet mode").build());
        Argument.addOption(Option.builder("n").hasArg(false).desc("sequence number use to processing").build());
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getName() + " " + AdapterDetection.class.getName(), Argument, true);
            System.exit(1);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        File InPutFile = Opts.GetFileOpt(ComLine, "i", null);
        String Prefix = Opts.GetStringOpt(ComLine, "p", Configure.Prefix);
        int SubIndex = Opts.GetIntOpt(ComLine, "c", 0);
        int SeqNum = Opts.GetIntOpt(ComLine, "n", 100);
    }

    public static LinkerSequence[] LinkersDetection(FastqFile input_file, File prefix, int length) throws IOException {
        int SeqNum = 4000;
        ArrayList<FastqItem> list = input_file.Extraction(SeqNum);
        for (FastqItem item : list) {
            item.Sequence = item.Sequence.substring(0, length);
        }
        ArrayList<FastqItem> ValidKmerList = GetValidKmer(list, 10, 0.05f * SeqNum);
        ArrayList<ArrayList<FastqItem>> assembly_list = Assembly(ValidKmerList);
        ArrayList<ArrayList<FastqItem>> final_assembly_list = new ArrayList<>(), temp_assembly_list = new ArrayList<>();
        for (int i = 0; i < assembly_list.size(); i++) {
            if (assembly_list.get(i).size() >= 3) {
                if (temp_assembly_list.size() > final_assembly_list.size()) {
                    final_assembly_list = temp_assembly_list;
                }
                temp_assembly_list = new ArrayList<>();
            } else {
                if (assembly_list.get(i).size() >= 2) {
                    int Q1 = Integer.parseInt(assembly_list.get(i).get(0).Quality);
                    int Q2 = Integer.parseInt(assembly_list.get(i).get(1).Quality);
                    if (Q1 * 3 < Q2) {
                        assembly_list.get(i).remove(0);
                    } else if (Q1 > Q2 * 3) {
                        assembly_list.get(i).remove(1);
                    }
                }
                temp_assembly_list.add(assembly_list.get(i));
            }
        }
        if (temp_assembly_list.size() > final_assembly_list.size()) {
            final_assembly_list = temp_assembly_list;
        }

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

    private static ArrayList<FastqItem> GetValidKmer(ArrayList<FastqItem> list, int k, float threshold) {
        HashMap<String, int[]> KmerMap = new HashMap<>();
        HashMap<String, int[]> CleanMap = new HashMap<>();
        for (FastqItem item : list) {
            String[] kmer = Tools.GetKmer(item.Sequence, k);
            for (String s : kmer) {
                if (!KmerMap.containsKey(s)) {
                    KmerMap.put(s, new int[]{0});
                }
                KmerMap.get(s)[0]++;
            }
        }
        for (String s : KmerMap.keySet()) {
            if (KmerMap.get(s)[0] > threshold) {
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
}
