package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Utils.myReader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public abstract class Rule {
	
	protected static final int DEFAULT_SUB_COUNT = 1;
    protected static final int MAX_NAME_LENGTH = 30;

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

    public int getSubFeatureCnt() {
        return getSubFeatureNames().length;
    }

    public abstract String[] getSubFeatureNames();


    public abstract void readDef(DataInputStream dataIn) throws IOException;
    public abstract void writeDef(DataOutputStream dataOut) throws IOException;
}
