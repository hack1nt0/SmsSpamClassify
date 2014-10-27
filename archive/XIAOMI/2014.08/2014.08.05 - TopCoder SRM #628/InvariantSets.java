package TC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class InvariantSets {
    int[] belong;
    int circles;
    int[] f;
    boolean[] vis;
    ArrayList<Integer>[] adj;

    public long countSets(int[] f) {
        this.f = f;
        vis = new boolean[f.length];
        belong = new int[f.length];
        adj = new ArrayList[f.length];
        for (int i = 0; i < f.length; ++i)
            adj[i] = new ArrayList<Integer>();
        for (int i = 0; i < f.length; ++i)
            adj[f[i]].add(i);

        Arrays.fill(belong, -1);
        for (int i = 0; i < f.length; ++i) {
            if (0 <= belong[i]) continue;
            Arrays.fill(vis, false);
            findCircles(i, i);
        }
        long ans = 1;
        Arrays.fill(vis, false);
        for (int i = 0; i < f.length; ++i) {
            if (belong[i] == -1 || vis[belong[i]])
                continue;
            vis[belong[i]] = true;
            long res = 1;
            for (int j = 0; j < f.length; ++j) {
                if (belong[j] != belong[i])
                    continue;
                for (int chd: adj[j]) {
                    if (belong[chd] != -1)
                        continue;
                    res *= count(chd);
                }
            }
            res += 1;
            ans *= res;
        }
        return ans;
    }

    private long count(int cur) {
        if (adj[cur].size() == 0)
            return 2L;
        long res = 1;
        for (int chd: adj[cur])
            res *= count(chd);
        res += 1;
        return res;
    }

    private void findCircles(int begin, int cur) {
        if (vis[cur]) {
            if (cur == begin) {
                belong[begin] = circles;
                for (int i = f[begin]; i != begin; i = f[i])
                    belong[i] = circles;
                circles++;
            }
            return;
        }
        vis[cur] = true;
        findCircles(begin, f[cur]);
    }
}
