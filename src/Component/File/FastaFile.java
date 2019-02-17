package Component.File;

import Component.unit.FastaItem;
import Component.unit.SortItem;

import java.io.IOException;
/**
 * Created by snowf on 2019/2/17.
 *
 */
public class FastaFile extends AbstractFile<FastaItem> {
    public FastaFile(String pathname) {
        super(pathname);
    }

    public FastaFile(FastaFile file){
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

//    @Override
//    public FastaItem ReadItem() throws IOException {
//        Item = new FastaItem();
//        String s;
//        while ((s = reader.readLine()) != null) {
//            if (s.matches("^>.*")) {
//                Item.Title = s;
//                break;
//            }
//        }
//        while (true) {
//            s = reader.readLine();
//            if (s == null) {
//                Item = null;
//                break;
//            }
//            reader.mark(100);
//            if (s.matches("^>.*")) {
//                reader.reset();
//                break;
//            } else {
//                Item.Sequence.append(s);
//            }
//        }
//        return Item;
//    }

    @Override
    public void WriteItem(FastaItem item) throws IOException {

    }

    @Override
    public SortItem<FastaItem> ReadSortItem() throws IOException {
        return null;
    }

}
