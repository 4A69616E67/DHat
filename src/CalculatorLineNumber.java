import java.io.IOException;
import java.util.Date;

import Component.File.CommonFile;
/**
 * Created by snowf on 2019/2/17.
 *
 */
public class CalculatorLineNumber {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java -cp DLO-HIC-AnalysisTools CalculateItemNumber <File1 [File2] ...>");
            System.exit(0);
        }
        for (int i = 0; i < args.length; i++) {
            int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        long linenumber = new CommonFile(args[finalI]).CalculateItemNumber();
                        synchronized (Thread.class) {
                            System.out.println(new Date() + "\t" + args[finalI] + " line number is:\t" + linenumber);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
