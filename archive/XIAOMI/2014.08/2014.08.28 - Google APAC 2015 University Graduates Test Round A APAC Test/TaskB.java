package TC;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.PrintWriter;

public class TaskB {
    public void solve(int testNumber, Scanner in, PrintWriter out) {
        out.println("Case #" + testNumber + ":");
        int N = in.nextInt();
        String D = in.next();
        Map<String, Integer> rn = new HashMap<>();
        rn.put("up", 1);
        rn.put("down", 3);
        rn.put("left", 0);
        rn.put("right", 2);
        int[][] M = new int[N][N];
        for (int i = 0; i < N; ++i)
            for (int j = 0; j < N; ++j)
                M[i][j] = in.nextInt();
        switch (D) {
            case "up":
                T(M);
                doLeft(M);
                T(M);
                break;
            case "down":
                rotate(M, 1);
                doLeft(M);
                rotate(M, 3);
                break;
            case "right":
                R(M);
                doLeft(M);
                R(M);
                break;
            case "left":
                doLeft(M);
                break;
        }
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) {
                if (j > 0) out.print(" ");
                out.print(M[i][j]);
            }
            out.println();
        }
    }

    private void doLeft(int[][] M) {
        int N = M.length;
        for (int r = 0; r < N; ++r) {
            int p, q, q0;
            p = q = q0 = 0;
            while (true) {
                while (q < N && M[r][q] == 0)
                    ++q;
                if (q >= N) break;
                q0 = q + 1;
                while (q0 < N && M[r][q0] == 0)
                    ++q0;
                if (q0 < N && M[r][q] == M[r][q0]) {
                    M[r][p] = M[r][q] * 2;
                    q = q0 + 1;
                } else {
                    M[r][p] = M[r][q];
                    q++;
                }
                p++;
            }
            while (p < N)
                M[r][p++] = 0;
        }
    }

    private void rotate(int[][] M, int t) {
        for (int i = 0; i < t; ++i) {
            T(M);
            R(M);
        }
    }
    private void T(int[][] M) {
        for (int i = 0; i < M.length; ++i)
            for (int j = 0; j < i; ++j) {
                int t = M[i][j];
                M[i][j] = M[j][i];
                M[j][i] = t;
            }
    }

    private void R(int[][] M) {
        for (int i = 0; i < M.length; ++i) {
            for (int s = 0, t = M.length - 1; s < t; ++s, --t) {
                int tmp = M[i][s];
                M[i][s] = M[i][t];
                M[i][t] = tmp;
            }
        }
    }
}
