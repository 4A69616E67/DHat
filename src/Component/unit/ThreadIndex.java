package Component.unit;

/**
 * Created by snowf on 2019/2/26.
 */

public class ThreadIndex {
    private int index;

    public ThreadIndex(int i) {
        index = i;
    }

    public synchronized int Index() {
        return index;
    }

    public synchronized int Add(int i) {
        index += i;
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
