
/**
 * Built using CHelper plug-in
 * Actual solution is at the top
 * @author DY
 */
public class NarrowPassage2 { //WA
    public int count(int[] size, int maxSizeSum) {
        int N = size.length;
        long MOD = (long) 1e9 + 7;
        long[][] dp = new long[N][N];
        for (int i = 0; i < N; ++i) dp[i][i] = 1;

        for (int len = 2; len <= N; ++len)
            for (int i = 0; i + len - 1 < N; ++i) {
                int L = i, R = i + len - 1;
                /*
                if (len == 1) {
                    dp[L][R] = 1;
                    continue;
                }*/
                long res = dp[L + 1][R] % MOD * 2 % MOD;
                for (int j = i + 1; j < R; ++j) {
                    if (size[L] + size[j] > maxSizeSum) break;
                    res = (res + dp[L + 1][j] * dp[j + 1][R] % MOD) % MOD;
                }
                dp[L][R] = res % MOD;
            }
        int ret = (int) dp[0][N - 1];
        return ret;
    }
}

