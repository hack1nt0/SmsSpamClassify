package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Options;
import com.xiaomi.smsspam.Utils.Corpus;

import java.io.*;
import java.util.Collection;
import java.util.List;

public abstract class RulePrevious extends Rule {
	
    protected BufferedWriter modelOut;
    protected BufferedWriter extractedRulesOut;
    protected BufferedReader modelIn;

    private int startIndex = 1;

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public abstract void reset();

    public abstract void updRemainingBody(Corpus cps);

    public void writeCurRules(BufferedWriter out, Collection curRules) {
        if (!Options.PRINT_CUR_RULES) return;
        try {
            out.write(curRules + "\n");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void process(Corpus cps);

    //train the parameters of the rule
    public abstract void train(List<Corpus> cpss);

    @Override
    public String getName() {
        return "an unknown previous rule";
    }
}
