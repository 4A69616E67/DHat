package Component.File;

import Component.unit.MatrixItem;
import Component.unit.SortItem;

import java.io.IOException;

/**
 * Created by 浩 on 2019/2/1.
 */
public class MatrixFile extends AbstractFile<MatrixItem> {

    public MatrixFile(String pathname) {
        super(pathname);
    }

    @Override
    protected MatrixItem ExtractItem(String s) {
        if (s != null) {
            String[] ss = s.split("\\n+");
            Item = new MatrixItem(ss.length, ss.length);
            for (int i = 0; i < ss.length; i++) {
                String[] sss = ss[i].split("\\s+|,+");
                for (int j = 0; j < sss.length; j++) {
                    Item.setEntry(i, j, Double.parseDouble(sss[j]));
                }
            }
        } else {
            Item = null;
        }
        return Item;
    }

    @Override
    public synchronized String ReadItemLine() throws IOException {
        StringBuilder s = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            s.append(line.trim()).append("\n");
        }
        s.setLength(s.length() - 1);
        return s.toString();
    }

    @Override
    public void WriteItem(MatrixItem item) throws IOException {
        WriteItem(item, "\t");
    }

    public void WriteItem(MatrixItem item, String separator) throws IOException {
        for (int i = 0; i < Item.getRowDimension(); i++) {
            for (int j = 0; j < Item.getColumnDimension(); j++) {
                writer.write(String.valueOf(Item.getEntry(i, j)) + separator);
            }
            writer.write("\n");
        }
    }

    @Deprecated
    @Override
    public SortItem<MatrixItem> ReadSortItem() throws IOException {
        return null;
    }

}
