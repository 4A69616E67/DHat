package Component.File;

import Component.tool.Tools;
import Component.unit.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by snowf on 2019/2/17.
 */
public class BedpeFile extends AbstractFile<InterAction> {
    private Opts.FileFormat Format = Opts.FileFormat.BedpeRegionFormat;
    public boolean SortByName = false;

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

    protected InterAction ExtractItem(String[] s) {
        if (s != null) {
            String[] ss = s[0].split("\\s+");
            try {
                if (ss.length >= 11) {
                    Item = new InterAction(new ChrRegion(new String[]{ss[0], ss[1], ss[2], ss[9]}), new ChrRegion(new String[]{ss[3], ss[4], ss[5], ss[10]}));
                } else {
                    Item = new InterAction(new ChrRegion(new String[]{ss[0], ss[1], ss[2]}), new ChrRegion(new String[]{ss[3], ss[4], ss[5]}));
                }
                Item.getLeft().SortByName = SortByName;
                Item.getRight().SortByName = SortByName;
                Item.Name = ss[6];
                Item.SortByName = SortByName;
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        } else {
            Item = null;
        }
        return Item;
    }

    @Override
    public void WriteItem(InterAction item) throws IOException {
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
}
