package TC;

public class QuadraticLaw {
    public long getTime(long d) {
        double l = 0, r = 1e9;
        while (cmp(l, r) < 0) {
            double mid = (l + r) / 2;
            double dur = mid * mid + mid;
            int ord = cmp(dur, d);
            if (ord <= 0) l = mid;
            else r = mid;
            //System.out.println(l);
        }
        long ans = (long) Math.ceil(l);
        if (ans * ans + ans > d) ans--;
        return ans;
    }

    private int cmp(double dur, double d) {
        if (Math.abs(dur - d) < 1e-6) return 0;
        if (dur > d) return 1;
        return -1;
    }
}
