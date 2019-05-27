package Component.FragmentDigested;

import Component.File.FileTool;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by snowf on 2019/4/11.
 */

public class RestrictionEnzyme {

    //    public static final RestrictionEnzyme HindIII = new RestrictionEnzyme("HindIII", "A^AGCTT");
//    public static final RestrictionEnzyme MseI = new RestrictionEnzyme("MseI", "T^TAA");
//    public static final RestrictionEnzyme ClaI = new RestrictionEnzyme("ClaI", "AT^CGAT");
    public static final RestrictionEnzyme[] list = load("/Resource/EnzymeSite.txt");
    private static final String Delimiter = "^";

    private String Name;
    private String Sequence;
    private int CutSite;

    public RestrictionEnzyme(String name, String s) {
        Name = name;
        if (s.matches(".*[^a-z|A-Z].*")) {
            s = s.replaceAll("[^a-z|A-Z]+", Delimiter);
            CutSite = s.indexOf(Delimiter);
            Sequence = s.replaceAll("\\" + Delimiter, "");
        } else {
            Sequence = s;
            CutSite = 0;
        }
    }

    private static RestrictionEnzyme[] load(String f) {
        ArrayList<RestrictionEnzyme> list = new ArrayList<>();
        BufferedReader in = new BufferedReader(FileTool.GetFileStream(f));
        String line;
        try {
            while ((line = in.readLine()) != null) {
                String[] str = line.split("\\s+");
                list.add(new RestrictionEnzyme(str[0], str[1]));
            }
        } catch (IOException e) {
            System.err.println("Load " + f + " error!");
        }

        return list.toArray(new RestrictionEnzyme[0]);
    }

    public static void main(String[] args) {
        RestrictionEnzyme test1 = new RestrictionEnzyme("AA*GCTT");
        RestrictionEnzyme test2 = new RestrictionEnzyme("AA__GCTT");
    }

    public RestrictionEnzyme(String s) {
        boolean flag = false;
        for (RestrictionEnzyme aList : list) {
            if (s.compareToIgnoreCase(aList.getName()) == 0) {
                copy(aList);
                flag = true;
                break;
            }
        }
        if (!flag) {
            copy(new RestrictionEnzyme("", s));
        }
    }

    public void copy(RestrictionEnzyme r) {
        Name = r.Name;
        Sequence = r.Sequence;
        CutSite = r.CutSite;
    }

    public String getName() {
        return Name;
    }

    public String getSequence() {
        return Sequence;
    }

    public int getCutSite() {
        return CutSite;
    }

    @Override
    public String toString() {
        return Sequence.substring(0, CutSite) + Delimiter + Sequence.substring(CutSite);
    }

}
