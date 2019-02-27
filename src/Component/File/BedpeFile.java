package Component.File;

import Component.tool.Tools;
import Component.unit.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by snowf on 2019/2/17.
 */
public class BedpeFile extends AbstractFile<BedpeItem> {
    private Opts.FileFormat Format = Opts.FileFormat.BedpeRegionFormat;
    public BedItem.Sort SortBy = BedItem.Sort.Location;

    public static BedpeFile[] Copy(BedpeFile[] files) {
        BedpeFile[] NewFiles = new BedpeFile[files.length];
        for (int i = 0; i < files.length; i++) {
            NewFiles[i] = new BedpeFile(files[i]);
        }
        return NewFiles;
    }

    public BedpeFile(String pathname) {
        super(pathname);
    }

    public BedpeFile(File f) {
        super(f);
    }

    public BedpeFile(BedpeFile f) {
        super(f);
    }

    protected BedpeItem ExtractItem(String[] s) {
        if (s != null) {
            String[] ss = s[0].split("\\s+");
            Item = new BedpeItem(ss);
            Item.SortBy = SortBy;
        } else {
            Item = null;
        }
        return Item;
    }

    @Override
    protected SortItem<BedpeItem> ExtractSortItem(String[] s) {
        if (s == null) {
            return null;
        }
        String[] ls = s[0].split("\\s+");
        if (SortBy == BedItem.Sort.SeqTitle) {
            Item = new BedpeItem(ls[3], null, 0, null);
        } else {
            InterAction i = new InterAction(ls);
            Item = new BedpeItem(null, i, 0, null);
            if (ls.length > 9) {
                Item.getLocation().getLeft().Orientation = ls[8].charAt(0);
                Item.getLocation().getRight().Orientation = ls[9].charAt(0);
            }
        }
        return new SortItem<>(Item);
    }

    @Override
    public void WriteItem(BedpeItem item) throws IOException {
        writer.write(item.toString());
    }

    public void SplitSortFile(BedpeFile OutFile) throws IOException {
        int splitItemNum = 5000000;
        if (this.ItemNum == 0) {
            this.CalculateItemNumber();
        }
        if (this.ItemNum > splitItemNum) {
            splitItemNum = (int) Math.ceil(this.ItemNum / Math.ceil((double) this.ItemNum / splitItemNum));
            ArrayList<CommonFile> TempSplitFile = this.SplitFile(this.getPath(), splitItemNum);
            BedpeFile[] TempSplitSortFile = new BedpeFile[TempSplitFile.size()];
            for (int i = 0; i < TempSplitFile.size(); i++) {
                TempSplitSortFile[i] = new BedpeFile(TempSplitFile.get(i).getPath() + ".sort");
                new BedpeFile(TempSplitFile.get(i).getPath()).SortFile(TempSplitSortFile[i]);
            }
            OutFile.MergeSortFile(TempSplitSortFile);
            if (Configure.DeBugLevel < 1) {
                for (int i = 0; i < TempSplitFile.size(); i++) {
                    AbstractFile.delete(TempSplitFile.get(i));
                    AbstractFile.delete(TempSplitSortFile[i]);
                }
            }
        } else {
            this.SortFile(OutFile);
        }
    }

    public Opts.FileFormat BedpeDetect() throws IOException {//不再支持 BedpePointFormat
        ReadOpen();
        try {
            ReadItem();
        } catch (IndexOutOfBoundsException | NumberFormatException i) {
            Format = Opts.FileFormat.ErrorFormat;
        }
        if (Item == null) {
            Format = Opts.FileFormat.EmptyFile;
        }
        ReadClose();
        return Format;
    }

    public BedpeFile[] SeparateBedpe(Chromosome[] Chromosome, String Prefix, int Threads) throws IOException {
        System.out.println(new Date() + "\tSeparate Bedpe file\t" + getName());
        ReadOpen();
        BedpeFile[] ChrSameFile = new BedpeFile[Chromosome.length];
        //------------------------------------------------------------
        for (int i = 0; i < Chromosome.length; i++) {
            ChrSameFile[i] = new BedpeFile(Prefix + "." + Chromosome[i].Name + ".same.bedpe");
            ChrSameFile[i].WriteOpen();
        }
        Thread[] t = new Thread[Threads];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                String Line;
                String[] Str;
                try {
                    while ((Line = reader.readLine()) != null) {
                        Str = Line.split("\\s+");
                        for (int j = 0; j < Chromosome.length; j++) {
                            if (Str[0].equals(Chromosome[j].Name)) {
                                synchronized (Chromosome[j]) {
                                    ChrSameFile[j].getWriter().write(Line + "\n");
                                }
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        ReadClose();
        for (int i = 0; i < Chromosome.length; i++) {
            ChrSameFile[i].WriteClose();
        }
        System.out.println(new Date() + "\tEnd separate Bedpe file " + getName());
        return ChrSameFile;
    }

    public void BedToBedpe(BedFile file1, BedFile file2) throws IOException {
        file1.ReadOpen();
        file2.ReadOpen();
        WriteOpen();
        ItemNum = 0;
        BedItem item1 = file1.ReadItem();
        item1.SortBy = BedItem.Sort.SeqTitle;
        BedItem item2 = file2.ReadItem();
        while (item1 != null && item2 != null) {
            int res = item1.compareTo(item2);
            if (res == 0) {
                item1.SortBy = BedItem.Sort.Location;
                if (item1.compareTo(item2) > 0) {
                    WriteItemln(item2.ToBedpe(item1));
                } else {
                    WriteItemln(item1.ToBedpe(item2));
                }
                ItemNum++;
                item1 = file1.ReadItem();
                item2 = file2.ReadItem();
            } else if (res > 0) {
                item2 = file2.ReadItem();
            } else {
                item1 = file1.ReadItem();
                item1.SortBy = BedItem.Sort.SeqTitle;
            }
        }
        file1.ReadClose();
        file2.ReadClose();
        WriteClose();
    }
}
