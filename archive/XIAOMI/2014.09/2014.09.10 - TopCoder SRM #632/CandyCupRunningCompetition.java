package TC;

import java.util.*;

public class CandyCupRunningCompetition { //WA, wrong to mod the capacity of the edge
    class Edge {
        int a, b; long cap;
        Edge rev;

        Edge(int a, int b, long cap) {
            this.a = a;
            this.b = b;
            this.cap = cap;
        }
    }
    List<Edge>[] adj;
    long MOD = (long)1e9 + 7;
    int[] lv, nEdgeIndex;
    List<Edge> mE;
    int N, M;

    public int findMaximum(int N, int[] A, int[] B) {
        this.N = N;
        this.M = A.length;
        adj = new ArrayList[N];
        lv = new int[N];
        mE = new ArrayList<>();
        nEdgeIndex = new int[A.length];
        for (int i = 0; i < N; ++i) adj[i] = new ArrayList<>();
        for (int i = 0; i < A.length; ++i) {
            addEdge(A[i], B[i], pow(3, i));
            addEdge(B[i], A[i], pow(3, i));
        }
        long ret = maxFlow(0, N - 1);
        return (int)ret;
    }

    private void addEdge(int a, int b, long cap) {
        Edge e1 = new Edge(a, b, cap);
        Edge e2 = new Edge(b, a, 0);
        e1.rev = e2; e2.rev = e1;
        adj[e1.a].add(e1); adj[e1.b].add(e2);
    }

    private long maxFlow(int S, int T) {
        long res = 0;
        while (BFS(S, T)) {
            long acc = 0;
            Arrays.fill(nEdgeIndex, 0);//1
            do {
                mE.clear();
                acc = DFS(S, T);
                for (Edge e: mE) {
                    e.cap -= acc; e.rev.cap += acc;
                }
                res = (res + acc) % MOD;
            } while (acc > 0);
        }
        return res;
    }

    private long DFS(int S, int T) {
        if (S == T) return Integer.MAX_VALUE;
        while (nEdgeIndex[S] < adj[S].size()) {
            Edge e = adj[S].get(nEdgeIndex[S]);
            long res;
            if (e.cap <= 0 || lv[e.b] <= lv[e.a] /*2*/ || (res = DFS(e.b, T)) == 0) {
                nEdgeIndex[S]++; continue;
            }
            mE.add(e);
            return Math.min(e.cap, res);
        }
        return 0;
    }

    private boolean BFS(int S, int T) {
        Queue<Integer> Q = new LinkedList<>();
        Q.add(S);
        Arrays.fill(lv, 0);
        boolean[] vis = new boolean[N];
        vis[S] = true;
        while (!Q.isEmpty()) {
            int h = Q.poll();
            for (Edge e : adj[h]) if (!vis[e.b] && e.cap > 0) {
                Q.add(e.b);
                lv[e.b] = lv[e.a] + 1;
                vis[e.b] = true;
            }
        }
        return vis[T];
    }

    private long pow(int a, int b) {
        if (b == 0) return 1;
        long res = pow(a, b / 2);
        res = res * res % MOD;
        if ((b & 1) > 0) res = res * a % MOD;
        return res;
    }
}
