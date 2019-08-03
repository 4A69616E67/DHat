package Utils;

import java.util.Date;

import Component.File.CommonFile;
import Component.tool.Tools;
import Component.unit.Opts;

/**
 * Created by snowf on 2019/2/17.
 */
public class CalculateLineNumber {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -cp " + Opts.JarFile + " " + CalculateLineNumber.class.getName() + " <File1 [File2] ...>");
            System.exit(1);
        }
        Thread[] t = new Thread[args.length];
        for (int i = 0; i < args.length; i++) {
            int finalI = i;
            t[i] = new Thread(() -> {
                long linenumber = new CommonFile(args[finalI]).getItemNum();
                synchronized (Thread.class) {
                    System.out.println(new Date() + "\t" + args[finalI] + " line number is:\t" + linenumber);
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
    }
}
