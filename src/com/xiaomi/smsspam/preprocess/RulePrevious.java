package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Options;
import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Utils.myReader;
import com.xiaomi.smsspam.Utils.myWriter;

import java.io.*;
import java.util.Collection;
import java.util.List;

public abstract class RulePrevious extends Rule {
	
    protected myWriter modelOut;
    protected myWriter extractedRulesOut;
    protected myReader modelIn;

    private int startIndex = 1;

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public abstract void reset();

    public abstract void updRemainingBody(Corpus cps);

    public abstract void process(Corpus cps);

    //train the parameters of the rule
    public abstract void train(List<Corpus> cpss);

    @Override
    public String getName() {
        return "an unknown previous rule";
    }
}
