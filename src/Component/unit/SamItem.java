package Component.unit;

public class SamItem implements Comparable<SamItem> {
    public String Title;
    public int Stat;
    public String Chr;
    public int BeginSite;
    public int MappingQuality;
    public String MappingStat;
    public String Sequence;
    public String Quality;
    public String ExtendCol;
    public boolean SortByName = true;

    public SamItem() {

    }

    @Override
    public int compareTo( SamItem o) {
        if (SortByName) {
            return Title.compareTo(o.Title);
        } else {
            int result = Chr.compareTo(o.Chr);
            if (result == 0) {
                return BeginSite - o.BeginSite;
            } else {
                return result;
            }
        }
    }
}
