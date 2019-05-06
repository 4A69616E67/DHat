package Component.unit;

import Component.File.GffFile.GffItem;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by snowf on 2019/5/4.
 */

public class Gene implements Comparable<Gene> {
    public String ID;
    public String Name;
    public ChrRegion GeneRegion;
    public ArrayList<Transcript> transcripts = new ArrayList<>();

    public static final String GENE = "Gene";
    public static final String PROMOTER = "Promoter";
    public static final String INTERGENIC = "Intergenic";

    public static HashMap<String, HashMap<String, long[]>> CreateAnnotationStat() {
        HashMap<String, HashMap<String, long[]>> map = new HashMap<>();
        String[] list = new String[]{GENE, PROMOTER, INTERGENIC, "-"};
        for (String k1 : list) {
            map.put(k1, new HashMap<>());
            for (String k2 : list) {
                map.get(k1).put(k2, new long[]{0});
            }
        }
        return map;
    }

    public Gene(GffItem g) {
        GeneRegion = new ChrRegion(g.Columns[0], Integer.parseInt(g.Columns[3]), Integer.parseInt(g.Columns[4]), g.Columns[6].charAt(0));
        ID = g.map.get("ID");
        Name = g.map.get("Name");
    }

    public Gene(String s) {
        this(new GffItem(s));
    }

    @Override
    public int compareTo(Gene o) {
        return GeneRegion.compareTo(o.GeneRegion);
    }

    public static String[] GeneDistance(Gene g, ChrRegion c) {
        int dis;
        String[] res = new String[4];
        res[1] = g.Name;
        res[2] = String.valueOf(g.GeneRegion.Orientation);
        if (g.GeneRegion.IsOverlap(c)) {
            res[0] = GENE;
            res[3] = "0";
            return res;
        } else {
            if (g.GeneRegion.compareTo(c) > 0) {
                if (g.GeneRegion.Orientation == '+') {
                    dis = c.region.End - g.GeneRegion.region.Start;
                } else {
                    dis = g.GeneRegion.region.Start - c.region.End;
                }
            } else {
                if (g.GeneRegion.Orientation == '+') {
                    dis = c.region.Start - g.GeneRegion.region.End;
                } else {
                    dis = g.GeneRegion.region.End - c.region.Start;
                }
            }
            res[3] = String.valueOf(dis);
            if (dis <= 0 && dis >= -10000) {
                res[0] = PROMOTER;
            } else {
                res[0] = INTERGENIC;
            }
        }
        return res;
    }
}
