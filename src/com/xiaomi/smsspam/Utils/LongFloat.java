package com.xiaomi.smsspam.Utils;

/**
 * Created by dy on 14-10-24.
 */
public class LongFloat {
    private int mE;
    private double mD;

    public LongFloat(int e, double d) {
        mD = d;
        mE = e;
    }

    public LongFloat(double d) {
        mD = d;
        mE = 0;
        if (mD > 0.0) {
            while (mD < 1.0) {
                mD *= 10;
                mE--;
            }
            while (mD > 10) {
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

    public void multiply(double d){
        mD *= d;
        while(mD < 1.0){
            mD *= 10;
            mE--;
        }
        while(mD > 10){
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
