package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.util.ArrayList;
import java.util.List;

public abstract class RulePrevious extends Rule {
	
	protected boolean mHit = false;

    public abstract void reset();

    public abstract boolean fit(Corpus cps, int startIndex);

    protected abstract List<String> process(String str);

    public List<String> process(List<String> strs){
        reset();
        List<String> ret = new ArrayList<String>();
        for(String s : strs){
            ret.addAll(process(s));
        }
        return ret;
    }

    @Override
    public String getName() {
        return "an unknown previous rule";
    }
}
