package TC;

import java.util.HashMap;
import java.util.HashSet;

public class CandyAddict {
    public long[] solve(int[] X, int[] Y, int[] Z) {
        HashMap<Long, Long> firstOccur = new HashMap<Long, Long>();
        long[] ans = new long[X.length];
        for (int Q = 0; Q < X.length; ++Q) {
            long lc = 0, lm = 0;
            firstOccur.clear();
            firstOccur.put(0L, 1L);
            for (long day = 1; day <= Z[Q]; ) {
                long maxExt = (lm + X[Q]) / Y[Q];
                lm = (lm + X[Q]) % Y[Q];
                long day1 = Math.min(Z[Q] + 1, day + maxExt);
                if (Z[Q] < day1) {
                    ans[Q] = lm + (Z[Q] - day) * X[Q];
                    break;
                }

                if (firstOccur.containsKey(lm)) {
                    long step = day + maxExt - firstOccur.get(lm);
                    long ldist = (Z[Q] - (day + maxExt) + 1) % step;
                    ans[Q] = lm + (ldist - 1) * X[Q];
                    break;
                }
                //firstOccur.put(lm, day);
                lm += (maxExt - 1) * X[Q];
                day += maxExt;
            }
        }
        return ans;
    }
}
