package TC;

public class ShufflingCardsDiv2 {
    public String shuffle(int[] permutation) {//WA
        String IMPOSSIBLE = "Impossible";
        String POSSIBLE = "Possible";
        int N = permutation.length / 2;
        int cnt = 0;
        for (int i = 0; i < permutation.length; i += 2) {
            if (permutation[i] <= N) ++cnt;
        }
        if (cnt == (N + 1) / 2) return POSSIBLE;
        return IMPOSSIBLE;
    }
}
