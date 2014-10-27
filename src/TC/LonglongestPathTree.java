package TC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LonglongestPathTree { //WA
    class Edge implements Comparable<Edge> {
        int v; long l;

        Edge(int v, long l) {
            this.v = v;
            this.l = l;
        }

        @Override
        public int compareTo(Edge o) {
            if (o.l == l) return 0;
            if (o.l > l) return 1;
            return -1;
        }
    }
    List<Edge>[] adj;
    long[] chd, unchd;
    long ret;
    public long getLength(int[] A, int[] B, int[] L) {
        int N = A.length + 1;
        adj = new ArrayList[N]; for (int i = 0; i < N; ++i) adj[i] = new ArrayList<>();
        for (int i = 0; i < A.length; ++i) {
            adj[A[i]].add(new Edge(B[i], L[i]));
            adj[B[i]].add(new Edge(A[i], L[i]));
        }
        chd = new long[N]; unchd = new long[N];
        DFS(0, -1);
        return ret;
    }

    private void DFS(int cur, int fa) {
        List<Edge> chds = new ArrayList<>(), unchds = new ArrayList<>();
        for (Edge e: adj[cur]) {
            if (e.v == fa) continue;
            DFS(e.v, cur);
            unchds.add(new Edge(e.v, e.l + unchd[e.v]));
            chds.add(new Edge(e.v, chd[e.v] == 0 ? 0 : e.l + chd[e.v]));
        }
        if (chds.size() == 0) return;

        Collections.sort(chds);
        Collections.sort(unchds);
        long res1 = 0, res2 = 0;
        for (int i = 0; i < unchds.size() && i < 3; ++i) res1 += unchds.get(i).l;
        res2 = chds.get(0).v == unchds.get(0).v ? (unchds.size() > 1 ? unchds.get(1).l : 0) + chds.get(0).l : unchds.get(0).l + chds.get(0).l;
        ret = Math.max(res1, ret);
        ret = Math.max(res2, ret);
        chd[cur] = chds.get(0).l;
        if (unchds.size() > 1) chd[cur] = Math.max(unchds.get(0).l + unchds.get(1).l, chd[cur]);
        unchd[cur] = unchds.get(0).l;
    }
}
