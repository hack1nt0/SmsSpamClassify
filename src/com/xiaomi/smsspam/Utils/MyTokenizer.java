package com.xiaomi.smsspam.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dy on 14-11-20.
 */
public class MyTokenizer {
    Dict dict;
    HMM hmm;

    public MyTokenizer(String dictFilePath, String HMMFilePath) {
        dict = new AlphabeticalOrderArray(dictFilePath);
        //dict = new Trie(dictFilePath);
        hmm = new HMM(HMMFilePath);
    }

    public String[] getTokens(String text) {
        List<String> ret = new ArrayList<>();
        char[] arr = text.toCharArray();
        int N = arr.length;

        float[] dp = new float[N + 1];
        dp[N] = 0;
        int[] nxt = new int[N + 1];
        for (int i = N - 1; i >= 0; --i) {
            nxt[i] = i;
            dp[i] = dict.getMinLogFreq() + dp[i + 1];
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
        //ret = hmm.getTokens(arr, 0, arr.length);
        return ret.toArray(new String[0]); //ToDO
    }

    public boolean inDict(String token){
        int index = dict.contains(token.toCharArray(), 0, token.length());
        return index >= 0;
    }

    public static void main(String[] args) {
        MyTokenizer myTokenizer = new MyTokenizer("data/jieba.dict.utf8.sorted", "data/hmm_model.utf8");

        String text = "姜文的一步之遥将于12月18号上映，期待！";
        String[] tokens = myTokenizer.getTokens(text);
        for (String t: tokens) System.out.println(t);
    }
}

class HMM {
    int YN = Character.MAX_VALUE;
    int XN = 4;
    float[] PPx = new float[XN];
    float[][] Pxixj = new float[XN][XN];
    float[][] Pxiyi = new float[XN][YN];
    float[][] dp = new float[YN][XN];
    int[][] nxt = new int[YN][XN];
    String decode = "BEMS";
    //B E M S
    int[][] autoM = {
            {1, 0, 0, 0},
            {2, 0, 1, 2},
    };

    public HMM(String filePath) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            long beforeM = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long beforeT = System.currentTimeMillis();

            while (true) {
                String line = in.readLine();
                if (line == null) break;
                if (line.charAt(0) == '#') continue;
                String[] tmp = line.split(" ");
                for (int i = 0; i < PPx.length; ++i) PPx[i] = Float.valueOf(tmp[i]);
                line = in.readLine();
                for (int i = 0; i < XN; ++i) {
                    line = in.readLine();
                    tmp = line.split(" ");
                    for (int j = 0; j < XN; ++j) Pxixj[i][j] = Float.valueOf(tmp[j]);
                }

                for (int i = 0; i < XN;) {
                    line = in.readLine();
                    if (line == null) break;
                    if (line.charAt(0) == '#') continue;
                   // StringReader sr = new StringReader(line);
                    for (int j = 0; j < line.length();) {
                        char y = line.charAt(j);
                        j += 2;
                        int k = j;
                        while (k < line.length() && line.charAt(k) != ',') ++k;
                        float logFreq = Float.valueOf(line.substring(j, k));
                        Pxiyi[i][y] = logFreq;
                        j = k > line.length() ? k : k + 1;
                    }
                    ++i;
                }
            }
            in.close();
            System.gc();
            long afterM = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long afterT = System.currentTimeMillis();
            double usedM = (afterM - beforeM) / 1024.0 / 1024;
            double usedT = (afterT - beforeT) / 1000.0;
            System.out.println("Loaded HMM Model.");
            System.out.println("Consumed Time: " + usedT + "s");
            System.out.println("Consumed Mem: " + usedM + "MB");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getTokens(char[] text, int L, int R) {
        List<String> ret = new ArrayList<>();
        int M = R - L;
        Arrays.fill(dp[M], 0);
        for (int i = M - 1; i >= 0; --i)
            for (int j = 0; j < XN; ++j) {
                float res = Float.NEGATIVE_INFINITY;
                for (int k = 0; k < XN; ++k) {
                    if (Pxixj[j][k] == Float.NEGATIVE_INFINITY || dp[i + 1][k] == Float.NEGATIVE_INFINITY) continue;
                    float tmp = dp[i + 1][k] + Pxixj[j][k];
                    if (tmp > res) {
                        res = tmp;
                        nxt[i][j] = k;
                    }
                }
                if (res == Float.NEGATIVE_INFINITY || Pxiyi[j][text[i + L]] == Float.NEGATIVE_INFINITY) {
                    dp[i][j] = Float.NEGATIVE_INFINITY;
                    int maxPPxi = 0; for (int k = 0; k < XN; ++k) if (PPx[maxPPxi] < PPx[k]) maxPPxi = k;
                    nxt[i][j] = maxPPxi;
                }
                else
                    dp[i][j] = res  + Pxiyi[j][text[i + L]];
            }
        for (int i = 0; i < XN; ++i) dp[0][i] += PPx[i];
        int xi = 0;
        for (int i = 0; i < XN; ++i) if (dp[0][i] > dp[0][xi]) xi = i;
        int[] xs = new int[M]; xs[0] = xi;
        for (int i = 1; i < xs.length; ++i) xs[i] = nxt[i - 1][xs[i - 1]];

        /*
        int xit = xi;
        for (int i = 0; i < M; xit = nxt[i][xit], ++i) System.out.print(decode.charAt(xit));
        System.out.println();
        */

        for (int i = 0, autoS = 0; i < M;) {
            autoS = autoM[autoS][xs[i]];
            int j = i + 1;
            while (j < M && autoS == 1) {
                autoS = autoM[autoS][xs[j]];
                ++j;
            }

            if (autoS == 0) {
                ret.add(new String(text, L + i, j - i));
                i = j;
            } else if (j >= M) {//TODO
                for (int k = i; k < M; ++k) ret.add(new String(text, L + k, 1));
                break;
            } else {
                for (int k = i; k < j - 1; ++k) ret.add(new String(text, L + k, 1));
                i = j - 1;
            }
            autoS = 0;
        }
        return ret;
    }
}
