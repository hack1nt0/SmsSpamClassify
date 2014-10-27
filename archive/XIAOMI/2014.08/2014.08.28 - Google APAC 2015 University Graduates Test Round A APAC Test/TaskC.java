package TC;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.PrintWriter;

public class TaskC {
    Map<Long, Long> inStack = new HashMap<>();
    int vocab;

    public void solve(int testNumber, Scanner in, PrintWriter out) {
        inStack.clear();
        out.println("Case #" + testNumber + ":");
        Map<String, Integer> ID = new HashMap<>();
        int N = in.nextInt();
        int[][] eqs = new int[N][3];
        for (int i = 0; i < N; ++i) {
            String[] tmp = in.next().split("\\+|=");
            int a = toID(tmp[0], ID), b = toID(tmp[1], ID);
            eqs[i] = new int[]{Math.min(a, b), Math.max(a, b), Integer.valueOf(tmp[2])};
        }
        vocab = ID.size();
        int Q = in.nextInt();
        for (int i = 0; i < Q; ++i) {
            String[] tmp = in.next().split("\\+");
            if (!ID.containsKey(tmp[0]) || !ID.containsKey(tmp[1]))
                continue;
            int a = toID(tmp[0], ID), b = toID(tmp[1], ID);
            long ret = add(Math.min(a, b), Math.max(a, b), eqs);
            if (ret == Long.MAX_VALUE)
                continue;
            out.println(tmp[0] + "+" + tmp[1] + "=" + ret);
            out.flush();
        }
    }

    private long add(int a, int b, int[][] eqs) {
        long key = (long)a * vocab + b;
        if (inStack.containsKey(key))
            return inStack.get(key);
        long res = Long.MAX_VALUE;
        inStack.put(key, Long.MAX_VALUE);

        int n = eqs.length;
        for (int i = 0; i < eqs.length; ++i)
            if (eqs[i][0] == a && eqs[i][1] == b) {
                inStack.put(key, (long) eqs[i][2]);
                return inStack.get(key);
            }
        int na, nb;
        na = nb = -1;
        long c = 0;
        for (int i = 0; i < n; ++i)
            if (eqs[i][0] == a || eqs[i][1] == a) {
                na = eqs[i][1] + eqs[i][0] - a;
                c = 0;
                c += eqs[i][2];
                for (int j = 0; j < n; ++j) if (j != i)
                    if (eqs[j][0] == b || eqs[j][1] == b) {
                        nb = eqs[j][1] + eqs[j][0] - b;
                        c += eqs[j][2];
                        if (na != -1 && nb != -1) {
                            res = add(na, nb, eqs);
                            if (res != Long.MAX_VALUE) {
                                res = c - res;
                                inStack.put(key, res);
                                return res;
                            }
                        }
                        c -= eqs[j][2];
                    }
            }
        return Long.MAX_VALUE;
    }


    private int toID(String name, Map<String, Integer> ID) {
        if (!ID.containsKey(name))
            ID.put(name, ID.size());
        return ID.get(name);
    }
}
