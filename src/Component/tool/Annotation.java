package Component.tool;

import Component.File.BedPeFile.BedpeFile;
import Component.File.GffFile.GffFile;

import java.io.IOException;
import java.util.Date;

/**
 * Created by snowf on 2019/5/5.
 */

public class Annotation {
    public static void main(String[] args) throws IOException {
        BedpeFile inFile = new BedpeFile(args[0]);
        GffFile gffFile = new GffFile(args[1]);
        BedpeFile outFile = new BedpeFile(args[2]);
        System.out.println(new Date() + "\tStart");
        inFile.Annotation(gffFile, outFile, 1);
        System.out.println(new Date() + "\tSuccess");
    }
}
