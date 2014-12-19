package TC;

public class AliceGameEasy {
    public long findMinimumValue(long x, long y) {
        long n = (long)Math.sqrt(1 + (x + y) * 8.0);
        if (n * n != 1 + (x + y) * 8 || (n - 1) % 2 != 0) return -1;
        n = (n - 1) / 2;
        long tmpx = x;
        long ret = 0;
        for (long i = n; i > 0; --i) {
            if (i > tmpx) continue;
            tmpx -= i;
            ++ret;
        }
        return ret;
    }
}
