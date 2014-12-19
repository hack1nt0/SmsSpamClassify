package TC;

public class BuyingTshirts {
    public int meet(int T, int[] Q, int[] P) {
        int N = Q.length;
        int ret = 0;
        boolean[] vis = new boolean[N];
        for (int i = 0, cnt = 0; i < N;) {
            while (i < N && cnt < T) cnt += Q[i++];
            if (cnt >= T) {
                cnt -= T;
                vis[i - 1] = true;
            }
        }
        for (int i = 0, cnt = 0; i < N;) {
            while (i < N && cnt < T) cnt += P[i++];
            if (cnt >= T) {
                cnt -= T;
                ret += vis[i - 1] ? 1 : 0;
            }
        }
        return ret;
    }
}
