package TC;

import java.util.ArrayDeque;
import java.util.Arrays;

public class Target {
    public String[] draw(int n) {
        char[][] res = new char[n][n];
        for (int i = 0; i < res.length; ++i) Arrays.fill(res[i], ' ');
        for (int l = n; l > 0; l -= 4)
            for (int dl = 0; dl < l; ++dl) {
                int sr = (n - l) / 2;
                res[sr][sr + dl] = res[sr + l - 1][sr + dl] = res[sr + dl][sr] = res[sr + dl][sr + l - 1] = '#';
            }
        String[] ret = new String[n];
        for (int i = 0; i < ret.length; ++i) ret[i] = new String(res[i]);
        return ret;
    }
}
