package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Utils.Statistics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

// Assume the length of spam of SMS is longer than normal
public class SmsLength extends RulePrevious {
    int len1; //[len0=0, len1, len2=MAXLEN]

    @Override
    public int subClassCount() {
        return 1;
    }

    @Override
    public void train(List<Corpus> cpss) {
        int MAXLen = 0;
        for (Corpus cps: cpss) MAXLen = Math.max(getLen(cps), MAXLen);
        double MAXIG = 0;
        for (int len = 0; len <= MAXLen; ++len) {
            for (Corpus cps: cpss) cps.getX()[1] = getLen(cps) <= len ? 0 : 1;
            double IG = Statistics.getIG(cpss, 1, 0);
            if (IG > MAXIG) {MAXIG = IG; len1 = len;}
        }
    }

    private int getLen(Corpus cps) {
        return cps.getOriginalBody().length();
    }

    @Override
    public void reset() {
    }

    @Override
    public void updRemainingBody(Corpus cps) {

    }

    @Override
    public void process(Corpus cps) {
        cps.getX()[this.getStartIndex()] = getLen(cps) <= len1 ? 0 : 1;
    }

    @Override
	public String getName() {
		return "SmsLength";
	}


	@Override
	public void readDef(DataInputStream dataIn) throws IOException {
		len1 = dataIn.readInt();
	}


	@Override
	public void writeDef(DataOutputStream dataOut) throws IOException {
		dataOut.writeInt(len1);
		
	}
}
