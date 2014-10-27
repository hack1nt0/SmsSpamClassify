package com.xiaomi.smsspam.preprocess;

import java.util.ArrayList;
import java.util.List;

public abstract class RulePrevious extends Rule {
	
	protected boolean mHit = false;

    protected void reset(){
        mHit = false;
    }

    public boolean fit(int[] vals, int start){
        if(null == vals || start >= vals.length){
            return false;
        }
        return doFitting(vals, start);
    }

    public abstract boolean doFitting(int[] vals, int start);

    protected abstract List<String> process(String str);

    public List<String> process(List<String> strs){
        List<String> ret = new ArrayList<String>();
        for(String s : strs){
            ret.addAll(process(s));
        }
        return ret;
    }
}
