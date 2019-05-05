package Component.unit;

import Component.File.GffFile.GffItem;

import java.util.ArrayList;

/**
 * Created by snowf on 2019/5/4.
 */

public class Gene implements Comparable<Gene> {
    public String ID;
    public String Name;
    public ChrRegion GeneRegion;
    public ArrayList<Transcript> transcripts = new ArrayList<>();

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

    public static String GeneDistance(Gene g, ChrRegion c) {
        if (g.GeneRegion.IsOverlap(c)) {
            return "Gene:" + g.Name + ":" + g.GeneRegion.Orientation + ":0";
        } else {
            if (g.GeneRegion.compareTo(c) > 0) {
                if (g.GeneRegion.Orientation == '+') {
                    return "Intergenic:" + g.Name + ":+:" + (c.region.End - g.GeneRegion.region.Start);
                } else {
                    return "Intergenic:" + g.Name + ":-:" + (g.GeneRegion.region.Start - c.region.End);
                }
            } else {
                if (g.GeneRegion.Orientation == '+') {
                    return "Intergenic:" + g.Name + ":+:" + (c.region.Start - g.GeneRegion.region.End);
                } else {
                    return "Intergenic:" + g.Name + ":-:" + (g.GeneRegion.region.End - c.region.Start);
                }
            }
        }
    }
}
