package TC;

public class TaroCoins {
    public long getNumber(long N) { //WA
        int MAXN = 60;
        for (MAXN = 0; (1L << MAXN) <= N; ++MAXN); --MAXN;
        //System.out.println(1L << MAXN);
        int maxACC = 3;
        long dp[][] = new long[MAXN + 1][maxACC + 1];
        if ((N >> 0 & 1) == 0)
            dp[0][2] = 1;
        else
            dp[0][1] = 1;
        for (int i = 0; i < MAXN; ++i) {
            for (int j = 0; j <= maxACC; ++j) {
                if ((N >> i + 1 & 1) > 0) {
                    dp[i + 1][1 + j / 2] += dp[i][j];
                    if (j >= 2)
                        dp[i + 1][2 + (j - 2) / 2] += j * (j - 1) / 2 * dp[i][j];
                } else
                    dp[i + 1][2 + j / 2] += dp[i][j];
            }
        }
        long ret = 0;
        for (int i = 0; i <= maxACC; ++i) {
            ret += dp[MAXN][i];
            //kSystem.out.println(dp[MAXN][i]);
        }
        return ret;
    }
}
