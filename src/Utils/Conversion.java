package Utils;

import Component.File.BedPeFile.BedpeFile;
import Component.File.BedPeFile.BedpeItem;
import Component.File.CommonFile.CommonFile;
import Component.unit.ChrRegion;

import java.io.IOException;
import java.util.Date;

/**
 * Created by snowf on 2019/7/1.
 */

/**
 * convert the bedpe file to the pre-file of .hic file, so that can convert it to .hic file by juicer box
 */
public class Conversion {
    public static void main(String[] args) throws IOException {
        Conversion.run(new BedpeFile(args[0]), new CommonFile(args[1]));
    }

    public static void run(BedpeFile inFile, CommonFile outFile) throws IOException {
        if (!inFile.isSorted()) {
            inFile.SplitSortFile(new BedpeFile(inFile + ".sort"), new BedpeItem.LocationComparator());
            inFile = new BedpeFile(inFile + ".sort");
        }
        inFile.ReadOpen();
        inFile.ItemNum = 0;
        outFile.WriteOpen();
        BedpeItem item;
        while ((item = inFile.ReadItem()) != null) {
            inFile.ItemNum++;
            ChrRegion Left = item.getLocation().getLeft();
            ChrRegion Right = item.getLocation().getRight();
            outFile.WriteItem(item.getSeqTitle() + "\t" + (Left.Orientation == '+' ? 0 : 1) + "\t" + Left.Chr + "\t" + Left.region.Center() + "\t" + item.Extends[0].replaceAll("-.*", "") + "\t" + (Right.Orientation == '+' ? 0 : 1) + "\t" + Right.Chr + "\t" + Right.region.Center() + "\t" + item.Extends[2].replaceAll("-.*", "") + "\t" + item.getScore() + "\t" + item.getScore() + "\n");
            if (inFile.ItemNum % 1000000 == 0) {
                System.out.println(new Date() + "\t" + inFile.ItemNum / 1000000 + " Million has been process");
            }
        }
        System.out.println("Total item: " + inFile.ItemNum);
        inFile.ReadClose();
        outFile.WriteClose();
    }
}
