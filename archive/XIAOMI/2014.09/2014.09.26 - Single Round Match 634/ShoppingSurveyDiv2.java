package TC;

public class ShoppingSurveyDiv2 {
    public int minValue(int N, int[] s) {
        int join = N;
        for (int i = 0; i < s.length; ++i) {
            join = join + s[i] - N;
            if (join <= 0) return 0;
        }
        return join;
    }
}
