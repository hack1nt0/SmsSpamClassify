package TC;

import java.util.ArrayList;
import java.util.Arrays;

public class CandyMaking {
    public double findSuitableDensity(int[] containerVolume, int[] desiredWeight) {
        int N = containerVolume.length;
        ArrayList<Double> lst = new ArrayList<Double>();
        for (int i = 0; i < N; ++i)
            for (int j = 0; j < containerVolume[i]; ++j)
                lst.add(desiredWeight[i] / (containerVolume[i] + 0.0));
        Double[] arr = lst.toArray(new Double[0]);
        Arrays.sort(arr);
        double ans = 0;
        for (double ele: arr)
            ans += Math.abs(ele - arr[N / 2 - 1]);
        return ans;
    }
}
