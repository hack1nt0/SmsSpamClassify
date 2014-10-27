package TC;

import java.util.Arrays;

public class PeriodicJumping { //WA
    public int minimalTime(int x, int[] jumpLengths) {
        x = Math.abs(x);
        int totStepLen = 0;
        int N = jumpLengths.length;
        for (int n: jumpLengths) totStepLen += n;
        int ret = 0;
        if (x / totStepLen > 1) {
            ret += x / totStepLen - 1;
            x = totStepLen + x % totStepLen;
        }
        if (x == 0)
            return ret;
        for (int i = 1; i <= N * 2; ++i)
            for (int j = 0; j <= i; ++j)
                for (int k = j; k <= i; ++k)
                    if (isValidTri(i, j, k, jumpLengths, x))
                        return ret += i;
        return -1;
    }

    private boolean isValidTri(int len, int j, int k, int[] jumpLengths, int c) {
        long dc = 0, a = 0, b = 0;
        for (int i = 0; i < j; ++i) dc += jumpLengths[i % jumpLengths.length];
        for (int i = j; i < k; ++i) a += jumpLengths[i % jumpLengths.length];
        for (int i = k; i < len; ++i) b += jumpLengths[i % jumpLengths.length];
        long tri[] = {c + dc, a, b};
        Arrays.sort(tri);
        if (tri[0] + tri[1] >= tri[2]) return true;
        tri = new long[] {c - dc, a, b};
        Arrays.sort(tri);
        if (tri[0] >= 0 && tri[0] + tri[1] >= tri[2]) return true;
        return false;
    }
}
