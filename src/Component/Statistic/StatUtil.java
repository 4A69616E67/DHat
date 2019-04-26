package Component.Statistic;

/**
 * Created by 浩 on 2019/3/2.
 */
public class StatUtil {
    private StatUtil() {
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

    public static int sum(int[] i) {
        int res = 0;
        if (i != null) {
            for (long aI : i) {
                res += aI;
            }
        }
        return res;
    }

    public static int maxValue(int[] i) {
        int max = Integer.MIN_VALUE;
        for (int a : i) {
            if (a > max) {
                max = a;
            }
        }
        return max;
    }

    public static int maxIndex(int[] i) {
        int max = Integer.MIN_VALUE;
        int maxIndex = 0;
        for (int j = 0; j < i.length; j++) {
            if (i[j] > max) {
                max = i[j];
                maxIndex = j;
            }
        }
        return maxIndex;
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
