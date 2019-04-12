package Component.Statistic;

import Component.unit.Opts;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by snowf on 2019/3/3.
 */

public class ResourceStat extends AbstractStat {
    private Thread StatThread;
    private float IntervalTime = 2;//second
    private SimpleDateFormat data_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void Stat() {
        StatThread.start();
    }

    @Override
    public String Show() {
        return null;
    }

    @Override
    protected void UpDate() {

    }

    @Override
    public void Init() {
        StatThread = new Thread(() -> {
            try {
                int i = 1;
                Opts.ResourceStatFile.Append("Time\tMemory(M)\tCPU\n");
                while (true) {
                    Thread.sleep((long) (IntervalTime * 1000));
                    Opts.ResourceStatFile.Append(data_format.format(new Date()) + "\t" + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / 1024 / 1024 + "\n");
                    i++;
                }
            } catch (InterruptedException | IOException ignored) {
            }
        });
    }

    public void Finish() {
        StatThread.interrupt();
    }
}
