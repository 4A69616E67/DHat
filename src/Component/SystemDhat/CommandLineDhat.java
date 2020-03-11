package Component.SystemDhat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Created by snowf on 2019/6/15.
 */

public class CommandLineDhat {
    private Process P;
    private Thread OutThread, ErrThread;
    private int ExitValue;

    /**
     * close the io stream when you redirect to a file
     */
    public int run(String CommandStr, PrintWriter Out, PrintWriter Error) throws IOException, InterruptedException {
        P = Runtime.getRuntime().exec(CommandStr);
        OutThread = new Thread(() -> {
            try {
                String line;
                BufferedReader bufferedReaderIn = new BufferedReader(new InputStreamReader(P.getInputStream()));
                if (Out != null) {
                    while ((line = bufferedReaderIn.readLine()) != null) {
                        Out.print(line + "\n");
                        Out.flush();
                    }
                    bufferedReaderIn.close();
                } else {
                    while (true) {
                        if (bufferedReaderIn.readLine() == null) break;
                    }
                    bufferedReaderIn.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        ErrThread = new Thread(() -> {
            try {
                String line;
                BufferedReader bufferedReaderIn = new BufferedReader(new InputStreamReader(P.getErrorStream()));
                if (Error != null) {
                    while ((line = bufferedReaderIn.readLine()) != null) {
                        Error.write(line + "\n");
                        Error.flush();
                    }
                    bufferedReaderIn.close();
                } else {
                    while (true) {
                        if (bufferedReaderIn.readLine() == null) break;
                    }
                    bufferedReaderIn.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        OutThread.start();
        ErrThread.start();
        OutThread.join();
        ErrThread.join();
        ExitValue = P.waitFor();
        return ExitValue;
    }

    /**
     * close the io stream when you redirect to a file
     */
    public int run(String CommandStr) throws IOException, InterruptedException {
        return run(CommandStr, null, null);
    }

    public void interrupt() {
        P.destroy();
    }
}
