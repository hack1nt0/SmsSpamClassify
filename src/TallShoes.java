import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * Built using CHelper plug-in
 * Actual solution is at the top
 * @author DY
 */
public class TallShoes {
    class Edge {
        int to; long cap;

        public Edge(int to, long cap) {
            this.to = to;
            this.cap = cap;
        }
    }

    public List<Edge>[] adj;
    public int S, T;
    public boolean[] vis;

    public int maxHeight(int N, int[] X, int[] Y, int[] height, long B) {
        adj = new ArrayList[N];
        vis = new boolean[N];
        S = 0; T = N - 1;
        for (int i = 0; i < N; ++i) adj[i] = new ArrayList<Edge>();
        for (int i = 0; i < X.length; ++i) {
            int from = X[i], to = Y[i];
            adj[from].add(new Edge(to, height[i]));
            adj[to].add(new Edge(from, height[i]));
        }
        long L = 0, R = 10L + Integer.MAX_VALUE;
        while (L < R) {
            long M = L + (R - L) /2;
            Arrays.fill(vis, false);
            if (findPath(S, B, M)) L = M + 1;
            else R = M;
        }
        return (int)(L - 1);
    }

    private boolean findPath(int cur, long leftBudget, long needCap) {
        if (leftBudget < 0) return false;
        if (cur == T) return true;
        vis[cur] = true;
        for (Edge e: adj[cur]) {
            if (vis[e.to]) continue;
            if (needCap <= e.cap && findPath(e.to, leftBudget, needCap)
                    || findPath(e.to, leftBudget - (needCap - e.cap) * (needCap - e.cap), needCap))
                return true;
        }
        return false;
    }
}

