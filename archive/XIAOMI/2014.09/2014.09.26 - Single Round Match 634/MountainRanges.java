package TC;

public class MountainRanges {
    public int countPeaks(int[] heights) {
        int ret = 0;
        for (int i = 0; i < heights.length; ++i) {
            int l = i == 0 ? 0 : heights[i - 1];
            int r = i == heights.length - 1 ? 0 : heights[i + 1];
            if (l < heights[i] && heights[i] > r) ret++;
        }
        return ret;
    }
}
