package com.xiaomi.smsspam;

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
//        System.out.println("RRRR:" + r + "     sn:" + s + ":" + n + "    big_small:" + big + "_" + small);
        if((ig > 0.01) || (ig > 0.001 && r >= 4)){
            return true;
        }
        return false;
    }
}

class Pair{
    int i;
    double p;
    Pair(int ii, double pp){
        i = ii;
        p = pp;
    }
}

class PairCount{
    String phs;
    int[] vals;
    double ig;
    //
    boolean dup = false;
    PairCount(String p, int[] v){
        phs = p;
        vals = v;
    }
}

class LongFloat{
    private int mE;
    private double mD;
    public LongFloat(int e, double d){
        mD = d;
        mE = e;
    }

    public LongFloat(double d){
        mD = d;
        mE = 0;
        if(mD > 0.0){
            while(mD < 1.0){
                mD *= 10;
                mE--;
            }
            while(mD > 10){
                mD /= 10;
                mE++;
            }
        }
    }

    public double div(LongFloat lf){
        int e = mE - lf.mE;
        double d = mD / lf.mD;
        while(e < 0){
            d /= 10;
            e++;
        }
        while(e > 0){
            d *= 10;
            e--;
        }
        return d;
    }
    //changed by DY
    public void multiply(double d){

        while(d < 1.0){
            d *= 10;
            mE--;
        }

        mD *= d;
        while (mD > 10) {
            mD /= 10;
            mE++;
        }
    }

    public boolean compare(LongFloat lf){
        if(mE > lf.mE){
            return true;
        }else if(mE < lf.mE){
            return false;
        }else{
            return mD > lf.mD;
        }
    }

    protected Object copy() {
        LongFloat lf = new LongFloat(0.0);
        lf.mD = this.mD;
        lf.mE = this.mE;
        return lf;
    }

    @Override
    public String toString() {
        return "[" + mD + "," + mE + "]";
    }
    
}

