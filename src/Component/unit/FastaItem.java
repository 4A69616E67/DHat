package Component.unit;

public class FastaItem implements Comparable<FastaItem> {
    public String Title;
    public StringBuilder Sequence = new StringBuilder();

    public FastaItem() {

    }

    @Override
    public int compareTo( FastaItem o) {
        return 0;
    }
}
