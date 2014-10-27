package TC;

public class RunningAroundPark {
    public int numberOfLap(int N, int[] d) {
        int ret = 1;
        for (int i = 1; i < d.length; ++i) {
           if (d[i - 1] >= d[i]) ++ret;
        }
        return ret;
    }
}
