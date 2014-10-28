package com.xiaomi.smsspam.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dy on 14-10-25.
 */
public class Statistics {
    public static double getMI(List<Corpus> cpss, int X, int Y) {
        return getEntropy(cpss, X) + getEntropy(cpss, Y) - getEntropy(cpss, X, Y);
    }

    public static double getIG(List<Corpus> cpss, int X, int Y) {
        return getMI(cpss, X, Y);
    }


    public static double getIGRatio(List<Corpus> cpss, int X, int Y) {
        double MI = getMI(cpss, X, Y);
        if (MI == 0) return 0;
        double HX = getEntropy(cpss, X);
        assert HX == 0;
        return MI / HX;
    }

    //joint probability
    public static double getEntropy(List<Corpus> cpss, int... Xs) {
        Map<Long, Double> P = new HashMap<>();
        for (Corpus cps: cpss) {
            int[] X = cps.getX();
            long id = 0;
            for (int i = 0; i < Xs.length; ++i) {
                if (X[Xs[i]] == 0) continue;
                id += 1L << Xs.length - 1 - i;
            }
            P.put(id, P.containsKey(id) ? P.get(id) + 1 : 1);
        }
        double res = 0.0;
        for (Long xs: P.keySet()) {
            P.put(xs, P.get(xs) / cpss.size());
            res += P.get(xs) * Math.log(P.get(xs)) / Math.log(2);
        }
        return -res;
    }

    public static double getCondEntropy(int X, int Y, List<Corpus> cpss) {
        return getEntropy(cpss, X) - getMI(cpss, X, Y);
    }
}
