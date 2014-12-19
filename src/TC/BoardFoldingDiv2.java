package TC;

import java.util.Arrays;

public class BoardFoldingDiv2 {
    public int howMany(String[] paper) {//WA
        int N = paper.length, M = paper[0].length();
        int[] dpC = new int[M + 1], dpR = new int[N + 1];
        int[] dpC1 = new int[M + 1], dpR1 = new int[N + 1];
        Arrays.fill(dpC, Integer.MAX_VALUE); Arrays.fill(dpR, Integer.MAX_VALUE); dpC[0] = dpC[M] = 0; dpR[0] = dpR[N] =  0;
        Arrays.fill(dpC1, Integer.MAX_VALUE); Arrays.fill(dpR1, Integer.MAX_VALUE); dpC1[M] = dpC1[0] = 0; dpR1[0] = dpR1[N] = 0;

        for (int i = 1; i < M; ++i) {
            for (int l = 1; l <= Math.min(i, M - i); ++l) {
                if (!isameC(paper, i - l, i + l - 1)) break;
                if (dpC[i - l] != Integer.MAX_VALUE) {
                    dpC[i] = l;break;
                }
            }
        }
        for (int i = M - 1; i > 0; --i) {
            for (int l = 1; l <= Math.min(i, M - i); ++l) {
                if (!isameC(paper, i - l, i + l - 1)) break;
                if (dpC1[i + l] != Integer.MAX_VALUE) {
                    dpC1[i] = l;break;
                }
            }
        }
        for (int i = 1; i < N; ++i) {
            for (int l = 1; l <= Math.min(i, N - i); ++l) {
                if (!isameR(paper, i - l, i + l - 1)) break;
                if (dpR[i - l] != Integer.MAX_VALUE) {
                    dpR[i] = l;break;
                }
            }
        }
        for (int i = N - 1; i > 0; --i) {
            for (int l = 1; l <= Math.min(i, N - i); ++l) {
                if (!isameR(paper, i - l, i + l - 1)) break;
                if (dpR1[i + l] != Integer.MAX_VALUE) {
                    dpR1[i] = l;break;
                }
            }
        }
        int ret = 0;
        for (int i = 0; i < N; ++i)
            for (int j = 0; j < M; ++j) {
                if (dpR[i] == Integer.MAX_VALUE || dpC[j] == Integer.MAX_VALUE) continue;
                int cj = 0;
                for (int tj = j + Math.max(1, dpC[j]); tj <= M; ++tj) if (dpC1[tj] <= tj - j) ++cj;
                int ci = 0;
                for (int ti = i + Math.max(1, dpR[i]); ti <= N; ++ti) if (dpR1[ti] <= ti - i) ++ci;
                ret += ci * cj;
            }

        return ret;
    }

    private boolean isameR(String[] paper, int a, int b) {
        for (int i = 0; i < paper[0].length(); ++i) if (paper[a].charAt(i) != paper[b].charAt(i))
            return false;
        return true;
    }

    private boolean isameC(String[] pater, int a, int b) {
        for (int i = 0; i < pater.length; ++i) if (pater[i].charAt(a) != pater[i].charAt(b))
            return false;
        return true;
    }
}
