package Component.unit;

public class ChrRegion implements Comparable<ChrRegion> {
    public String Name;
    public Chromosome Chr;
    public Region region;
    public char Orientation = '+';
    public boolean SortByName = true;

    public ChrRegion(String[] s) {
        Chr = new Chromosome(s[0]);
        int Begin = Integer.parseInt(s[1]);
        int Terminal = Integer.parseInt(s[2]);
        region = new Region(Begin, Terminal);
        if (s.length >= 4) {
            Orientation = s[3].charAt(0);
        }
    }

    public ChrRegion(Chromosome s, int left, int right) {
        Chr = s;
        region = new Region(left, right);
    }

    public ChrRegion(Chromosome s, int left, int right, char orientation) {
        this(s, left, right);
        Orientation = orientation;
    }

    public boolean IsOverlap(ChrRegion reg) {
        return this.Chr.Name.equals(reg.Chr.Name) && region.IsOverlap(reg.region);
    }

    public boolean IsBelong(ChrRegion reg) {
        return this.Chr.Name.equals(reg.Chr.Name) && region.IsBelong(reg.region);
    }

    public boolean IsContain(ChrRegion reg) {
        return this.Chr.Name.equals(reg.Chr.Name) && region.IsContain(reg.region);
    }

    @Override
    public int compareTo(ChrRegion o) {
        if (SortByName) {
            return this.Name.compareTo(o.Name);
        } else {
            if (this.Chr.equals(o.Chr)) {
                return region.compareTo(o.region);
            } else {
                return this.Chr.compareTo(o.Chr);
            }
        }
    }

    public String toString() {
        return Chr.Name + "\t" + region.Start + "\t" + region.End;
    }
}
