package Component.unit;

/**
 * Created by snowf on 2019/2/17.
 *
 */

public class Chromosome implements Comparable<Chromosome> {
    public String Name;
    public int Size;

    public Chromosome(String s) {
        this(s, 0);
    }

    public Chromosome(String[] s) {
        String chr;
        int size;
        try {
            chr = s[0];
        } catch (IndexOutOfBoundsException e) {
            chr = null;
        }
        try {
            size = Integer.parseInt(s[1]);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            size = 0;
        }
        Name = chr == null || chr.equals("") ? "?" : chr;
        Size = size;
    }

    public Chromosome(String name, int size) {
        Name = name == null || name.equals("") ? "?" : name;
        Size = size;
    }


    @Override
    public int compareTo( Chromosome o) {
        return Name.compareTo(o.Name);
    }

    public boolean equals(Chromosome b) {
        return this.Name.equals(b.Name) && this.Size == b.Size;
    }

    @Override
    public String toString() {
        return Name + ":" + Size;
    }
}
