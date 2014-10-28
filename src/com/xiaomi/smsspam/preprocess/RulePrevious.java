package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.util.ArrayList;
import java.util.List;

public abstract class RulePrevious extends Rule {
	
	protected boolean mHit = false;
    private int startIndex = 1;

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public abstract void reset();

    public abstract void process(Corpus cps);

    //train the parameters of the rule
    public abstract void train(List<Corpus> cpss);

    @Override
    public String getName() {
        return "an unknown previous rule";
    }
}
