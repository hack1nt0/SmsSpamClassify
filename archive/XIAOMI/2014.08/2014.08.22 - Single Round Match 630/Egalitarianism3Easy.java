package TC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.List;

public class Egalitarianism3Easy {
    public int maxCities(int n, int[] a, int[] b, int[] len) {
        int[][] d = new int[n + 1][n + 1];//[]
        for (int i = 0; i < d.length; ++i)
            Arrays.fill(d[i], (int)1e5);
        for (int i = 0; i < a.length; ++i) {
            d[a[i]][b[i]] = d[b[i]][a[i]] = len[i];
        }
        for (int k = 1; k <= n; ++k)
            for (int i = 1; i <= n; ++i)
                for (int j = 1; j <= n; ++j)
                    d[i][j] = Math.min(d[i][k] + d[k][j], d[i][j]);
        int ans = 1;
        for (int S = 0; S < 1 << n; ++S) {
            List<Integer> eles = new ArrayList<Integer>();
            for (int i = 0; i < n; ++i) if (((S >> i) & 1) != 0)
                eles.add(i + 1);
            if (eles.size() < 2)
                continue;
            int inst = d[eles.get(0)][eles.get(1)];
            boolean sat = true;
            for (int i = 0; i < eles.size(); ++i)
                for (int j = i + 1; j < eles.size(); ++j) if (d[eles.get(i)][eles.get(j)] != inst) {
                    sat = false;
                    break;
                }
            if (sat)
                ans = Math.max(eles.size(), ans);
        }
        return ans;
    }
}
