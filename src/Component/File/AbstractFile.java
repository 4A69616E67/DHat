package Component.File;

import Component.unit.SortItem;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;

/**
 * Created by snowf on 2019/2/17.
 */
public abstract class AbstractFile<E extends Comparable<E>> extends File {
    protected E Item;
    public long ItemNum;
    protected BufferedReader reader;
    protected BufferedWriter writer;

    AbstractFile(String pathname) {
        super(pathname);
    }

    AbstractFile(File file) {
        super(file.getPath());
    }

    AbstractFile(AbstractFile file) {
        super(file.getPath());
        ItemNum = file.ItemNum;
    }

    public long CalculateItemNumber() throws IOException {
        if (!isFile()) {
            return 0;
        }
        long ItemNumber = 0;
        ReadOpen();
        while (ReadItemLine() != null) {
            ItemNumber++;
        }
        ReadClose();
        ItemNum = ItemNumber;
        return ItemNum;
    }

    void ReadOpen() throws IOException {
        reader = new BufferedReader(new FileReader(this));
    }

    void ReadClose() throws IOException {
        reader.close();
    }

    BufferedWriter WriteOpen() throws IOException {
        return WriteOpen(false);
    }

    private BufferedWriter WriteOpen(boolean append) throws IOException {
        writer = new BufferedWriter(new FileWriter(this, append));
        return writer;
    }

    void WriteClose() throws IOException {
        writer.close();
    }

    protected abstract E ExtractItem(String s);

    public synchronized String ReadItemLine() throws IOException {
        return reader.readLine();
    }

    public E ReadItem() throws IOException {
        return ExtractItem(ReadItemLine());
    }

    public abstract void WriteItem(E item) throws IOException;

    public void WriteItemln(E item) throws IOException {
        WriteItem(item);
        writer.write("\n");
    }

    public BufferedReader getReader() {
        return reader;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public synchronized void Append(AbstractFile file) throws IOException {
        System.out.println(new Date() + "\tAppend " + file.getName() + " to " + getName());
        String item;
        file.ReadOpen();
        this.WriteOpen(true);
        while ((item = file.ReadItemLine()) != null) {
            this.getWriter().write(item + "\n");
            ItemNum++;
        }
        file.ReadClose();
        this.WriteClose();
        System.out.println(new Date() + "\tEnd append " + file.getName() + " to " + getName());
    }

    public synchronized void Append(String item) throws IOException {
        this.WriteOpen(true);
        this.getWriter().write(item);
        WriteClose();
        ItemNum++;
    }

    public void SortFile(AbstractFile OutFile) throws IOException {
        System.out.println(new Date() + "\tBegin to sort file " + getName());
        BufferedWriter outfile = OutFile.WriteOpen();
        ItemNum = 0;
        ReadOpen();
        SortItem<E> sortItem;
        ArrayList<SortItem<E>> SortList = new ArrayList<>();
        while ((sortItem = ReadSortItem()) != null) {
            SortList.add(sortItem);
            ItemNum++;
        }
        Collections.sort(SortList);
        for (SortItem aSortList : SortList) {
            outfile.write(aSortList.getLines());
            outfile.write("\n");
        }
        SortList.clear();
        outfile.close();
        ReadClose();
        System.out.println(new Date() + "\tEnd sort file " + getName());
    }

    public synchronized void MergeSortFile(AbstractFile<E>[] InFile) throws IOException {
        ItemNum = 0;
        System.out.print(new Date() + "\tMerge ");
        for (File s : InFile) {
            System.out.print(s.getName() + " ");
        }
        System.out.print("to " + getName() + "\n");
        //=========================================================================================
        LinkedList<SortItem<E>> SortList = new LinkedList<>();
        BufferedWriter writer = WriteOpen();
        if (InFile.length == 0) {
            return;
        }
        for (int i = 0; i < InFile.length; i++) {
            InFile[i].ReadOpen();
            SortItem<E> item = InFile[i].ReadSortItem();
            if (item != null) {
                item.serial = i;
                SortList.add(item);
            } else {
                InFile[i].ReadClose();
            }
        }
        Collections.sort(SortList);
        while (SortList.size() > 0) {
            SortItem<E> item = SortList.removeFirst();
            int serial = item.serial;
            writer.write(item.getLines());
            writer.write("\n");
            ItemNum++;
            item = InFile[serial].ReadSortItem();
            if (item == null) {
                continue;
            }
            item.serial = serial;
            Iterator<SortItem<E>> iterator = SortList.iterator();
            boolean flage = false;
            int i = 0;
            while (iterator.hasNext()) {
                SortItem<E> item1 = iterator.next();
                if (item.compareTo(item1) <= 0) {
                    SortList.add(i, item);
                    flage = true;
                    break;
                }
                i++;
            }
            if (!flage) {
                SortList.add(item);
            }
        }
        WriteClose();
        //============================================================================================
        System.out.print(new Date() + "\tEnd merge ");
        for (File s : InFile) {
            System.out.print(s.getName() + " ");
        }
        System.out.print("to " + getName() + "\n");
    }

    public synchronized void Merge(AbstractFile[] files) throws IOException {
        Merge(this, files);
    }

    public static void Merge(AbstractFile file, AbstractFile[] merge_files) throws IOException {
        BufferedWriter writer = file.WriteOpen();
        file.ItemNum = 0;
        String line;
        for (AbstractFile x : merge_files) {
            System.out.println(new Date() + "\tMerge " + x.getName() + " to " + file.getName());
            x.ReadOpen();
            while ((line = x.ReadItemLine()) != null) {
                writer.write(line + "\n");
                file.ItemNum++;
            }
            x.ReadClose();
        }
        file.WriteClose();
        System.out.println(new Date() + "\tDone merge");
    }

    public ArrayList<CommonFile> SplitFile(String Prefix, long itemNum) throws IOException {
        int filecount = 0;
        int count = 0;
        CommonFile TempFile;
        String line;
        ArrayList<CommonFile> Outfile = new ArrayList<>();
        this.ReadOpen();
        Outfile.add(TempFile = new CommonFile(Prefix + ".Split" + filecount));
        BufferedWriter outfile = TempFile.WriteOpen();
        while ((line = ReadItemLine()) != null) {
            count++;
            if (count > itemNum) {
                TempFile.ItemNum = itemNum;
                outfile.close();
                filecount++;
                Outfile.add(TempFile = new CommonFile(Prefix + ".Split" + filecount));
                outfile = TempFile.WriteOpen();
                count = 1;
            }
            outfile.write(line + "\n");
        }
        TempFile.ItemNum = count;
        outfile.close();
        this.ReadClose();
        return Outfile;
    }

    public E getItem() {
        return Item;
    }

    public boolean clean() {
        return clean(this);
    }

    public static boolean clean(File f) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            writer.close();
        } catch (IOException e) {
            System.err.println("Warning! can't clean " + f.getPath());
            return false;
        }
        return true;
    }

    public static void delete(File f) {
        if (f.exists() && !f.delete()) {
            System.err.println("Warning! can't delete " + f.getPath());
        }
    }

    public abstract SortItem<E> ReadSortItem() throws IOException;


}


