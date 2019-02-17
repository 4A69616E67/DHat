package Component.tool;

import java.io.*;
import java.util.Date;

/**
 * Created by snowf on 2019/2/17.
 *
 */
public class BedpeToInter {
    public BedpeToInter(String BedpeFile, String OutFile) throws IOException {
        BufferedReader infile = new BufferedReader(new FileReader(BedpeFile));
        BufferedWriter outfile = new BufferedWriter(new FileWriter(OutFile));
        String line;
        String[] str;
        System.out.println(new Date() + "\tBed to interaction start\t" + BedpeFile);
        while ((line = infile.readLine()) != null) {
            str = line.split("\\s+");
            outfile.write(str[0] + "\t" + (Integer.parseInt(str[2]) + Integer.parseInt(str[1])) / 2 + "\t");
            outfile.write(str[3] + "\t" + (Integer.parseInt(str[5]) + Integer.parseInt(str[4])) / 2);
            for (int i = 6; i < str.length; i++) {
                outfile.write("\t" + str[i]);
            }
            outfile.write("\n");
        }
        outfile.close();
        System.out.println(new Date() + "\tBed to interaction end\t" + BedpeFile);
    }
}
