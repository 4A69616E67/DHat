package Component.Statistic;

import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.MathUtils;

/**
 * Created by 浩 on 2019/3/2.
 */
public class Stat {
    private Stat() {
    }

    public static long sum(long[] l) {
        long res = 0;
        if (l != null) {
            for (long aL : l) {
                res += aL;
            }
        }
        return res;
    }

    public static int max(int[] i) {
        int max = Integer.MIN_VALUE;
        for (int a : i) {
            if (a > max) {
                max = a;
            }
        }
        return max;
    }

    public static int min(int[] i) {
        int min = Integer.MAX_VALUE;
        for (int a : i) {
            if (a < min) {
                min = a;
            }
        }
        return min;
    }
}
