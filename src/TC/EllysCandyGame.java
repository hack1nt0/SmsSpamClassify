package TC;

import java.util.Arrays;

public class EllysCandyGame {
    public String getWinner(int[] sweets) {//WA unknown edge case
        int N = sweets.length;
        int tot = 1 << N;
        int[] max = new int[tot];
        Arrays.fill(max, Integer.MIN_VALUE / 3);
        int[] min = new int[tot];
        Arrays.fill(min, Integer.MAX_VALUE / 3);
        for (int S = 0; S < tot; ++S) {
            boolean canTrans = false;
            for (int i = 0; i < N; ++i) {
                if ((S >> i & 1) == 0 || sweets[i] == 0) continue;
                canTrans = true;
                int curSweet = sweets[i];
                if (0 < i - 1 && (S >> i - 1 & 1) == 0 && sweets[i - 1] > 0) curSweet *= 2;
                if (i + 1 < N && (S >> i + 1 & 1) == 0 && sweets[i + 1] > 0) curSweet *= 2;
                max[S] = Math.max(min[S ^ 1 << i] + curSweet, max[S]);
                min[S] = Math.min(max[S ^ 1 << i] - curSweet, min[S]);
            }
            if (!canTrans) max[S] = min[S] = 0;
        }
        int res = max[tot - 1];
        if (res == 0) return "Draw";
        if (res > 0) return "Elly";
        else return "Kris";
    }
}
