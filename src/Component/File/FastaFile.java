package Component.File;

import Component.unit.FastaItem;
import Component.unit.SortItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by snowf on 2019/2/17.
 */
public class FastaFile extends AbstractFile<FastaItem> {
    public FastaFile(String pathname) {
        super(pathname);
    }

    public FastaFile(FastaFile file) {
        super(file);
    }

    @Override
    protected FastaItem ExtractItem(String[] s) {
        Item = new FastaItem();
        if (s == null || s.length <= 1) {
            Item = null;
        } else {
            Item.Title = s[0];
            for (int i = 1; i < s.length; i++) {
                Item.Sequence.append(s[i]);
            }
        }
        return Item;
    }

    @Override
    public synchronized String[] ReadItemLine() throws IOException {
        ArrayList<String> seq = new ArrayList<>();
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        seq.add(line);
        reader.mark(1000);
        line = reader.readLine();
        while (line != null && !line.matches("^>.*")) {
            seq.add(line);
            reader.mark(1000);
            line = reader.readLine();
        }
        reader.reset();
        return seq.toArray(new String[0]);
    }

    @Override
    public void WriteItem(FastaItem item) {

    }

    @Override
    public SortItem<FastaItem> ReadSortItem() {
        return null;
    }

}
