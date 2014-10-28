package TC;

public class ClosestRabbit {
    public double getExpected(String[] board, int r) {
        int N = board.length, M = board[0].length();
        int[][] S = new int[N + 1][M + 1];
        for (int i = 1; i <= N; ++i)
            for (int j = 1; j <= M; ++j) S[i][j] = S[i][j - 1] + S[i - 1][j] - S[i - 1][j - 1] + board[i - 1].charAt(j - 1) - '0';
        int ret = 0;
        for (int r1 = 1; r1 < N; ++r1)
            for (int r2 = r1 + 1; r2 < N; ++r2)
                for (int c1 = 1; c1 < M; ++c1)
                    for (int c2 = c1 + 1; c2 < M; ++c2) {
                        int res = Integer.MAX_VALUE;
                        res = Math.min(getSum(0, r1, 0, c1, S), res);
                        res = Math.min(getSum(0, r1, c1, c2, S), res);
                        res = Math.min(getSum(0, r1, c2, M, S), res);

                        res = Math.min(getSum(r1, r2, 0, c1, S), res);
                        res = Math.min(getSum(r1, r2, c1, c2, S), res);
                        res = Math.min(getSum(r1, r2, c2, M, S), res);

                        res = Math.min(getSum(r2, N, 0, c1, S), res);
                        res = Math.min(getSum(r2, N, c1, c2, S), res);
                        res = Math.min(getSum(r2, N, c2, M, S), res);
                        ret = Math.max(res, ret);
                    }
        return ret;
    }

    private int getSum(int r1, int r2, int c1, int c2, int[][] S) {
        return S[r2][c2] - S[r2][c1] - S[r1][c2] + S[r1][c1];
    }
}
