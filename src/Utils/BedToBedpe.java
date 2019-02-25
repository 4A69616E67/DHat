package Utils;


import Component.File.BedFile;
import Component.File.BedpeFile;

import java.io.IOException;

/**
 * Created by snowf on 2019/2/24.
 */

public class BedToBedpe {
    public static void main(String[] args) throws IOException {
//        new Component.tool.BedToBedpe(new BedFile(args[0]), new BedFile(args[1]), new BedpeFile(args[2]), 4, "");
        BedpeFile file = new BedpeFile(args[2]);
        file.BedToBedpe(new BedFile(args[0]), new BedFile(args[1]));
        System.out.println(file.getItemNum());
    }
}
