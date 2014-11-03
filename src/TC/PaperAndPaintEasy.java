package TC;

public class PaperAndPaintEasy {
    public long computeArea(int width, int height, int xfold, int cnt, int x1, int y1, int x2, int y2) {
        int M = Math.max(width - xfold, xfold), N = height / (cnt + 1);
        int x0 = Math.min(width - xfold, xfold);
        int ret = 0;
        if (x1 < x0 && x0 < x2) {
            ret = (x0 - x1) * (y2 - y1) * 2 + (x2 - x0) * (y2 - y1);
        } else if (x2 <= x0) {
            ret = (x2 - x1) * (y2 - y1) * 2;
        } else {
            ret = (x2 - x1) * (y2 - y1);
        }
        ret = width * height - ret * (cnt + 1);
        return ret;
    }
}
