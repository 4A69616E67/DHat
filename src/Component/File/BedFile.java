package Component.File;

import Component.unit.ChrRegion;
import Component.unit.Chromosome;
import Component.unit.Configure;
import Component.unit.SortItem;

import java.io.IOException;
import java.util.ArrayList;
/**
 * Created by snowf on 2019/2/17.
 *
 */
public class BedFile extends AbstractFile<ChrRegion> {
    public boolean SortByName = true;

    public BedFile(String pathname) {
        super(pathname);
    }

    public BedFile(BedFile file) {
        super(file);
    }

    @Override
    protected ChrRegion ExtractItem(String s) {
        if (s != null) {
            String[] ss = s.split("\\s+");
            Item = new ChrRegion(new Chromosome(ss[0]), Integer.parseInt(ss[1]), Integer.parseInt(ss[2]));
            if (ss.length >= 4) {
                Item.Name = ss[3];
            }
            if (ss.length >= 6) {
                Item.Orientation = ss[5].charAt(0);
            }
            Item.SortByName = SortByName;
        } else {
            Item = null;
        }
        return Item;
    }

    public void SplitSortFile(BedFile OutFile) throws IOException {
        int splitItemNum = 5000000;
        if (this.ItemNum == 0) {
            this.CalculateItemNumber();
        }
        if (this.ItemNum > splitItemNum) {
            splitItemNum = (int) Math.ceil(this.ItemNum / Math.ceil((double) this.ItemNum / splitItemNum));
            ArrayList<CommonFile> TempSplitFile = this.SplitFile(this.getPath(), splitItemNum);
            BedFile[] TempSplitSortFile = new BedFile[TempSplitFile.size()];
            for (int i = 0; i < TempSplitFile.size(); i++) {
                TempSplitSortFile[i] = new BedFile(TempSplitFile.get(i).getPath() + ".sort");
                new BedFile(TempSplitFile.get(i).getPath()).SortFile(TempSplitSortFile[i]);
            }
            OutFile.MergeSortFile(TempSplitSortFile);
            if (Configure.DeBugLevel < 1) {
                for (int i = 0; i < TempSplitFile.size(); i++) {
                    TempSplitFile.get(i).delete();
                    TempSplitSortFile[i].delete();
                }
            }
        } else {
            this.SortFile(OutFile);
        }
    }

    @Override
    public void WriteItem(ChrRegion item) throws IOException {
        writer.write(item.toString());
    }

    @Override
    public SortItem<ChrRegion> ReadSortItem() throws IOException {
        String line = ReadItemLine();
        Item = ExtractItem(line);
        if (Item == null) {
            return null;
        }
        return new SortItem<>(Item, line.toCharArray());
    }


}
