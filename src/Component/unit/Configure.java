package Component.unit;


import Component.File.CommonFile.CommonFile;
import Component.File.FastQFile.FastqFile;
import Component.FragmentDigested.RestrictionEnzyme;
import Component.Sequence.DNASequence;
import Component.Software.Bwa;
import Component.Software.MAFFT;
import Component.Software.Python;
import Component.tool.LinkerDetection;
import Component.tool.Tools;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by snowf on 2019/2/17.
 */
public class Configure {

    public static File InterActionDistanceDisPng;
    public static File LinkerScoreDisPng;
    //-----------------------------------------------------------
    public static FastqFile InputFile;
    public static RestrictionEnzyme Restriction;
    public static String[] HalfLinker;
    public static File GenomeFile;
    //-----------------------------------------------------------
    public static File OutPath = new File("./");
    public static String Prefix = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    public static File Index;
    public static Chromosome[] Chromosome;
    public static String[] AdapterSeq;
    public static int[] Resolution = new int[]{1000000};
    public static int[] DrawResolution = new int[]{1000000};
    public static int Thread = 8;
    public static String Step = "-";
    //-----------------------------------------------------------------
    public static Component.Software.Python Python = new Python("python");
    public static Component.Software.Bwa Bwa = new Bwa("bwa");//
    public static MAFFT Mafft = new MAFFT("mafft");
    public static String Bowtie = "";//bowtie
    public static int MatchScore = 1;
    public static int MisMatchScore = -1;
    public static int InDelScore = -1;
    public static int MinLinkerLen;
    public static int MinReadsLen = 16;
    public static int MaxReadsLen = 20;
    public static int AlignThread = 1;
    public static String AlignType = "Short";
    public static int AlignMisMatch = 0;
    public static int MinUniqueScore = 20;
    public static boolean Iteration = false;
    public static int DeBugLevel = 0;

    public enum Require {
        InputFile(Configure.InputFile), GenomeFile(Configure.GenomeFile);
        public Object Value;

        Require(Object v) {
            this.Value = v;
        }


        @Override
        public String toString() {
            return this.name() + " = " + (Value == null ? "" : Value.toString());
        }
    }

    public enum Optional {
        OutPath(Configure.OutPath), Prefix(Configure.Prefix), Restriction(Configure.Restriction), HalfLinker(Configure.HalfLinker != null ? Tools.ArraysToString(Configure.HalfLinker) : null), Index(Configure.Index),/* EnzymeFragmentPath(Configure.EnzymeFragmentPath),*/ Chromosomes(Tools.ArraysToString(Configure.Chromosome)), AdapterSeq(Tools.ArraysToString(Configure.AdapterSeq)), Resolutions(Tools.ArraysToString(Configure.Resolution)), DrawResolutions(Tools.ArraysToString(Configure.DrawResolution)), Thread(Configure.Thread), Step(Configure.Step);
        public Object Value;

        Optional(Object v) {
            this.Value = v;
        }

        @Override
        public String toString() {
            return name() + " = " + (Value == null ? "" : Value.toString());
        }
    }

    public enum Advance {
        Python(Configure.Python), Bwa(Configure.Bwa), Mafft(Configure.Mafft), /*Bowtie("Bowtie", Configure.Bowtie),*/ MatchScore(Configure.MatchScore), MisMatchScore(Configure.MisMatchScore), InDelScore(Configure.InDelScore), MinLinkerLen(Configure.MinLinkerLen), MinReadsLength(Configure.MinReadsLen), MaxReadsLength(Configure.MaxReadsLen),/* AlignThread( Configure.AlignThread),*/ AlignType(Configure.AlignType), AlignMisMatch(Configure.AlignMisMatch), MinUniqueScore(Configure.MinUniqueScore), Iteration(Configure.Iteration), DeBugLevel(Configure.DeBugLevel);

        public Object Value;

        Advance(Object v) {
            this.Value = v;
        }

        @Override
        public String toString() {
            return name() + " = " + (Value == null ? "" : Value.toString());
        }
    }

