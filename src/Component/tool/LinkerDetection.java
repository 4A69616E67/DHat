package Component.tool;

import Component.File.FastqFile;
import Component.Sequence.DNASequence;
import Component.Sequence.KmerStructure;
import Component.Statistic.StatUtil;
import Component.unit.*;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created by snowf on 2019/2/17.
 */
public class LinkerDetection {
    public static void main(String[] args) throws IOException, ParseException {


        //==============================================================================================================
        Options Argument = new Options();
        Argument.addOption(Option.builder("i").hasArg().desc("input file").required().build());
        Argument.addOption(Option.builder("p").hasArg().desc("prefix").build());
        Argument.addOption(Option.builder("s").hasArg().desc("cutoff start index (default 0)").build());
        Argument.addOption(Option.builder("t").hasArg().desc("cutoff terminal index (default 70, if you want to remain full reads, please set a large number)").build());
        Argument.addOption(Option.builder("n").hasArg().desc("sequence number use to processing (default 5000)").build());
        Argument.addOption(Option.builder("e").hasArg().desc("restriction enzyme seq (example A^AGCTT or T^TAA)").build());
        Argument.addOption(Option.builder("k").hasArg().desc("k-mer length (default 10)").build());
        Argument.addOption(Option.builder("f").hasArg().desc("threshold (default 0.05)").build());
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getName() + " " + LinkerDetection.class.getName(), Argument, true);
            System.exit(1);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        FastqFile InPutFile = new FastqFile(Opts.GetFileOpt(ComLine, "i", null));
        String Prefix = Opts.GetStringOpt(ComLine, "p", Configure.Prefix);
        int Index1 = Opts.GetIntOpt(ComLine, "s", 0);
        int Index2 = Opts.GetIntOpt(ComLine, "t", 70);
        int SeqNum = Opts.GetIntOpt(ComLine, "n", 5000);
        int KmerLen = Opts.GetIntOpt(ComLine, "k", 10);
        float Threshold = Opts.GetFloatOpt(ComLine, "f", 0.05f);
        RestrictionEnzyme enzyme = Opts.GetStringOpt(ComLine, "e", null) == null ? null : new RestrictionEnzyme(Opts.GetStringOpt(ComLine, "e", null));
        //--------------------------------------------------------------------------------------------------------------
        ArrayList<DNASequence> result = run(InPutFile, new File(Prefix), Index1, Index2, SeqNum, enzyme, KmerLen, Threshold);
        for (DNASequence d : result) {
            System.out.println(d);
        }
    }

    public static ArrayList<DNASequence> run(FastqFile InPutFile, File prefix, int start, int end, int seqNum, RestrictionEnzyme enzyme, int k_merLen, float threshold) throws IOException {
        ArrayList<DNASequence> linkers = LinkerDetection.SimilarSeqDetection(InPutFile, new File("test"), start, end, seqNum, k_merLen, threshold);
        if (enzyme == null || enzyme.getSequence().equals("")) {
            boolean flag = false;
            //find out restriction enzyme
            int[] Count = new int[RestrictionEnzyme.list.length];
            for (int i = 0; i < linkers.size(); i++) {
                int minPosition = 1000;
                int minIndex = 0;
                flag = false;
                for (int j = 0; j < RestrictionEnzyme.list.length; j++) {
                    String subEnzyme1 = RestrictionEnzyme.list[j].getSequence().substring(0, Math.max(RestrictionEnzyme.list[j].getCutSite(), RestrictionEnzyme.list[j].getSequence().length() - RestrictionEnzyme.list[j].getCutSite()));
                    int position = linkers.get(i).getSeq().indexOf(subEnzyme1);
                    if (position >= 0 && position <= 3 && position < minPosition) {
                        minPosition = position;
                        minIndex = j;
                        flag = true;
                    }
                }
                if (flag) {
                    Count[minIndex]++;
                }
            }
            int maxIndex = StatUtil.maxIndex(Count);
            if (Count[maxIndex] >= linkers.size() / 2) {
                enzyme = RestrictionEnzyme.list[maxIndex];
            }
        }
        if (enzyme == null) {
            System.out.println("Unknown enzyme");
        } else {
            System.out.println(enzyme);
        }
        //修剪
        if (enzyme != null) {
            for (int i = 0; i < linkers.size(); i++) {
                String subEnzyme1 = enzyme.getSequence().substring(0, Math.max(enzyme.getCutSite(), enzyme.getSequence().length() - enzyme.getCutSite()));
                String subEnzyme2 = enzyme.getSequence().substring(Math.min(enzyme.getCutSite(), enzyme.getSequence().length() - enzyme.getCutSite()));
                int index1, index2;
                index1 = linkers.get(i).getSeq().indexOf(subEnzyme1);
                index2 = linkers.get(i).getSeq().lastIndexOf(subEnzyme2);
                if (index1 >= 0 && index2 >= 0 && index1 + subEnzyme1.length() < index2 && index1 <= 3 && linkers.get(i).getSeq().length() - index2 - subEnzyme2.length() <= 3) {
                    DNASequence s = new DNASequence(linkers.get(i).getSeq().substring(index1 + subEnzyme1.length(), index2), '+', linkers.get(i).Value);
                    if (s.get_reverse_complement().equals(s.getSeq())) {
                        linkers.set(i, s);
                    } else {
                        linkers.remove(i);
                        i--;
                    }
                } else {
                    linkers.remove(i);
                    i--;
                }
            }
        }
        //去重
        Hashtable<String, Double> final_linkers = new Hashtable<>();
        for (DNASequence d : linkers) {
            if (!final_linkers.contains(d.getSeq())) {
                final_linkers.put(d.getSeq(), d.Value);
            } else {
                final_linkers.put(d.getSeq(), final_linkers.get(d.getSeq()) + d.Value);
            }
        }
        linkers.clear();
        for (String s : final_linkers.keySet()) {
            linkers.add(new DNASequence(s, '+', final_linkers.get(s)));
        }
        return linkers;
    }


    public static ArrayList<DNASequence> SimilarSeqDetection(FastqFile input_file, File prefix, int start, int end, int SeqNum, int k_merLen, float threshold) throws IOException {
        start = start < 0 ? 0 : start;
        end = end < start ? start : end;
        SeqNum = SeqNum == 0 ? 5000 : SeqNum;
        k_merLen = k_merLen == 0 ? 10 : k_merLen;
        threshold = threshold == 0 ? 0.05f : threshold;
        ArrayList<FastqItem> list = input_file.Extraction(SeqNum);
        for (FastqItem item : list) {
            item.Sequence = item.Sequence.substring(start, Math.min(end, item.Sequence.length()));
        }
        ArrayList<KmerStructure> ValidKmerList = GetValidKmer(list, k_merLen, threshold * SeqNum);
        ArrayList<KmerStructure> assembly_list = Assembly(ValidKmerList);
        ArrayList<DNASequence> final_assembly_list = AssemblyShow(assembly_list);
        for (DNASequence d : final_assembly_list) {
            d.Value = d.Value / d.getSeq().length();
        }
        return final_assembly_list;
    }

    private static ArrayList<DNASequence> AssemblyShow(ArrayList<KmerStructure> input) {
        ArrayList<DNASequence> result = new ArrayList<>();
        if (input == null || input.size() == 0) {
            result.add(new DNASequence(""));
        } else {
            for (int i = 0; i < input.size(); i++) {
                if (input.get(i).Visited) {
                    result.add(new DNASequence(""));
                    continue;
                }
                input.get(i).Visited = true;
                ArrayList<DNASequence> next_seq = AssemblyShow(input.get(i).next);
                for (int j = 0; j < next_seq.size(); j++) {
                    DNASequence s = next_seq.get(j);
                    if (s.getSeq().length() == 0) {
                        result.add(new DNASequence(input.get(i).Seq.getSeq(), '+', input.get(i).Seq.Value));
                    } else {
                        result.add(new DNASequence(input.get(i).Seq.getSeq() + s.getSeq().substring(input.get(i).Seq.getSeq().length() - 1), '+', input.get(i).Seq.Value + s.Value));
                    }
                }
                input.get(i).Visited = false;
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
