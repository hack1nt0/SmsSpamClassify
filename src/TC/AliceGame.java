package TC;

public class AliceGame {
    public long findMinimumValue(long x, long y) {
        long n = (long)Math.sqrt(x + y);
        long bh = 2;
        for (long i = 3; i <= n; ++i) bh += i * 2 - 1;
        if (n * n != x + y || x == 2 || x == bh || y == 2 || y == bh) return -1;
        long ret = 0;
        for (long i = n; i > 0; --i) {
            if (x < i * 2 - 1) continue;
            long tx = x - (i * 2 - 1);
            if (tx == bh || tx == 2) continue;
            x = tx;
            ++ret;
        }
        return ret;
    }
}
