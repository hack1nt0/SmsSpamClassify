import java.util.LinkedList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractCollection;
import java.util.Set;

/**
 * Built using CHelper plug-in
 * Actual solution is at the top
 */
public class ConnectingGameDiv2 {
    public int getmin(String[] board) {
        int N = board.length, M = board[0].length();
        Map<Character, Integer> sccId = new HashMap<Character, Integer>();
        for (String r: board)
            for (char c: r.toCharArray()) if (!sccId.containsKey(c)) sccId.put(c, sccId.size());
        boolean[][] adj = new boolean[sccId.size()][sccId.size()];
        int[] cells = new int[sccId.size()];
        int[] di = {-1, +1, 0, 0}, dj = {0, 0, -1, +1};
        for (int i = 0; i < N; ++i)
            for (int j = 0; j < M; ++j) {
                int curScc = sccId.get(board[i].charAt(j));
                cells[curScc]++;
                for (int d = 0; d < 4; ++d) {
                    int ni = i + di[d], nj = j + dj[d];
                    if (!isInBound(ni, nj, N, M)) continue;
                    int adjScc = sccId.get(board[ni].charAt(nj));
                    adj[curScc][adjScc] = adj[adjScc][curScc] = true;
                }
            }
        Graph G = new Graph(sccId.size() + 2);
        for (int i = 0; i < adj.length; ++i)
            for (int j = 0; j < adj[i].length; ++j) {
                if (i == j || !adj[i][j]) continue;
                G.addEdge(i, j, cells[j]);
            }
        int S = sccId.size(), T = S + 1;
        Set<Integer> firstRScc = new HashSet<Integer>();
        Set<Integer> firstCScc = new HashSet<Integer>();
        for (int i = 0; i < M; ++i) {
            firstRScc.add(sccId.get(board[0].charAt(i)));
            firstCScc.add(sccId.get(board[N - 1].charAt(i)));
        }
        for (int scc: firstRScc) {
            G.addEdge(S, scc, cells[scc]);
            G.addEdge(scc, S, cells[scc]);
        }
        for (int scc: firstCScc) {
            G.addEdge(scc, T, N * M);
            G.addEdge(T, scc, N * M);
        }
        int ret = G.getMaxFlow(S, T);
        return ret;
    }

    private boolean isInBound(int i, int j, int N, int M) {
        return 0 <= i && i < N && 0 <= j && j < M;
    }

    class Graph {
        private class Edge {
            int from, to, cap;

            Edge(int from, int to, int cap) {
                this.cap = cap;
                this.to = to;
                this.from = from;
            }
        }

        List<Edge>[] adj;
        int[] dist; //layer-network
        int[] curEdge; //curEdge optimize

        public Graph(int MAXN) {
            adj = new ArrayList[MAXN];
            for (int i = 0; i < MAXN; ++i) adj[i] = new ArrayList<Edge>();
            dist = new int[MAXN];
            curEdge = new int[MAXN];
        }

        void addEdge(int from, int to, int cap) {
            adj[from].add(new Edge(from, to, cap));
            adj[to].add(new Edge(to, from, 0));
        }

        public int getMaxFlow(int s, int t) {
            int res = 0;
            for(;bfs(s, t);) {
                Arrays.fill(curEdge, 0);
                for(;;) {
                    int flow = dfs(s, t, Integer.MAX_VALUE);
                    if (flow == 0) break;
                    res += flow;
                }
            }
            return res;
        }

        int dfs(int s, int t, int accFlow) {
            if (s == t)
                return accFlow;
            for (int i = curEdge[s]; i < adj[s].size(); i++, curEdge[s]++) {
                Edge e = adj[s].get(i);
                if (dist[e.to] == dist[s] + 1 && e.cap > 0) {
                    //inEdge[e.to] = e;
                    int deltaFlow = dfs(e.to, t, Math.min(e.cap, accFlow));
                    if (deltaFlow > 0) {
                        e.cap -= deltaFlow; //update residual-network
                        return deltaFlow;
                    }
                }
            }
            return 0;
        }

        boolean bfs(int s, int t) {
            Arrays.fill(dist, -1);
            LinkedList<Integer> que = new LinkedList<Integer>();
            que.add(s);
            dist[s] = 0;
            for(;!que.isEmpty();) {
                int cur = que.pop();
                for (Edge e: adj[cur]) if (dist[e.to] == -1 && e.cap > 0) {
                    int nxt = e.to;
                    dist[nxt] = dist[cur] + 1;
                    que.add(nxt);
                }
            }
            return dist[t] != -1;
        }
    }
}

