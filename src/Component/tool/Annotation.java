package Component.tool;

import Component.File.BedPeFile.BedpeFile;
import Component.File.GffFile.GffFile;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Created by snowf on 2019/5/5.
 */

public class Annotation {
    public static void main(String[] args) throws IOException {
        if (args.length <= 3) {

        }
        BedpeFile inFile = new BedpeFile(args[0]);
        GffFile gffFile = new GffFile(args[1]);
        BedpeFile outFile = new BedpeFile(args[2]);
        int thread = 0;
        if (args.length >= 4) {
            try {
                thread = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                thread = 1;
            }
        }
        if (thread <= 0) {
            thread = 1;
        }
//        System.out.println(new Date() + "\tStart");
        HashMap<String, HashMap<String, long[]>> StatMAp = inFile.Annotation(gffFile, outFile, thread);
//        System.out.println(new Date() + "\tSuccess");
        System.out.print("Item");
        for (String s : StatMAp.keySet()) {
            System.out.print("\t" + s);
        }
        System.out.print("\n");
        for (String k1 : StatMAp.keySet()) {
            System.out.print(k1);
            for (String k2 : StatMAp.get(k1).keySet()) {
                System.out.print("\t" + new DecimalFormat("#,###").format(StatMAp.get(k1).get(k2)[0]));
            }
            System.out.print("\n");
        }
    }
}
