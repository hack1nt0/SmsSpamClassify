package TC;

import java.util.Arrays;

public class PotentialArithmeticSequence {
    public int numberOfSubsequences(int[] d) {
        int[] first50 = new int[100];
        first50[0] = 0;
        for (int pivot = 1, i = 1; i < first50.length;) {
            first50[i] = pivot;
            for (int j = 0; j < i && i + j + 1 < first50.length; ++j)
                first50[i + j + 1] = first50[j];
            i += i + 1;
            ++pivot;
        }
        int ret = 0;
        int maxp = 0;
        for (int i = 0; i < first50.length; ++i)
            maxp = first50[i] > first50[maxp] ? i : maxp;
        for (int l = 0; l < d.length; ++l)
            for (int r = l; r < d.length; ++r) {
                int p1 = l;
                for (int i = l; i <= r; ++i) p1 = d[i] > d[p1] ? i : p1;
                int p2 = 0;
                for (int i = 0; i < first50.length; ++i) if (first50[i] == d[p1]) {
                    p2 = i; break;
                }
                p2 = d[p1] > first50[maxp] ? maxp : p2;
                if (cmp(first50, p2, d, p1, l, r)) ret++;
            }
        return ret;
    }

    private boolean cmp(int[] first50, int p2, int[] d, int p1, int l, int r) {
        if (p1 - l > p2 || r - p1 > p2) return false;
        for (int t1 = p1 - 1, t2 = p2 - 1; l <= t1 && 0 <= t2; --t1, --t2) if (first50[t2] != d[t1])
            return false;
        for (int t1 = p1 + 1, t2 = 0; t1 <= r && t2 < p2; ++t1, ++t2) if (first50[t2] != d[t1])
            return false;
        return true;
    }
}
