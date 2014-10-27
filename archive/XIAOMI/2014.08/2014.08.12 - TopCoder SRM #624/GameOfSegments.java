package TC;

import java.util.HashSet;
import java.util.Set;

public class GameOfSegments {
    public int winner(int N) {
        int MAXN = 1000;
        int[] SG = new int[MAXN + 1];
        SG[0] = SG[1] = 0;
        SG[2] = SG[3] = 1;
        for (int i = 4; i <= N; ++i) {
            Set<Integer> has = new HashSet<Integer>();
            for (int j = 0; j + 2 <= i; ++j)
                has.add(SG[j] ^ SG[i - j - 2]);
            for (int j = 0;; ++j) {
                if (!has.contains(j)) {SG[i] = j; break;}
            }
        }
        return SG[N] > 0 ? 1 : 2;
    }
}
