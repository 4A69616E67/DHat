package Component.tool;

import Component.File.FastqFile;
import Component.Sequence.DNASequence;
import Component.Sequence.KmerStructure;
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
        int SeqNum = 5000;
        ArrayList<FastqItem> list = input_file.Extraction(SeqNum);
        for (FastqItem item : list) {
//            item.Sequence = item.Sequence.substring(0, length);
            item.Sequence = item.Sequence.substring(length);
        }
        ArrayList<KmerStructure> ValidKmerList = GetValidKmer(list, 10, 0.1f * SeqNum);
        ArrayList<KmerStructure> assembly_list = Assembly(ValidKmerList);
        ArrayList<DNASequence> final_assembly_list = AssemblyShow(assembly_list);

        return new LinkerSequence[0];
    }

    private static ArrayList<DNASequence> AssemblyShow(ArrayList<KmerStructure> input) {
        ArrayList<DNASequence> result = new ArrayList<>();
        if (input == null || input.size() == 0) {
            result.add(new DNASequence(""));
        } else {
            for (int i = 0; i < input.size(); i++) {
                ArrayList<DNASequence> next_seq = AssemblyShow(input.get(i).next);
                for (int j = 0; j < next_seq.size(); j++) {
                    DNASequence s = next_seq.get(j);
                    if (s.getSeq().length() == 0) {
                        result.add(new DNASequence(input.get(i).Seq.getSeq(), '+', input.get(i).Seq.Value));
                    } else {
                        result.add(new DNASequence(input.get(i).Seq.getSeq() + s.getSeq().substring(input.get(i).Seq.getSeq().length() - 1), '+', input.get(i).Seq.Value + s.Value));
                    }
                }
            }
        }
        return result;
    }

    private static ArrayList<KmerStructure> Assembly(ArrayList<KmerStructure> origin) {
        ArrayList<KmerStructure> temp_list = new ArrayList<>(origin);
        ArrayList<KmerStructure> assembly_list = new ArrayList<>();
        ArrayList<String[]> subList = new ArrayList<>();
        for (int i = 0; i < temp_list.size(); i++) {
            String s = temp_list.get(i).Seq.getSeq();
            subList.add(new String[]{s.substring(0, s.length() - 1), s.substring(1)});
        }
        for (int i = 0; i < temp_list.size(); i++) {
            String[] sub1 = subList.get(i);
            for (int j = i; j < temp_list.size(); j++) {
                String[] sub2 = subList.get(j);
                if (sub1[0].equals(sub2[1])) {
                    temp_list.get(i).last.add(temp_list.get(j));
                    temp_list.get(j).next.add(temp_list.get(i));
                } else if (sub1[1].equals(sub2[0])) {
                    temp_list.get(i).next.add(temp_list.get(j));
                    temp_list.get(j).last.add(temp_list.get(i));
                }
            }
        }
        for (int i = 0; i < temp_list.size(); i++) {
            if (temp_list.get(i).last.size() == 0) {
                assembly_list.add(temp_list.get(i));
            }
        }
        return assembly_list;
    }

    private static ArrayList<KmerStructure> GetValidKmer(ArrayList<FastqItem> list, int k, float threshold) {
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
        ArrayList<KmerStructure> result = new ArrayList<>();
        int i = 0;
        for (String s : CleanMap.keySet()) {
            i++;
            result.add(new KmerStructure(new DNASequence(s, '+', CleanMap.get(s)[0])));
        }
        return result;
    }
}
