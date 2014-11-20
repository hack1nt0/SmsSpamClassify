package com.xiaomi.smsspam.Utils;

import com.xiaomi.smsspam.preprocess.Y;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dy on 14-11-20.
 */
public class MyTokenizer {
    Dict dict;
    HMM hmm;

    public MyTokenizer(String dictFilePath, String HMMFilePath) {
        dict = new AlphabeticalOrderArray(dictFilePath);
    }

    public String[] getTokens(String res) {
        List<String> ret = new ArrayList<>();
        char[] arr = res.toCharArray();
        int N = arr.length;
        float[] dp = new float[N + 1]; dp[N] = 0;
        int[] nxt = new int[N + 1];
        for (int i = N - 1; i >= 0; --i) {
            nxt[i] = i;
            for (int j = i + 1; j <= N; ++j) {
                int index = dict.contains(arr, i, j);
                if (index == -1) continue;
                float tmp = dict.getLogFreq(index) + dp[j];
                if (tmp > dp[i]) {
                    nxt[i] = j;
                    dp[i] = tmp;
                }
            }
        }
        for (int i = 0; i < N;) {
            if (nxt[i] == i) {
                int j = i;
                for (; j < N && nxt[j] == j; ++j);
                ret.addAll(hmm.getTokens(arr, i, j));
                i = j;
                continue;
            }
            ret.add(new String(arr, i, nxt[i] - i));
            i = nxt[i];
        }
        return ret.toArray(new String[0]); //ToDO
    }

    public boolean inDict(String token){
        int index = dict.contains(token.toCharArray(), 0, token.length());
        return index >= 0;
    }
}

class HMM {
    int YN = Character.MAX_VALUE;
    int XN = 4;
    float[] Ppriori = new float[YN];
    float[][] Pxixj = new float[XN][XN];
    float[][] Pxiyi = new float[XN][YN];
    float[][] dp = new float[YN][XN];
    int[][] nxt = new int[YN][XN];

    //B E M S
    int[][] autoM = {
            {1, 0, 0, 1},
            {2, 0, 1, 2},
    };

    public HMM(String filePath) {
        //TODO
    }

    public List<String> getTokens(char[] text, int L, int R) {
        List<String> ret = new ArrayList<>();
        int M = R - L;
        for (int i = 0; i < XN; ++i) dp[0][i] = Ppriori[i] + Pxiyi[i][text[L]];
        for (int i = M - 1; i >= 0; ++i)
            for (int j = 0; j < XN; ++j) {
                float res = 0;
                for (int k = 0; k < XN; ++k) {
                    float tmp = dp[i + 1][k] + Pxixj[j][k];
                    if (tmp > res) {
                        res = tmp;
                        nxt[i][j] = k;
                    }
                }
                dp[i][j] = res + Pxixj[j][text[i + L]];
            }
        for (int i = 0; i < XN; ++i) dp[0][i] += Pxiyi[i][text[L]];
        int xi = 0;
        for (int i = 0; i < XN; ++i) if (dp[0][i] > dp[0][xi]) xi = i;

        for (int i = 0, autoS = 0; i < M;) {
            autoS = autoM[autoS][i];
            int j = i + 1;
            while (j < M && autoS == 1) autoS = autoM[autoS][j++];
            if (autoS == 0)
                ret.add(new String(text, L + i, j - i));
            else
                for (int k = i; k < j; ++k) ret.add(new String(text, L + k, 1));

            autoS = 0;
            i = j;
        }
        return ret;
    }
}
