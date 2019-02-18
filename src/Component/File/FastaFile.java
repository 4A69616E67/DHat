package Component.File;

import Component.unit.FastaItem;
import Component.unit.SortItem;

import java.io.IOException;

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
    protected FastaItem ExtractItem(String s) {
        Item = new FastaItem();
        if (s == null) {
            Item = null;
        } else {
            String[] ss = s.split("\\n");
            if (ss.length <= 1) {
                return null;
            }
            Item.Title = ss[0];
            for (int i = 1; i < ss.length; i++) {
                Item.Sequence.append(ss[i]);
            }
        }
        return Item;
    }

    @Override
    public synchronized String ReadItemLine() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        StringBuilder s = new StringBuilder();
        s.append(line).append("\n");
        reader.mark(1000);
        line = reader.readLine();
        while (line != null && !line.matches("^>.*")) {
            s.append(line).append("\n");
            reader.mark(1000);
            line = reader.readLine();
        }
        s.deleteCharAt(s.length() - 1);
        reader.reset();
        return s.toString();
    }

    @Override
    public void WriteItem(FastaItem item) {

    }

    @Override
    public SortItem<FastaItem> ReadSortItem() {
        return null;
    }

}
