package TC;

public class SuffixArrayDiv1 {
    public int minimalCharacters(int[] SA) {
        int n = SA.length;
        int[] rank = new int[n + 1];
        rank[n] = -1;
        for (int i = 0; i < n; ++i)
            rank[SA[i]] = i;
        int ans = 1;
        for (int R = 1; R < n; ++R) {
            if (rank[SA[R] + 1] < rank[SA[R - 1] + 1])
                ans++;
        }
        return ans;
    }
}
