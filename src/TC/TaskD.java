package TC;

import java.util.*;
import java.io.PrintWriter;

public class TaskD {
    class Tile {
        long p, pow, maxn, left, need;
    }

    public void solve(int testNumber, Scanner in, PrintWriter out) {
        out.print("Case #" + testNumber + ": ");
        int N = in.nextInt();
        long M = in.nextInt();
        Map<Integer, Integer> pcnt = new TreeMap<Integer, Integer>();
        for (int i = 0; i < N; ++i) {
            int t = in.nextInt();
            pcnt.put(t, pcnt.containsKey(t) ? pcnt.get(t) + 1 : 1);
        }
        Tile[] tiles = new Tile[pcnt.size()];
        for (int i = 0; i < tiles.length; ++i) {
            tiles[i] = new Tile();
        }
        int tmp = 0;
        for (int p: pcnt.keySet()) {
            tiles[tmp].pow = 1L << p;
            tiles[tmp].p = p;
            tiles[tmp].left = 0;
            tiles[tmp].need = pcnt.get(p);
            tmp++;
        }
        Arrays.sort(tiles, new Comparator<Tile>() {
            @Override
            public int compare(Tile o1, Tile o2) {
                return (int)(o2.p - o1.p);
            }
        });
        long[] h = new long[3];
        long[] w = new long[3];
        Arrays.fill(h, M);
        Arrays.fill(w, M);
        for (int i = 0; i < tiles.length; ++i) {
            tiles[i].maxn = count(tiles[i].pow, h[0], w[0]) + count(tiles[i].pow, h[1], w[1]) - count(tiles[i].pow, h[2], w[2]);
            h[0] %= tiles[i].pow;
            w[1] %= tiles[i].pow;
            h[2] = h[0];
            w[2] = w[1];
        }
        long ret = 1;
        for (int i = 0; i < tiles.length; ++i) {
            long has = i > 0 ? tiles[i - 1].left * tiles[i - 1].pow / tiles[i].pow : 0;
            has += tiles[i].maxn;
            if (has > tiles[i].need) {
                tiles[i].left = has - tiles[i].need;
            } else {
                while (has < tiles[i].need) {
                    for (int j = 0; j <= i; ++j)
                        has += tiles[j].maxn * tiles[j].pow / tiles[i].pow;
                    ++ret;
                }
                tiles[i].left = has - tiles[i].need;
            }
        }
        out.println(ret);
    }

    private long count(long len, long h, long w) {
        return h / len * (w / len);
    }
}
