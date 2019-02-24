package Utils;

import Component.File.CommonFile;

/**
 * Created by snowf on 2019/2/23.
 */

public class CalculateLineNumber {
    public static void main(String[] args) {
        CommonFile file = new CommonFile(args[0]);
        if (args.length > 1) {
            file.setBufferSize(Integer.parseInt(args[1]));
        }
        System.out.println(file.getItemNum());
    }
}
