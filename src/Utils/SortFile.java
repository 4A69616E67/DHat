package Utils;

import Component.File.CommonFile.CommonFile;
import Component.File.CommonFile.CommonItem;

import java.io.IOException;

/**
 * Created by snowf on 2019/2/24.
 */

public class SortFile {
    public static void main(String[] args) throws IOException {
        CommonFile file = new CommonFile(args[0]);
        file.SortFile(new CommonFile(file + ".sort"), new CommonItem.CommonComparator());
    }
}
