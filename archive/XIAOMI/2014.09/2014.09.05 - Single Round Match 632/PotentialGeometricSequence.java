package TC;

public class PotentialGeometricSequence {
    public int numberOfSubsequences(int[] d) {
        int ret = 0;
        for (int i = 0, j = 0; i < d.length - 1;) {
            boolean first = true;
            while (j + 1 < d.length && d[j + 1] - d[j] == d[i + 1] - d[i]) ++j;
            int dn = j - i + 1;
            for (int k = 2; k <= dn; ++k)
                ret += dn - (k - 1);
            i = j;
        }
        ret += d.length;
        return ret;
    }
}
