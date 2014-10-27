package TC;

public class DivisorsPower {
    public long findArgument(long n) {
        long ans = Long.MAX_VALUE;
        for (int sqr = 1;; ++sqr) {
            long x = sqrt(n, sqr);
            if (x != -1 && d(x) == sqr) {
                ans = Math.min(x, ans);
            }
            if (sqr > Math.ceil(Math.log(n) / Math.log(2))) break;
        }
        ans = ans == Long.MAX_VALUE ? -1 : ans;
        return ans;
    }

    private long d(long x) {
        long MAXN = (long)Math.ceil(Math.sqrt(x));
        long ans = 1;
        for (long i = 2; i <= MAXN; ++i) {
            long tmp = 1;
            while (x > 1 && x % i == 0) {
                tmp++;
                x /= i;
            }
            ans *= tmp;
        }
        if (x > 1)
            ans *= 2;
        return ans;
    }

    private long sqrt(long n, int sqr) {
        long l = 0, r = n + 1;
        while (l < r) {
            long mid = l + (r - l) / 2;
            double v = pow((double)mid, sqr) - n;
            if (v == 0)
                return mid;
            if (v > 0)
                r = mid;
            else
                l = mid + 1;
        }
        return -1;
    }

    private double pow(double a, long b) {
        if (b == 0) return 1;
        double res = pow(a, b / 2);
        res *= res;
        if ((b & 1) == 1)
            res *= a;
        return res;
    }
}
