package TC;

public class TrianglesContainOriginEasy {//WA
    public int count(int[] x, int[] y) {
        int N = x.length;
        int ret = 0;
        for (int i = 0; i < N; ++i)
            for (int j = i + 1; j < N; ++j)
                for (int k = j + 1; k < N; ++k) {
                    int a = cross(x[i], y[i], x[j], y[j]);
                    int b = cross(x[j], y[j], x[k], y[k]);
                    int c = cross(x[k], y[k], x[i], y[i]);
                    int tot = cross(x[i] - x[j], y[i]- y[j], x[k] - x[j], y[k] - y[j]);
                    if (Math.abs(tot) == Math.abs(a + b + c)) ++ret;
                }
        return ret;
    }

    private int cross(int xa, int ya, int xb, int yb) {
        return xa * yb - xb * ya;
    }
}