    public static void GetOption(File ConfFile) throws IOException {
        Properties Config = new Properties();
        Config.load(new FileReader(ConfFile));
        for (Require r : Require.values()) {
            if (Config.getProperty(r.name()) != null && !Config.getProperty(r.name()).trim().equals(""))
                r.Value = Config.getProperty(r.name()).replaceAll("#.*", "").trim();
        }
        for (Optional o : Optional.values()) {
            if (Config.getProperty(o.name()) != null && !Config.getProperty(o.name()).trim().equals(""))
                o.Value = Config.getProperty(o.name()).replaceAll("#.*", "").trim();
        }
        for (Advance a : Advance.values()) {
            if (Config.getProperty(a.name()) != null && !Config.getProperty(a.name()).trim().equals(""))
                a.Value = Config.getProperty(a.name()).replaceAll("#.*", "").trim();
        }
    }

    public static boolean DependenceCheck() {
        boolean Satisfied = true;
        return Satisfied;
    }

    public static String ShowParameter() {
        Update();
        ArrayList<String> ParameterStr = new ArrayList<>();
        for (Require opt : Require.values()) {
            ParameterStr.add(opt.toString());
        }
        ParameterStr.add("======================================================================================");
        for (Optional opt : Optional.values()) {
            ParameterStr.add(opt.toString());
        }
        ParameterStr.add("======================================================================================");
        for (Advance opt : Advance.values()) {
            ParameterStr.add(opt.toString());
        }
        return String.join("\n", ParameterStr.toArray(new String[0]));
    }

    public static String ShowExecution() {
        StringBuilder s = new StringBuilder("Execution:\t");
        for (Opts.Step t : Opts.Step.values()) {
            s.append(t.toString()).append(" ");
        }
        return s.toString();
    }

    public static void SaveParameter(File file) throws IOException {
        Update();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (Require opt : Require.values()) {
            writer.write(opt + "\n");
        }
        writer.write("#======================================================================================\n");
        for (Optional opt : Optional.values()) {
            writer.write(opt + "\n");
        }
        writer.write("#======================================================================================\n");
        for (Advance opt : Advance.values()) {
            writer.write(opt + "\n");
        }
        writer.close();
    }

    public static void Update() {
        Require.InputFile.Value = InputFile;
        Optional.HalfLinker.Value = Tools.ArraysToString(HalfLinker);
        Optional.Restriction.Value = Restriction;
        Require.GenomeFile.Value = GenomeFile;
        //-------------------------------------------
        Optional.OutPath.Value = OutPath;
        Optional.Prefix.Value = Prefix;
        Optional.Index.Value = Index;
        Optional.Chromosomes.Value = Tools.ArraysToString(Chromosome);
        Optional.AdapterSeq.Value = Tools.ArraysToString(AdapterSeq);
        Optional.Resolutions.Value = Tools.ArraysToString(Resolution);
        Optional.DrawResolutions.Value = Tools.ArraysToString(DrawResolution);
        Optional.Thread.Value = Thread;
        Optional.Step.Value = Step;
        //----------------------------------------------
        Advance.Python.Value = Python;
        Advance.Bwa.Value = Bwa;
        Advance.Mafft.Value = Mafft;
        Advance.MatchScore.Value = MatchScore;
        Advance.MisMatchScore.Value = MisMatchScore;
        Advance.MinLinkerLen.Value = MinLinkerLen;
        Advance.MinReadsLength.Value = MinReadsLen;
        Advance.MaxReadsLength.Value = MaxReadsLen;
        Advance.InDelScore.Value = InDelScore;
        Advance.AlignType.Value = AlignType;
        Advance.AlignMisMatch.Value = AlignMisMatch;
        Advance.MinUniqueScore.Value = MinUniqueScore;
        Advance.Iteration.Value = Iteration;
        Advance.DeBugLevel.Value = DeBugLevel;
    }

