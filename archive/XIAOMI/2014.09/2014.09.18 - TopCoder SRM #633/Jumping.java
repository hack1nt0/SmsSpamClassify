package TC;

import java.util.Arrays;

public class Jumping {
    public String ableToGet(int x, int y, int[] jumpLengths) {
        int n = jumpLengths.length;
        boolean able = false;
        if (n == 1) able =  x * x + y * y == jumpLengths[0] * jumpLengths[0];
        else
            for (int i = 0, dc = 0; i < n - 1; dc += jumpLengths[i], ++i) {
                double a = 0, b = 0, c = Math.sqrt(x * x + y * y);
                for (int j = i; j < n; ++j) b += jumpLengths[j];
                for (int j = i; j < n - 1; ++j) {
                    a += jumpLengths[j];
                    b -= jumpLengths[j];
                    able |= validTri(a, b, c + dc) || validTri(a, b, c - dc);
                }
                String s = "\u263F";
                System.out.println(s);
            }
        return able ? "Able" : "Not able";
    }

    private boolean validTri(double a, double b, double c) {
        double[] tri = {a, b, c};
        Arrays.sort(tri);
        return tri[0] >= 0 && tri[0] + tri[1] >= tri[2];
    }
}
