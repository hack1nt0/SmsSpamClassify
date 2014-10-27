package com.xiaomi.smsspam.Utils;

public class Utils {
	
	public static final int SPAM = 0;
    public static final int NORMAL = 1;
    public static final int CLASS_COUNT = 2;

    private static final double MIN_DOUBLE = 0.0000000000001;
    public static double getEntropy(int total, int spam){
        double pSpam = 1.0 * spam / total;
        double pNormal = 1.0 * (total - spam) / total;
        double entropy = -pSpam * log2(pSpam) - pNormal * log2(pNormal);
        return entropy;
    }

    public static double log2(double v){
        if(Math.abs(v) < MIN_DOUBLE){
            return Math.log(MIN_DOUBLE) / Math.log(2);
        }
        return Math.log(v) / Math.log(2);
    }

    public static boolean goodInfo(double ig, int s, int n){
        int big = s > n ? s : n;
        int small = s > n ? n : s;
        small = small > 0 ? small : 1;
        int r = big / small;
        if((ig > 0.01) || (ig > 0.001 && r >= 4)){
            return true;
        }
        return false;
    }
}