    public static void Init() throws IOException {
        InputFile = Require.InputFile.Value != null ? new FastqFile(Require.InputFile.Value.toString().trim()) : null;
        Restriction = Optional.Restriction.Value != null ? new RestrictionEnzyme(Optional.Restriction.Value.toString().trim()) : null;
        HalfLinker = Optional.HalfLinker.Value != null ? Optional.HalfLinker.Value.toString().trim().split("\\s+") : null;
        if (HalfLinker == null) {
            ArrayList<DNASequence> linkers = LinkerDetection.run(InputFile, 0, 70, 5000, Restriction, 10, 0.05f);
            HashMap<String, Integer> map = new HashMap<>();
            for (int i = 0; i < linkers.size(); i++) {
                map.put(linkers.get(i).getSeq().substring(0, linkers.get(i).getSeq().length() / 2), 1);
            }
            HalfLinker = map.keySet().toArray(new String[0]);
            Optional.HalfLinker.Value = String.join(" ", HalfLinker);
        }
        if (Restriction == null) {
            Restriction = LinkerDetection.Enzyme;
            Optional.Restriction.Value = Restriction.toString();
        }
        GenomeFile = Require.GenomeFile.Value != null ? new File(Require.GenomeFile.Value.toString()) : null;
        //----------------------------------------------------------------------------------------------------
        OutPath = Optional.OutPath.Value != null ? new File(Optional.OutPath.Value.toString().trim()) : OutPath;
        Prefix = Optional.Prefix.Value != null ? Optional.Prefix.Value.toString().trim() : Prefix;
        Opts.StatisticFile = new CommonFile(Configure.OutPath + "/" + Prefix + ".Stat.txt");
        Opts.CommandOutFile = new CommonFile(Configure.OutPath + "/" + Prefix + ".command.log");
        Index = Optional.Index.Value != null ? new File(Optional.Index.Value.toString().trim()) : Index;
        if (Optional.Chromosomes.Value != null && !Optional.Chromosomes.Value.toString().trim().equals("")) {
            String[] str = Optional.Chromosomes.Value.toString().trim().split("\\s+");
            Chromosome = new Chromosome[str.length];
            for (int i = 0; i < Chromosome.length; i++) {
                Chromosome[i] = new Chromosome(str[i]);
            }
        }
        AdapterSeq = Optional.AdapterSeq.Value != null ? Optional.AdapterSeq.Value.toString().trim().split("\\s+") : AdapterSeq;
        Resolution = GetIntArray(Optional.Resolutions.Value, Resolution);
        DrawResolution = GetIntArray(Optional.DrawResolutions.Value, DrawResolution);
        Thread = GetIntItem(Optional.Thread.Value, Thread);
        Step = Optional.Step.Value != null ? Optional.Step.Value.toString() : Step;
        Opts.StepCheck(Step);
        Opts.Step.FindEnzymeFragment.Execute = true;
        //----------------------------------------------------------------------------------------------------
        MatchScore = GetIntItem(Advance.MatchScore.Value, MatchScore);
        MisMatchScore = GetIntItem(Advance.MisMatchScore.Value, MisMatchScore);
        InDelScore = GetIntItem(Advance.InDelScore.Value, InDelScore);
        MinLinkerLen = GetIntItem(Advance.MinLinkerLen.Value, MinLinkerLen);
        MinReadsLen = GetIntItem(Advance.MinReadsLength.Value, MinReadsLen);
        MaxReadsLen = GetIntItem(Advance.MaxReadsLength.Value, MaxReadsLen);
        AlignType = Advance.AlignType.Value != null ? Advance.AlignType.Value.toString().trim() : AlignType;
        AlignMisMatch = GetIntItem(Advance.AlignMisMatch.Value, AlignMisMatch);
        MinUniqueScore = GetIntItem(Advance.MinUniqueScore.Value, MinUniqueScore);
        Bwa = new Bwa(Advance.Bwa.Value.toString().split("\\s+")[0]);
        try {
            Python = new Python(Advance.Python.Value.toString().split("\\s+")[0]);
        } catch (Exception e) {
            System.err.println("Warning! System may can't execute python");
        }
        if (AdapterSeq != null && AdapterSeq[0].compareToIgnoreCase("auto") == 0) {
            Mafft = new MAFFT(Advance.Mafft.Value.toString().split("\\s+")[0]);
        }
        Iteration = Boolean.parseBoolean(Advance.Iteration.Value.toString());
        DeBugLevel = Integer.parseInt(Advance.DeBugLevel.Value.toString());
    }

    public static int GetIntItem(Object o, int d) {
        if (o != null) {
            try {
                int i = Integer.parseInt(o.toString().trim());
                if (i == 0) {
                    return d;
                } else {
                    return i;
                }
            } catch (NumberFormatException e) {
                return d;
            }
        }
        return d;
    }

    public static int[] GetIntArray(Object o, int[] d) {
        if (o != null) {
            try {
                int[] i = StringArrays.toInteger(o.toString().trim().split("\\s+"));
                if (i.length == 0) {
                    return d;
                } else {
                    return i;
                }
            } catch (NumberFormatException e) {
                return d;
            }
        }
        return d;
    }
}

