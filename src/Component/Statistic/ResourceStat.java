package Component.Statistic;

/**
 * Created by snowf on 2019/3/3.
 */

public class ResourceStat extends AbstractStat {
    private Thread StatThread;
    private float IntervalTime = 1;//second

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
                while (true) {
                    Thread.sleep((long) (IntervalTime * 1000));
                }
            } catch (InterruptedException ignored) {
            }
        });
    }

    public void Finish() {
        StatThread.interrupt();
    }
}
