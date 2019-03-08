package Utils;

import Component.File.CommonFile;
import Component.File.FastqFile;
import Component.unit.FastqItem;

import java.io.IOException;
import java.util.HashSet;

/**
 * Created by snowf on 2019/3/4.
 */

public class FastqExtract {
    public static void main(String[] args) throws IOException {
        FastqFile inputfile = new FastqFile(args[0]);
        CommonFile listfile = new CommonFile(args[1]);
        int threads = 1;
        if (args.length >= 3) {
            threads = Integer.parseInt(args[2]);
        }
        HashSet<String> IDList = new HashSet<>();
        String line;
        listfile.ReadOpen();
        while ((line = listfile.ReadItem()) != null) {
            IDList.add(line);
        }
        listfile.ReadClose();
        for (FastqItem i : inputfile.ExtractID(IDList, threads)) {
            System.out.println(i);
        }
    }
}
