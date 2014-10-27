package TC;

public class TaroGrid {
    public int getNumber(String[] grid) {
        int ret = 1;
        for (int c = 0; c < grid.length; ++c) {
            int res = 1;
            for (int r = 1; r < grid.length; ++r) {
                res = grid[r].charAt(c) == grid[r - 1].charAt(c) ? res + 1 : 1;
                ret = Math.max(res, ret);
            }
        }
        return ret;
    }
}
