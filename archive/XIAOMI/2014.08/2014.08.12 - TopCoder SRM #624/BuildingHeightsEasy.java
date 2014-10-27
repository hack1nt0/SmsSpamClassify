package TC;

import java.util.Arrays;

public class BuildingHeightsEasy {
    public int minimum(int M, int[] heights) {
        Arrays.sort(heights);
        int ans = Integer.MAX_VALUE;
        for (int i = 0; i < heights.length; ++i) {
            if (i + 1 < heights.length && heights[i] == heights[i + 1] || i + 1 < M)
                continue;
            int res = 0;
            for (int j = i; i - j + 1 <= M; --j)
                res += heights[i] - heights[j];
            ans = Math.min(res, ans);
        }
        return ans;
    }
}
