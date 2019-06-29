package Component.FragmentDigested;

import Component.File.BedFile.BedFile;
import Component.File.BedFile.BedItem;
import Component.File.BedPeFile.BedpeItem;
import Component.File.CommonFile;
import Component.File.FastaFile.FastaFile;
import Component.File.FastaFile.FastaItem;
import Component.tool.Tools;
import Component.unit.ChrRegion;
import Component.unit.Chromosome;
import Component.unit.ThreadIndex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by snowf on 2019/5/27.
 */

public class FragmentDigested {
    private File OutDir;
    private Chromosome[] Chrs;
    private RestrictionEnzyme Enzyme;
    private String Prefix;
    private BedFile[] ChrsFragmentFile;
    private BedFile AllChrsFragmentFile;
    private CommonFile ChrSizeFile;
    public int Threads = 1;

    public FragmentDigested(File outDir, Chromosome[] chrs, RestrictionEnzyme enzyme, String prefix) {
        OutDir = outDir;
        Chrs = chrs;
        Enzyme = enzyme;
        Prefix = prefix;
        CreateFile();
    }

    public void run(FastaFile genomeFile) throws IOException {
        System.out.println(new Date() + "\tCreate restriction fragment");
        if (!OutDir.isDirectory() && !OutDir.mkdir()) {
            System.err.println(new Date() + "\tCreate " + OutDir + " false !");
        }
        ArrayList<FastaItem> GenomeList = new ArrayList<>();
        //--------------------------------------------------
        genomeFile.ReadOpen();
        FastaItem item;
        while ((item = genomeFile.ReadItem()) != null) {
            int chrIndex = ContainChromosome(item.Title);
            if (chrIndex != -1) {
                GenomeList.add(item);
                Chrs[chrIndex].Size = item.Sequence.length();
            }
        }
        genomeFile.ReadClose();
        //------------------------------------------------
        Thread[] t = new Thread[Threads];
        ThreadIndex Index = new ThreadIndex(-1);
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                while (true) {
                    int index = Index.Add(1);
                    if (index >= GenomeList.size()) {
                        break;
                    }
                    int chrIndex = ContainChromosome(GenomeList.get(index).Title);
                    if (chrIndex != -1) {
                        ArrayList<BedItem> FragmentList = FindFragment(GenomeList.get(index), Enzyme);
                        try {
                            ChrsFragmentFile[chrIndex].WriteOpen();
                            for (BedItem frag : FragmentList) {
                                ChrsFragmentFile[chrIndex].WriteItemln(frag);
                            }
                            ChrsFragmentFile[chrIndex].WriteClose();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        //--------------------------------------------------
        BufferedWriter writer = ChrSizeFile.WriteOpen();
        for (int i = 0; i < ChrsFragmentFile.length; i++) {
            if (!ChrsFragmentFile[i].exists()) {
                System.err.println(new Date() + "\t[FindRestrictionFragment]\tWarning! No " + Chrs[i].Name + " in genomic file");
                ChrsFragmentFile[i].WriteOpen();
                ChrsFragmentFile[i].WriteClose();
            }
            writer.write(Chrs[i].Name + "\t" + Chrs[i].Size + "\n");
        }
        ChrSizeFile.WriteClose();
        AllChrsFragmentFile.Merge(ChrsFragmentFile);
        System.out.println(new Date() + "\tCreate restriction fragment finished");
        //-------------------
    }

    private void CreateFile() {
        ChrsFragmentFile = new BedFile[Chrs.length];
        for (int i = 0; i < ChrsFragmentFile.length; i++) {
            ChrsFragmentFile[i] = new BedFile(OutDir + "/" + Prefix + "." + Enzyme.getSequence() + "." + Chrs[i].Name + ".bed");
        }
        AllChrsFragmentFile = new BedFile(OutDir + "/" + Prefix + "." + Enzyme.getSequence() + ".all.bed");
        ChrSizeFile = new CommonFile(OutDir + "/" + Prefix + "." + Enzyme.getSequence() + ".ChrSize.bed");
    }

    public BedFile[] getChrsFragmentFile() {
        CreateFile();
        return ChrsFragmentFile;
    }

    public BedFile getAllChrsFragmentFile() {
        CreateFile();
        return AllChrsFragmentFile;
    }

    public Chromosome[] getChromosomes() {
        return Chrs;
    }

    public CommonFile getChrSizeFile() {
        return ChrSizeFile;
    }

    private int ContainChromosome(String s) {
        for (int i = 0; i < Chrs.length; i++) {
            if (s.equals(Chrs[i].Name)) {
                return i;
            }
        }
        return -1;
    }

    private ArrayList<BedItem> FindFragment(FastaItem refSeq, RestrictionEnzyme enzymeSeq) {
        String EnzySeq = enzymeSeq.getSequence();
        if (EnzySeq.length() < 1) {
            System.err.println("Null enzyme sequence!");
            System.exit(1);
        }
        Chromosome Chr = new Chromosome(refSeq.Title, refSeq.Sequence.length());
        ArrayList<BedItem> List = new ArrayList<>();
        int Count = 1;
        List.add(new BedItem("fragment" + Count, new ChrRegion(Chr.Name, 1, 0), Count, new String[0]));
        for (int i = 0; i < Chr.Size - EnzySeq.length() + 1; i++) {
            if (refSeq.Sequence.substring(i, i + EnzySeq.length()).compareToIgnoreCase(EnzySeq) == 0) {
                int EndIndex = i + enzymeSeq.getCutSite();
                if (EndIndex != 0) {
                    List.get(List.size() - 1).getLocation().region.End = EndIndex;
                }
                if (EndIndex < Chr.Size) {
                    Count++;
                    List.add(new BedItem("fragment" + Count, new ChrRegion(Chr.Name, EndIndex + 1, 0), Count, new String[0]));
                }
            }
        }
        if (List.get(List.size() - 1).getLocation().region.End == 0) {
            List.get(List.size() - 1).getLocation().region.End = Chr.Size;
        }
        return List;
    }
}
